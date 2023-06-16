/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.baremaps.workflow.tasks;

import static org.apache.baremaps.utils.ObjectMapperUtils.objectMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.apache.baremaps.config.ConfigReader;
import org.apache.baremaps.stream.ProgressLogger;
import org.apache.baremaps.stream.StreamUtils;
import org.apache.baremaps.tilestore.*;
import org.apache.baremaps.tilestore.file.FileTileStore;
import org.apache.baremaps.tilestore.mbtiles.MBTiles;
import org.apache.baremaps.tilestore.postgres.PostgresTileStore;
import org.apache.baremaps.vectortile.tileset.Tileset;
import org.apache.baremaps.vectortile.tileset.TilesetQuery;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.locationtech.jts.geom.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteConfig.JournalMode;
import org.sqlite.SQLiteConfig.LockingMode;
import org.sqlite.SQLiteConfig.SynchronousMode;
import org.sqlite.SQLiteConfig.TempStore;
import org.sqlite.SQLiteDataSource;

public record ExportVectorTiles(
    Path tileset,
    Path repository,
    int batchArraySize,
    int batchArrayIndex,
    boolean mbtiles) implements Task {

  private static final Logger logger = LoggerFactory.getLogger(ExportVectorTiles.class);

  @Override
  public void execute(WorkflowContext context) throws Exception {
    var configReader = new ConfigReader();
    var objectMapper = objectMapper();
    var tileset = objectMapper.readValue(configReader.read(this.tileset), Tileset.class);
    var datasource = context.getDataSource(tileset.getDatabase());
    var sourceTileStore = sourceTileStore(tileset, datasource);
    var targetTileStore = targetTileStore(tileset);

    var envelope = tileset.getBounds().size() == 4
        ? new Envelope(
            tileset.getBounds().get(0), tileset.getBounds().get(2),
            tileset.getBounds().get(1), tileset.getBounds().get(3))
        : new Envelope(-180, 180, -85.0511, 85.0511);

    var count = TileCoord.count(envelope, tileset.getMinzoom(), tileset.getMaxzoom());

    var stream =
        StreamUtils.stream(TileCoord.iterator(envelope, tileset.getMinzoom(), tileset.getMaxzoom()))
            .peek(new ProgressLogger<>(count, 5000));

    StreamUtils.batch(stream, 10).forEach(new TileChannel(sourceTileStore, targetTileStore));
  }

  private TileStore sourceTileStore(Tileset tileset, DataSource datasource) {
    return new PostgresTileStore(datasource, tileset);
  }

  private TileStore targetTileStore(Tileset source) throws TileStoreException, IOException {
    if (mbtiles) {
      Files.deleteIfExists(repository);
      var dataSource = createDataSource(repository);
      var tilesStore = new MBTiles(dataSource);
      tilesStore.initializeDatabase();
      tilesStore.writeMetadata(metadata(source));
      return tilesStore;
    } else {
      return new FileTileStore(repository);
    }
  }

  private Map<String, String> metadata(Tileset tileset) throws JsonProcessingException {
    var metadata = new HashMap<String, String>();

    metadata.put("name", tileset.getName());
    metadata.put("version", tileset.getVersion());
    metadata.put("description", tileset.getDescription());
    metadata.put("attribution", tileset.getAttribution());
    metadata.put("type", "baselayer");
    metadata.put("format", "pbf");
    metadata.put(
        "center",
        tileset.getCenter().stream().map(Number::toString).collect(Collectors.joining(", ")));
    metadata.put(
        "bounds",
        tileset.getBounds().stream().map(Object::toString).collect(Collectors.joining(", ")));
    metadata.put("minzoom", Double.toString(tileset.getMinzoom()));
    metadata.put("maxzoom", Double.toString(tileset.getMaxzoom()));

    var layers =
        tileset.getVectorLayers().stream()
            .map(
                layer -> {
                  Map<String, Object> map = new HashMap<>();
                  map.put("id", layer.getId());
                  map.put("description", layer.getDescription());
                  map.put(
                      "minzoom",
                      layer.getQueries().stream().mapToInt(TilesetQuery::getMinzoom).min()
                          .getAsInt());
                  map.put(
                      "maxzoom",
                      layer.getQueries().stream().mapToInt(TilesetQuery::getMaxzoom).max()
                          .getAsInt());
                  return map;
                })
            .toList();

    metadata.put("json", new ObjectMapper().writeValueAsString(layers));

    return metadata;
  }

  /**
   * Create a SQLite data source.
   * 
   * @param path the path to the SQLite database
   * @return the SQLite data source
   */
  public static DataSource createDataSource(Path path) {
    var sqliteConfig = new SQLiteConfig();
    sqliteConfig.setCacheSize(1000000);
    sqliteConfig.setPageSize(65536);
    sqliteConfig.setJournalMode(JournalMode.OFF);
    sqliteConfig.setLockingMode(LockingMode.EXCLUSIVE);
    sqliteConfig.setSynchronous(SynchronousMode.OFF);
    sqliteConfig.setTempStore(TempStore.MEMORY);

    var sqliteDataSource = new SQLiteDataSource();
    sqliteDataSource.setConfig(sqliteConfig);
    sqliteDataSource.setUrl("jdbc:sqlite:" + path);

    var hikariConfig = new HikariConfig();
    hikariConfig.setDataSource(sqliteDataSource);
    hikariConfig.setMaximumPoolSize(1);

    return new HikariDataSource(hikariConfig);
  }
}
