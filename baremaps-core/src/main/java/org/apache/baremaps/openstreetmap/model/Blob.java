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

package org.apache.baremaps.openstreetmap.model;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.StringJoiner;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import org.apache.baremaps.osm.binary.Fileformat;
import org.apache.baremaps.osm.binary.Fileformat.BlobHeader;

/** Represents a raw blob of data in an OpenStreetMap dataset. */
public record Blob(BlobHeader header, byte[] rawData, int size) {

  /**
   * Returns the data.
   *
   * @return the data
   * @throws DataFormatException
   * @throws InvalidProtocolBufferException
   */
  public ByteString data() throws DataFormatException, InvalidProtocolBufferException {
    Fileformat.Blob blob = Fileformat.Blob.parseFrom(rawData);
    if (blob.hasRaw()) {
      return blob.getRaw();
    } else if (blob.hasZlibData()) {
      byte[] bytes = new byte[blob.getRawSize()];
      Inflater inflater = new Inflater();
      inflater.setInput(blob.getZlibData().toByteArray());
      inflater.inflate(bytes);
      inflater.end();
      return ByteString.copyFrom(bytes);
    } else {
      throw new DataFormatException("Unsupported toPrimitiveBlock format");
    }
  }
}
