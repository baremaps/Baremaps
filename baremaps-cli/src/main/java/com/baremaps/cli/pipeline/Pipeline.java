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

package com.baremaps.cli.pipeline;

import com.baremaps.blob.Blob;
import com.baremaps.blob.BlobStore;
import com.baremaps.cli.Options;
import com.baremaps.pipeline.model.Config;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "pipeline", description = "Execute a pipeline.")
public class Pipeline implements Callable<Integer> {

  @Mixin private Options options;

  @Option(
      names = {"--config"},
      paramLabel = "CONFIG",
      description = "The pipeline configuration.",
      required = true)
  private URI config;

  @Override
  public Integer call() throws Exception {
    BlobStore blobStore = options.blobStore();
    Blob blob = blobStore.get(config);
    ObjectMapper mapper = new ObjectMapper();
    Config value = mapper.readValue(blob.getInputStream(), Config.class);
    Path directory = Files.createDirectories(Paths.get("pipeline"));
    // Pipeline pipeline = new Pipeline(directory, blobStore);
    // pipeline.execute();
    return 0;
  }
}
