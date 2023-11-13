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

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.apache.baremaps.workflow.WorkflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecuteSqlScript implements Task {

  private static final Logger logger = LoggerFactory.getLogger(ExecuteSqlScript.class);

  private Object database;

  private Path file;

  public ExecuteSqlScript() {

  }

  public ExecuteSqlScript(Object database, Path file) {
    this.database = database;
    this.file = file;
  }

  public Object getDatabase() {
    return database;
  }

  public void setDatabase(Object database) {
    this.database = database;
  }

  public Path getFile() {
    return file;
  }

  public void setFile(Path file) {
    this.file = file;
  }

  @Override
  public void execute(WorkflowContext context) throws Exception {
    var script = Files.readString(file);
    var dataSource = context.getDataSource(database);
    try (var connection = dataSource.getConnection();
        var statement = connection.createStatement()) {
      statement.execute(script);
    } catch (SQLException e) {
      throw new WorkflowException(e);
    }
  }

}
