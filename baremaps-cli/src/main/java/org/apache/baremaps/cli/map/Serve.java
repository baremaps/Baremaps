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

package org.apache.baremaps.cli.map;

import static io.servicetalk.data.jackson.jersey.ServiceTalkJacksonSerializerFeature.newContextResolver;
import static org.apache.baremaps.utils.ObjectMapperUtils.objectMapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import io.servicetalk.http.netty.HttpServers;
import io.servicetalk.http.router.jersey.HttpJerseyRouterBuilder;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import org.apache.baremaps.cli.Options;
import org.apache.baremaps.config.ConfigReader;
import org.apache.baremaps.postgres.PostgresUtils;
import org.apache.baremaps.server.*;
import org.apache.baremaps.tilestore.TileCache;
import org.apache.baremaps.tilestore.TileStore;
import org.apache.baremaps.tilestore.postgres.PostgresTileStore;
import org.apache.baremaps.vectortile.style.Style;
import org.apache.baremaps.vectortile.tileset.Tileset;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "serve", description = "Start a tile server with caching capabilities.")
public class Serve implements Callable<Integer> {

  private static final Logger logger = LoggerFactory.getLogger(Serve.class);

  @Mixin
  private Options options;

  @Option(names = {"--cache"}, paramLabel = "CACHE", description = "The caffeine cache directive.")
  private String cache = "";

  @Option(names = {"--tileset"}, paramLabel = "TILESET", description = "The tileset file.",
      required = true)
  private Path tilesetPath;

  @Option(names = {"--style"}, paramLabel = "STYLE", description = "The style file.",
      required = true)
  private Path stylePath;

  @Option(names = {"--host"}, paramLabel = "HOST", description = "The host of the server.")
  private String host = "localhost";

  @Option(names = {"--port"}, paramLabel = "PORT", description = "The port of the server.")
  private int port = 9000;

  @Override
  public Integer call() throws Exception {
    var objectMapper = objectMapper();
    var configReader = new ConfigReader();
    var tileset = objectMapper.readValue(configReader.read(this.tilesetPath), Tileset.class);
    var caffeineSpec = CaffeineSpec.parse(cache);
    var datasource = PostgresUtils.dataSource(tileset.getDatabase());

    var tileStoreSupplierType = new TypeLiteral<Supplier<TileStore>>() {};
    var tileStore = new PostgresTileStore(datasource, tileset);
    var tileCache = new TileCache(tileStore, caffeineSpec);
    var tileStoreSupplier = (Supplier<TileStore>) () -> tileCache;

    var styleSupplierType = new TypeLiteral<Supplier<Style>>() {};
    var style = objectMapper.readValue(configReader.read(this.stylePath), Style.class);
    var styleSupplier = (Supplier<Style>) () -> style;

    var application =
        new ResourceConfig()
            .register(CorsFilter.class)
            .register(ServerResources.class)
            .register(StyleRessources.class)
            .register(TileResources.class)
            .register(ClassPathResources.class)
            .register(newContextResolver(objectMapper))
            .register(new AbstractBinder() {
              @Override
              protected void configure() {
                bind("assets").to(String.class).named("directory");
                bind("server.html").to(String.class).named("index");
                bind(tilesetPath).to(Path.class).named("tileset");
                bind(tileCache).to(TileStore.class);
                bind(objectMapper).to(ObjectMapper.class);
                bind(tileStoreSupplier).to(tileStoreSupplierType);
                bind(styleSupplier).to(styleSupplierType);
              }
            });

    var httpService = new HttpJerseyRouterBuilder().buildBlockingStreaming(application);
    var serverContext = HttpServers.forPort(port).listenBlockingStreamingAndAwait(httpService);

    logger.info("Listening on {}", serverContext.listenAddress());

    serverContext.awaitShutdown();
    return 0;
  }
}
