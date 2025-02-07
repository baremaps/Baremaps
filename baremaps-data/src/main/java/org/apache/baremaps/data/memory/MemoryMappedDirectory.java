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

package org.apache.baremaps.data.memory;



import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.apache.baremaps.data.util.FileUtils;
import org.apache.baremaps.data.util.MappedByteBufferUtils;


/**
 * A {@link Memory} that stores segments on-disk using mapped byte buffers in a directory of files.
 */
public class MemoryMappedDirectory extends Memory<MappedByteBuffer> {

  private final Path directory;

  /**
   * Constructs an {@link MemoryMappedDirectory} with a custom directory and a default segment size
   * of 1gb.
   *
   * @param directory the directory that stores the data
   */
  public MemoryMappedDirectory(Path directory) {
    this(directory, 1 << 30);
    if (!Files.exists(directory)) {
      try {
        Files.createDirectories(directory);
      } catch (IOException e) {
        throw new MemoryException(e);
      }
    }
  }

  /**
   * Constructs an {@link MemoryMappedDirectory} with a custom directory and a custom segment size.
   *
   * @param directory the directory that stores the data
   * @param segmentSize the size of the segments in bytes
   */
  public MemoryMappedDirectory(Path directory, int segmentSize) {
    super(1 << 14, segmentSize);
    this.directory = directory;
  }

  @Override
  protected MappedByteBuffer allocateHeader() {
    try {
      Path file = directory.resolve("header");
      try (FileChannel channel = FileChannel.open(file, StandardOpenOption.CREATE,
          StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        return channel.map(MapMode.READ_WRITE, 0, headerSize());
      }
    } catch (IOException e) {
      throw new MemoryException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  protected MappedByteBuffer allocateSegment(int index) {
    try {
      Path file = directory.resolve(String.format("%s.part", index));
      try (FileChannel channel = FileChannel.open(file, StandardOpenOption.CREATE,
          StandardOpenOption.READ, StandardOpenOption.WRITE)) {
        return channel.map(MapMode.READ_WRITE, 0, segmentSize());
      }
    } catch (IOException e) {
      throw new MemoryException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void close() throws IOException {
    MappedByteBufferUtils.unmap(header);
    for (MappedByteBuffer buffer : segments) {
      MappedByteBufferUtils.unmap(buffer);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void clear() throws IOException {
    close();
    header.clear();
    segments.clear();
    FileUtils.deleteRecursively(directory);
  }

}
