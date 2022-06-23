/*
 * Copyright (C) 2020 The Baremaps Authors
 *
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

package com.baremaps.cli.database;

import com.baremaps.cli.Options;
import com.baremaps.collection.AlignedDataList;
import com.baremaps.collection.DataStore;
import com.baremaps.collection.LongDataMap;
import com.baremaps.collection.LongDataSortedMap;
import com.baremaps.collection.LongSizedDataDenseMap;
import com.baremaps.collection.memory.OnDiskDirectoryMemory;
import com.baremaps.collection.type.LonLatDataType;
import com.baremaps.collection.type.LongDataType;
import com.baremaps.collection.type.LongListDataType;
import com.baremaps.collection.type.PairDataType;
import com.baremaps.collection.utils.FileUtils;
import com.baremaps.database.ImportService;
import com.baremaps.database.postgres.PostgresUtils;
import com.baremaps.database.repository.HeaderRepository;
import com.baremaps.database.repository.PostgresHeaderRepository;
import com.baremaps.database.repository.PostgresNodeRepository;
import com.baremaps.database.repository.PostgresRelationRepository;
import com.baremaps.database.repository.PostgresWayRepository;
import com.baremaps.database.repository.Repository;
import com.baremaps.osm.domain.Node;
import com.baremaps.osm.domain.Relation;
import com.baremaps.osm.domain.Way;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;
import javax.sql.DataSource;
import org.locationtech.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "import", description = "Import data in the database.")
public class Import implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Import.class);

  @Mixin private Options options;

  @Option(
      names = {"--config"},
      paramLabel = "CONFIG",
      description = "The configuration file.",
      required = true)
  private Path config;

  @Option(
      names = {"--database"},
      paramLabel = "DATABASE",
      description = "The JDBC url of the database.",
      required = true)
  private String database;

  @Option(
      names = {"--directory"},
      paramLabel = "DIRECTORY",
      description = "The directory used to store data.")
  private Path directory;

  @Option(
      names = {"--srid"},
      paramLabel = "SRID",
      description = "The projection used by the database.")
  private int srid = 3857;

  @Override
  public Integer call() throws Exception {
    DataSource datasource = PostgresUtils.dataSource(database);
    HeaderRepository headerRepository = new PostgresHeaderRepository(datasource);
    Repository<Long, Node> nodeRepository = new PostgresNodeRepository(datasource);
    Repository<Long, Way> wayRepository = new PostgresWayRepository(datasource);
    Repository<Long, Relation> relationRepository = new PostgresRelationRepository(datasource);

    Path directory = Files.createTempDirectory(Paths.get("."), "baremaps_");
    Path nodes = Files.createDirectories(directory.resolve("nodes"));
    Path referencesKeys = Files.createDirectories(directory.resolve("references_keys"));
    Path referencesValues = Files.createDirectories(directory.resolve("references_values"));

    LongDataMap<Coordinate> coordinates =
        new LongSizedDataDenseMap<>(new LonLatDataType(), new OnDiskDirectoryMemory(nodes));
    LongDataMap<List<Long>> references =
        new LongDataSortedMap<>(
            new AlignedDataList<>(
                new PairDataType<>(new LongDataType(), new LongDataType()),
                new OnDiskDirectoryMemory(referencesKeys)),
            new DataStore<>(new LongListDataType(), new OnDiskDirectoryMemory(referencesValues)));

    logger.info("Importing data");
    new ImportService(
            config,
            coordinates,
            references,
            headerRepository,
            nodeRepository,
            wayRepository,
            relationRepository,
            4326,
            srid)
        .call();

    FileUtils.deleteRecursively(directory);

    logger.info("Done");

    return 0;
  }
}
