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

import java.nio.file.Path;
import org.apache.baremaps.database.schema.DataTableAdapter;
import org.apache.baremaps.database.schema.DataTableGeometryTransformer;
import org.apache.baremaps.storage.postgres.PostgresDataSchema;
import org.apache.baremaps.storage.shapefile.ShapefileDataTable;
import org.apache.baremaps.utils.ProjectionTransformer;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.apache.baremaps.workflow.WorkflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportShapefile implements Task {

  private static final Logger logger = LoggerFactory.getLogger(ImportShapefile.class);

  private Path file;
  private Object database;
  private Integer sourceSRID;
  private Integer targetSRID;

  public ImportShapefile() {

  }

  public ImportShapefile(Path file, Object database, Integer sourceSRID, Integer targetSRID) {
    this.file = file;
    this.database = database;
    this.sourceSRID = sourceSRID;
    this.targetSRID = targetSRID;
  }

  public Path getFile() {
    return file;
  }

  public void setFile(Path file) {
    this.file = file;
  }

  public Object getDatabase() {
    return database;
  }

  public void setDatabase(Object database) {
    this.database = database;
  }

  public Integer getSourceSRID() {
    return sourceSRID;
  }

  public void setSourceSRID(Integer sourceSRID) {
    this.sourceSRID = sourceSRID;
  }

  public Integer getTargetSRID() {
    return targetSRID;
  }

  public void setTargetSRID(Integer targetSRID) {
    this.targetSRID = targetSRID;
  }

  @Override
  public void execute(WorkflowContext context) throws Exception {
    var path = file.toAbsolutePath();
    try {
      var shapefileDataTable = new ShapefileDataTable(path);
      var dataSource = context.getDataSource(database);
      var postgresDataStore = new PostgresDataSchema(dataSource);
      var rowTransformer = new DataTableGeometryTransformer(shapefileDataTable,
          new ProjectionTransformer(sourceSRID, targetSRID));
      var transformedDataTable = new DataTableAdapter(shapefileDataTable, rowTransformer);
      postgresDataStore.add(transformedDataTable);
    } catch (Exception e) {
      throw new WorkflowException(e);
    }
  }
}
