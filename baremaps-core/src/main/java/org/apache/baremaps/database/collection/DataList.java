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

package org.apache.baremaps.database.collection;



import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An abstract list of data elements that can hold a large number of elements.
 *
 * @param <E> The type of the data.
 */
public interface DataList<E> extends DataCollection<E> {

  /**
   * Appends a value to the list and returns its index.
   *
   * @param value the value
   * @return the index of the value.
   */
  long addIndexed(E value);

  /**
   * {@inheritDoc}
   */
  @Override
  default boolean add(E value) {
    addIndexed(value);
    return true;
  }

  /**
   * Sets the value at the specified index.
   *
   * @param index the index
   * @param value the value
   */
  void set(long index, E value);

  /**
   * Returns the value at the specified index.
   *
   * @param index the index
   * @return the value
   */
  E get(long index);

  /** {@inheritDoc} */
  @Override
  default Iterator<E> iterator() {
    return new Iterator<>() {

      private long index = 0;

      private long size = size();

      @Override
      public boolean hasNext() {
        return index < size;
      }

      @Override
      public E next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        return get(index++);
      }
    };
  }
}
