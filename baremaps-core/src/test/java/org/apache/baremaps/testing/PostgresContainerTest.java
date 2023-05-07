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

package org.apache.baremaps.testing;



import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class PostgresContainerTest {

  private PostgreSQLContainer container;

  private DataSource dataSource;

  @BeforeEach
  public void startContainer() throws SQLException, IOException {
    // start the container
    var postgis =
        DockerImageName.parse("ghcr.io/baosystems/postgis:14-3.3")
            .asCompatibleSubstituteFor("postgres");
    container = new PostgreSQLContainer(postgis);
    container.start();

    // set the datasource
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(jdbcUrl());
    dataSource = new HikariDataSource(config);
  }

  @AfterEach
  public void stopContainer() {
    container.stop();
  }

  public String jdbcUrl() {
    return String.format("%s&user=%s&password=%s&currentSchema=%s", container.getJdbcUrl(),
        container.getUsername(), container.getPassword(), "public");
  }

  public DataSource dataSource() {
    return dataSource;
  }
}
