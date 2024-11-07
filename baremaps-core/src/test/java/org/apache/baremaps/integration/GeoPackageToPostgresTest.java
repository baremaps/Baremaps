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

package org.apache.baremaps.integration;

import org.apache.baremaps.geopackage.store.GeoPackageDataStore;
import org.apache.baremaps.postgres.store.PostgresDataStore;
import org.apache.baremaps.testing.PostgresContainerTest;
import org.apache.baremaps.testing.TestFiles;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class GeoPackageToPostgresTest extends PostgresContainerTest {

  @Test
  @Tag("integration")
  void copyGeoPackageToPostgres() {
    // Open the GeoPackage
    var file = TestFiles.resolve("baremaps-testing/data/samples/countries.gpkg");
    var geoPackageSchema = new GeoPackageDataStore(file);
    var geoPackageTable = geoPackageSchema.get("countries");

    // Copy the table to Postgres
    var postgresStore = new PostgresDataStore(dataSource());
    postgresStore.add(geoPackageTable);

    // Check the table in Postgres
    var postgresTable = postgresStore.get("countries");
    Assertions.assertEquals("countries", postgresTable.schema().name());
    Assertions.assertEquals(4, postgresTable.schema().columns().size());
    Assertions.assertEquals(179l, postgresTable.size());
    Assertions.assertEquals(179l, postgresTable.stream().count());
  }
}
