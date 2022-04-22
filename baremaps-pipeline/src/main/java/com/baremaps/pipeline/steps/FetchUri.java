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

package com.baremaps.pipeline.steps;

import com.baremaps.pipeline.Context;
import com.baremaps.pipeline.PipelineException;
import com.baremaps.pipeline.Step;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public record FetchUri(String id, List<String> needs, String uri, String path) implements Step {

  @Override
  public void execute(Context context) {
    try (var inputStream = context.blobStore().get(URI.create(uri)).getInputStream()) {
      var downloadFile = context.directory().resolve(path);
      Files.createDirectories(downloadFile.getParent());
      Files.copy(inputStream, downloadFile, StandardCopyOption.REPLACE_EXISTING);
    } catch (Exception e) {
      throw new PipelineException(e);
    }
  }
}
