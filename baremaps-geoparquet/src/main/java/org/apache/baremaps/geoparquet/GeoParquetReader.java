/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.geoparquet;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.baremaps.geoparquet.GeoParquetGroup.Schema;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.s3a.AnonymousAWSCredentialsProvider;
import org.apache.hadoop.fs.s3a.S3AFileSystem;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.metadata.BlockMetaData;
import org.apache.parquet.hadoop.metadata.FileMetaData;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;
import org.apache.parquet.schema.MessageType;

/**
 * This reader is based on the parquet example code located at: org.apache.parquet.example.data.*.
 */
public class GeoParquetReader {

  protected final Configuration configuration;
  protected final List<FileStatus> files;
  private final AtomicLong groupCount = new AtomicLong(-1);

  public GeoParquetReader(URI uri) {
    this(uri, createDefaultConfiguration());
  }

  public GeoParquetReader(URI uri, Configuration configuration) {
    this.configuration = configuration;
    this.files = initializeFiles(uri, configuration);
  }

  private static List<FileStatus> initializeFiles(URI uri, Configuration configuration) {
    try {
      Path globPath = new Path(uri.getPath());
      FileSystem fileSystem = FileSystem.get(uri, configuration);
      FileStatus[] fileStatuses = fileSystem.globStatus(globPath);
      if (fileStatuses == null) {
        throw new GeoParquetException("No files found at the specified URI.");
      }
      return Collections.unmodifiableList(Arrays.asList(fileStatuses));
    } catch (IOException e) {
      throw new GeoParquetException("IOException while attempting to list files.", e);
    }
  }

  public MessageType getParquetSchema() {
    return files.stream()
        .findFirst()
        .map(this::getFileInfo)
        .orElseThrow(
            () -> new GeoParquetException("No files available to read schema.")).messageType;
  }

  private FileInfo getFileInfo(FileStatus fileStatus) {
    try {
      ParquetMetadata parquetMetadata =
          ParquetFileReader.readFooter(configuration, fileStatus.getPath());

      long recordCount = parquetMetadata.getBlocks().stream()
          .mapToLong(BlockMetaData::getRowCount)
          .sum();

      FileMetaData fileMetaData = parquetMetadata.getFileMetaData();
      Map<String, String> keyValueMetadata = fileMetaData.getKeyValueMetaData();
      MessageType messageType = fileMetaData.getSchema();

      GeoParquetMetadata geoParquetMetadata = null;
      Schema geoParquetSchema = null;
      if (keyValueMetadata.containsKey("geo")) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        geoParquetMetadata =
            objectMapper.readValue(keyValueMetadata.get("geo"), GeoParquetMetadata.class);
        geoParquetSchema =
            GeoParquetGroupFactory.createGeoParquetSchema(messageType, geoParquetMetadata);
      }

      return new FileInfo(
          fileStatus,
          recordCount,
          keyValueMetadata,
          messageType,
          geoParquetMetadata,
          geoParquetSchema);

    } catch (IOException e) {
      throw new GeoParquetException("Failed to build FileInfo for file: " + fileStatus, e);
    }
  }

  public GeoParquetMetadata getGeoParquetMetadata() {
    return files.stream()
        .findFirst()
        .map(this::getFileInfo)
        .orElseThrow(this::noParquetFilesAvailable)
        .metadata();
  }

  public Schema getGeoParquetSchema() {
    return files.stream()
        .findFirst()
        .map(this::getFileInfo)
        .orElseThrow(this::noParquetFilesAvailable)
        .geoParquetSchema();
  }

  public GeoParquetException noParquetFilesAvailable() {
    return new GeoParquetException("No parquet files available.");
  }

  public boolean validateSchemasAreIdentical() {
    // Verify that all files have the same schema
    Set<MessageType> schemas = files.parallelStream()
        .map(this::getFileInfo)
        .map(fileInfo -> fileInfo.messageType)
        .collect(Collectors.toSet());
    return schemas.size() == 1;
  }

  public long size() {
    if (groupCount.get() == -1) {
      long totalCount = files.parallelStream()
          .map(this::getFileInfo)
          .mapToLong(fileInfo -> fileInfo.recordCount)
          .sum();
      groupCount.set(totalCount);
    }
    return groupCount.get();
  }

  private Stream<GeoParquetGroup> streamGeoParquetGroups(boolean inParallel) {
    Spliterator<GeoParquetGroup> spliterator = new GeoParquetSpliterator(files, configuration, 0, files.size());
    return StreamSupport.stream(spliterator, inParallel);
  }

  public Stream<GeoParquetGroup> read() {
    return streamGeoParquetGroups(false);
  }

  public Stream<GeoParquetGroup> readParallel() {
    return streamGeoParquetGroups(true);
  }

  private static Configuration createDefaultConfiguration() {
    Configuration conf = new Configuration();
    conf.set("fs.s3a.endpoint", "s3.us-west-2.amazonaws.com");
    conf.set("fs.s3a.aws.credentials.provider", AnonymousAWSCredentialsProvider.class.getName());
    conf.set("fs.s3a.impl", S3AFileSystem.class.getName());
    conf.set("fs.s3a.path.style.access", "true");
    return conf;
  }

  private record FileInfo(
      FileStatus file,
      long recordCount,
      Map<String, String> keyValueMetadata,
      MessageType messageType,
      GeoParquetMetadata metadata,
      Schema geoParquetSchema) {

  }

}
