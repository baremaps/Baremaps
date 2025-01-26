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
import java.nio.ByteBuffer;

/** A {@link Memory} that stores segments on-heap using regular byte buffers. */
public class OnHeapMemory extends Memory<ByteBuffer> {

  /** Constructs an {@link OnHeapMemory} with a default segment size of 1mb. */
  public OnHeapMemory() {
    this(1 << 20);
  }

  /**
   * Constructs an {@link OnHeapMemory} with a custom segment size.
   *
   * @param segmentSize the size of the segments in bytes
   */
  public OnHeapMemory(int segmentSize) {
    super(1024, segmentSize);
  }

  /** {@inheritDoc} */
  @Override
  protected ByteBuffer allocateHeader() {
    return ByteBuffer.allocate(headerSize());
  }

  /** {@inheritDoc} */
  @Override
  protected ByteBuffer allocateSegment(int index) {
    return ByteBuffer.allocate(segmentSize());
  }

  /** {@inheritDoc} */
  @Override
  public void close() throws IOException {
    // Nothing to close
  }

  /** {@inheritDoc} */
  @Override
  public void clear() throws IOException {
    // Nothing to clean
  }
}
