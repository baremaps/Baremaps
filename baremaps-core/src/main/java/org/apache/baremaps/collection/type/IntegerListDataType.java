/*
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

package org.apache.baremaps.collection.type;



import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/** A {@link DataType} for reading and writing lists of integers in {@link ByteBuffer}s. */
public class IntegerListDataType implements DataType<List<Integer>> {

  /** {@inheritDoc} */
  @Override
  public int size(final List<Integer> values) {
    return Integer.BYTES + values.size() * Integer.BYTES;
  }

  @Override
  public int size(final ByteBuffer buffer, final int position) {
    return buffer.getInt(position);
  }

  /** {@inheritDoc} */
  @Override
  public void write(final ByteBuffer buffer, final int position, final List<Integer> values) {
    buffer.putInt(position, size(values));
    var p = position + Integer.BYTES;
    for (Integer value : values) {
      buffer.putInt(p, value);
      p += Integer.BYTES;
    }
  }

  /** {@inheritDoc} */
  @Override
  public List<Integer> read(final ByteBuffer buffer, final int position) {
    var size = size(buffer, position);
    var list = new ArrayList<Integer>();
    for (var p = position + Integer.BYTES; p < position + size; p += Integer.BYTES) {
      list.add(buffer.getInt(p));
    }
    return list;
  }
}
