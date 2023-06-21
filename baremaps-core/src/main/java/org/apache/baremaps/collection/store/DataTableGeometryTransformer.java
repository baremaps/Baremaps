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

package org.apache.baremaps.collection.store;

import java.util.function.Function;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.util.GeometryTransformer;

/**
 * A transformer that applies a {@code GeometryTransformer} to the geometries of a
 * {@code DataTable}.
 */
public class DataTableGeometryTransformer implements Function<DataRow, DataRow> {

  private final DataTable dataTable;

  private final GeometryTransformer geometryTransformer;

  /**
   * Constructs a new table transformer.
   *
   * @param dataTable the table to transform
   * @param geometryTransformer the geometry transformer
   */
  public DataTableGeometryTransformer(DataTable dataTable,
      GeometryTransformer geometryTransformer) {
    this.dataTable = dataTable;
    this.geometryTransformer = geometryTransformer;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DataRow apply(DataRow dataRow) {
    var columns = dataTable.schema()
        .columns().stream()
        .filter(column -> column.type().isAssignableFrom(Geometry.class))
        .toList();
    for (DataColumn dataColumn : columns) {
      var name = dataColumn.name();
      var geometry = (Geometry) dataRow.get(name);
      if (geometry != null) {
        dataRow.set(name, geometryTransformer.transform(geometry));
      }
    }
    return dataRow;
  }
}
