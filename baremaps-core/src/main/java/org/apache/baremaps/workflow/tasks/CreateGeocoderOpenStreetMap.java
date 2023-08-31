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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.apache.baremaps.database.collection.AppendOnlyBuffer;
import org.apache.baremaps.database.collection.DataMap;
import org.apache.baremaps.database.collection.MemoryAlignedDataList;
import org.apache.baremaps.database.collection.MemoryAlignedDataMap;
import org.apache.baremaps.database.collection.MonotonicDataMap;
import org.apache.baremaps.database.memory.MemoryMappedDirectory;
import org.apache.baremaps.database.type.LongDataType;
import org.apache.baremaps.database.type.LongListDataType;
import org.apache.baremaps.database.type.PairDataType;
import org.apache.baremaps.database.type.geometry.LonLatDataType;
import org.apache.baremaps.geocoder.GeocoderConstants;
import org.apache.baremaps.geocoderosm.OSMNodeDocumentMapper;
import org.apache.baremaps.openstreetmap.model.Block;
import org.apache.baremaps.openstreetmap.model.DataBlock;
import org.apache.baremaps.openstreetmap.model.HeaderBlock;
import org.apache.baremaps.openstreetmap.model.Node;
import org.apache.baremaps.openstreetmap.pbf.PbfBlockReader;
import org.apache.baremaps.stream.StreamUtils;
import org.apache.baremaps.utils.FileUtils;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.MMapDirectory;
import org.locationtech.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record CreateGeocoderOpenStreetMap(Path file, Integer databaseSrid,Path indexDirectory)
    implements
    Task {

  private static final Logger logger = LoggerFactory.getLogger(CreateGeocoderOpenStreetMap.class);

  @Override
  public void execute(WorkflowContext context) throws Exception {

    var path = file.toAbsolutePath();


    var cacheDir = Files.createTempDirectory(Paths.get("."), "cache_");

    DataMap<Long, Coordinate> coordinateMap;
    if (Files.size(path) > 1 << 30) {
      var coordinateDir = Files.createDirectories(cacheDir.resolve("coordinate_keys"));
      coordinateMap = new MemoryAlignedDataMap<>(
          new LonLatDataType(),
          new MemoryMappedDirectory(coordinateDir));
    } else {
      var coordinateKeysDir = Files.createDirectories(cacheDir.resolve("coordinate_keys"));
      var coordinateValuesDir = Files.createDirectories(cacheDir.resolve("coordinate_vals"));
      coordinateMap =
          new MonotonicDataMap<>(
              new MemoryAlignedDataList<>(
                  new PairDataType<>(new LongDataType(), new LongDataType()),
                  new MemoryMappedDirectory(coordinateKeysDir)),
              new AppendOnlyBuffer<>(
                  new LonLatDataType(),
                  new MemoryMappedDirectory(coordinateValuesDir)));
    }

    var referenceKeysDir = Files.createDirectory(cacheDir.resolve("reference_keys"));
    var referenceValuesDir = Files.createDirectory(cacheDir.resolve("reference_vals"));
    var referenceMap =
        new MonotonicDataMap<>(
            new MemoryAlignedDataList<>(
                new PairDataType<>(new LongDataType(), new LongDataType()),
                new MemoryMappedDirectory(referenceKeysDir)),
            new AppendOnlyBuffer<>(
                new LongListDataType(),
                new MemoryMappedDirectory(referenceValuesDir)));

    var directory = MMapDirectory.open(indexDirectory);
    var config = new IndexWriterConfig(GeocoderConstants.ANALYZER);

    try(var indexWriter = new IndexWriter(directory, config)) {
      var importer = new BlockGeocoderImporter(indexWriter);
      execute(
          path,
          coordinateMap,
          referenceMap,
          databaseSrid, importer);
    }
    FileUtils.deleteRecursively(cacheDir);
  }

  public static void execute(
      Path path,
      DataMap<Long, Coordinate> coordinateMap,
      DataMap<Long, List<Long>> referenceMap,
      Integer databaseSrid, BlockGeocoderImporter importer) throws IOException {

    // configure the block reader
    var reader = new PbfBlockReader()
        .geometries(true)
        .projection(databaseSrid)
        .coordinateMap(coordinateMap)
        .referenceMap(referenceMap);

    try (var input = Files.newInputStream(path)) {
      StreamUtils.batch(reader.stream(input)).forEach(importer);
    }

  }

  static class BlockGeocoderImporter implements Consumer<Block> {


    private final IndexWriter indexWriter;

    public BlockGeocoderImporter(IndexWriter indexWriter) {
      this.indexWriter = indexWriter;
    }

    @Override
    public void accept(Block block) {
      if (block instanceof HeaderBlock headerBlock) {
        // headerBlock.getHeader();
      } else if (block instanceof DataBlock dataBlock) {
        var documents = Stream
            .concat(dataBlock.getDenseNodes().stream(), dataBlock.getNodes().stream())
            .filter(node -> node.getTags().containsKey("population"))
            .map(new OSMNodeDocumentMapper());
        try {
          indexWriter.addDocuments((Iterable<Document>) documents::iterator);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }

      }
    }
  }
}
