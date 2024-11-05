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

package org.apache.baremaps.data.storage;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link DataSchema} defines the structure of a table.
 */
public record DataSchemaImpl(String name, List<DataColumn> columns) implements DataSchema {

  /**
   * Constructs a schema with the specified name and columns.
   *
   * @param name the name of the schema
   * @param columns the columns of the schema
   */
  public DataSchemaImpl {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String name() {
    return name;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<DataColumn> columns() {
    return columns;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DataRow createRow() {
    var values = new ArrayList<>(columns.size());
    for (int i = 0; i < columns.size(); i++) {
      values.add(null);
    }
    return new DataRowImpl(this, values);
  }
}
