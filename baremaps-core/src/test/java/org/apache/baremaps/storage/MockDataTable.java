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

package org.apache.baremaps.storage;

import static org.apache.baremaps.openstreetmap.repository.Constants.GEOMETRY_FACTORY;

import java.util.Iterator;
import java.util.List;
import org.apache.baremaps.collection.store.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

public class MockDataTable extends AbstractDataTable {

  private final DataSchema schema;

  private final List<DataRow> rows;

  public MockDataTable() {
    this.schema = new DataSchemaImpl("mock", List.of(
        new DataColumnImpl("string", String.class),
        new DataColumnImpl("integer", Integer.class),
        new DataColumnImpl("double", Double.class),
        new DataColumnImpl("float", Float.class),
        new DataColumnImpl("geometry", Geometry.class)));
    this.rows = List.of(
        new DataRowImpl(schema,
            List.of("string", 1, 1.0, 1.0f, GEOMETRY_FACTORY.createPoint(new Coordinate(1, 1)))),
        new DataRowImpl(schema,
            List.of("string", 2, 2.0, 2.0f, GEOMETRY_FACTORY.createPoint(new Coordinate(2, 2)))),
        new DataRowImpl(schema,
            List.of("string", 3, 3.0, 3.0f, GEOMETRY_FACTORY.createPoint(new Coordinate(3, 3)))),
        new DataRowImpl(schema,
            List.of("string", 4, 4.0, 4.0f, GEOMETRY_FACTORY.createPoint(new Coordinate(4, 4)))),
        new DataRowImpl(schema,
            List.of("string", 5, 5.0, 5.0f, GEOMETRY_FACTORY.createPoint(new Coordinate(5, 5)))));
  }

  @Override
  public Iterator<DataRow> iterator() {
    return rows.iterator();
  }

  @Override
  public long sizeAsLong() {
    return rows.size();
  }

  @Override
  public DataSchema schema() {
    return schema;
  }
}
