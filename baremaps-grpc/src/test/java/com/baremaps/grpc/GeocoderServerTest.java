package com.baremaps.grpc;

import com.baremaps.geocoder.Geocoder;
import com.baremaps.geocoder.geonames.GeonamesGeocoder;
import static org.junit.Assert.assertEquals;
import com.baremaps.grpc.service.GeocoderServiceImpl;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import java.net.URI;
import java.nio.file.Path;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Test;

@RunWith(JUnit4.class)
public class GeocoderServerTest {
  private Path indexPath;
  private URI geonamesData;
  private Geocoder geocoder;
  /**
   * This rule manages automatic graceful shutdown for the registered servers and channels at the
   * end of test.
   */
  @Rule
  public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

  /**
   * To test the server, make calls with a real stub using the in-process channel, and verify
   * behaviors or state changes from the client side.
   */
  @Test
  public void geocoderServiceImpl_replyMessage() throws Exception {
    // Generate a unique in-process server name.
    String serverName = InProcessServerBuilder.generateName();

    // Create a server, add service, start, and register for automatic graceful shutdown.
    grpcCleanup.register(InProcessServerBuilder
        .forName(serverName).directExecutor().addService(new GeocoderServiceImpl(geocoder)).build().start());

    GeocoderServiceGrpc.GeocoderServiceBlockingStub blockingStub = GeocoderServiceGrpc.newBlockingStub(
        // Create a client channel and register for automatic graceful shutdown.
        grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build()));


    SearchReply reply =
        blockingStub.search(SearchRequest.newBuilder().setQuery("Lausanne").setLimit(10).build());

    assertEquals(false, reply.getGeonamesResultsList().isEmpty());
  }
}
