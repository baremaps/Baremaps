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



import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/** A base class to manage segments of on-heap, off-heap, or on-disk memory. */
public abstract class Memory<T extends ByteBuffer> implements Closeable {

  private final int headerSize;

  private final int segmentSize;

  private final long segmentShift;

  private final long segmentMask;

  protected T header;

  protected List<T> segments = new ArrayList<>();

  /**
   * Constructs a memory with a given segment size.
   *
   * @param segmentSize the size of the segments
   */
  protected Memory(int headerSize, int segmentSize) {
    if ((segmentSize & -segmentSize) != segmentSize) {
      throw new IllegalArgumentException("The segment size must be a power of 2");
    }
    this.headerSize = headerSize;
    this.segmentSize = segmentSize;
    this.segmentShift = (long) (Math.log(this.segmentSize) / Math.log(2));
    this.segmentMask = this.segmentSize - 1l;
  }

  /**
   * Returns the size of the header.
   *
   * @return the size of the header
   */
  public int headerSize() {
    return headerSize;
  }

  /**
   * Returns the size of the segments.
   *
   * @return the size of the segments
   */
  public int segmentSize() {
    return segmentSize;
  }

  /**
   * Returns the bit shift to find a segment index from a memory position.
   *
   * @return the bit shift
   */
  public long segmentShift() {
    return segmentShift;
  }

  /**
   * Returns the bit mask to find a segment offset from a memory position.
   *
   * @return the bit mask
   */
  public long segmentMask() {
    return segmentMask;
  }

  public ByteBuffer header() {
    if (header == null) {
      synchronized (this) {
        header = allocateHeader();
      }
    }
    return header;
  }

  /**
   * Allocates a header.
   *
   * @return the header
   */
  protected abstract T allocateHeader();

  /**
   * Returns a segment of the memory.
   *
   * @param index the index of the segment
   * @return the segment
   */
  public ByteBuffer segment(int index) {
    if (segments.size() <= index) {
      return allocateSegmentInternal(index);
    }
    ByteBuffer segment = segments.get(index);
    if (segment == null) {
      return allocateSegmentInternal(index);
    }
    return segment;
  }

  /** Returns the size of the allocated memory. */
  public long size() {
    return (long) segments.size() * (long) segmentSize;
  }

  /** The allocation of segments is synchronized to enable access by multiple threads. */
  private synchronized ByteBuffer allocateSegmentInternal(int index) {
    while (segments.size() <= index) {
      segments.add(null);
    }
    T segment = segments.get(index);
    if (segment == null) {
      segment = allocateSegment(index);
      segments.set(index, segment);
    }
    return segment;
  }

  /**
   * Allocates a segment for a given index and size.
   *
   * @param index the index of the segment
   * @return the segment
   */
  protected abstract T allocateSegment(int index);

  /**
   * Clears the memory and the underlying resources.
   */
  public abstract void clear() throws IOException;

}
