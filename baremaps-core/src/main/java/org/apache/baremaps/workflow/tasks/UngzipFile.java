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

package org.apache.baremaps.workflow.tasks;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.GZIPInputStream;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.apache.baremaps.workflow.WorkflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ungzip a file.
 */
@JsonTypeName("UngzipFile")
public class UngzipFile implements Task {

  private static final Logger logger = LoggerFactory.getLogger(UngzipFile.class);

  private Path file;
  private Path directory;

  /**
   * Constructs an {@code UngzipFile}.
   */
  public UngzipFile() {

  }

  /**
   * Constructs an {@code UngzipFile}.
   *
   * @param file the file
   * @param directory the directory
   */
  public UngzipFile(Path file, Path directory) {
    this.file = file;
    this.directory = directory;
  }

  /**
   * Returns the file.
   *
   * @return the file
   */
  public Path getFile() {
    return file;
  }

  /**
   * Sets the file.
   *
   * @param file the file
   */
  public void setFile(Path file) {
    this.file = file;
  }

  /**
   * Returns the directory.
   *
   * @return the directory
   */
  public Path getDirectory() {
    return directory;
  }

  /**
   * Sets the directory.
   *
   * @param directory the directory
   */
  public void setDirectory(Path directory) {
    this.directory = directory;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute(WorkflowContext context) throws Exception {
    var filePath = file.toAbsolutePath();
    var directoryPath = directory.toAbsolutePath();
    try (var zis = new GZIPInputStream(new BufferedInputStream(Files.newInputStream(filePath)))) {
      var decompressed = directoryPath.resolve(filePath.getFileName().toString().substring(0,
          filePath.getFileName().toString().length() - 3));
      if (!Files.exists(decompressed)) {
        Files.createDirectories(decompressed.getParent());
        Files.createFile(decompressed);
      }
      Files.copy(zis, decompressed, StandardCopyOption.REPLACE_EXISTING);
    } catch (Exception e) {
      throw new WorkflowException(e);
    }
  }
}
