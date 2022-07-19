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

package com.baremaps.workflow.tasks;

import com.baremaps.workflow.Task;
import com.baremaps.workflow.WorkflowException;
import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public record UnzipFile(
    String file,
    String directory)
    implements Task {

  @Override
  public void run() {
    var filePath = Paths.get(file);
    var directoryPath = Paths.get(directory);
    try (var zis = new ZipInputStream(new BufferedInputStream(Files.newInputStream(filePath)))) {
      ZipEntry ze;
      while ((ze = zis.getNextEntry()) != null) {
        var file = directoryPath.resolve(ze.getName());
        Files.createDirectories(file.getParent());
        Files.copy(zis, file, StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (Exception e) {
      throw new WorkflowException(e);
    }
  }
}
