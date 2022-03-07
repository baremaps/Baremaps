/*
 * Copyright (C) 2020 The Baremaps Authors
 *
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

package com.baremaps.osm.pbf;

import static com.baremaps.stream.ConsumerUtils.consumeThenReturn;

import com.baremaps.osm.domain.Block;
import com.baremaps.osm.domain.Entity;
import com.baremaps.osm.function.BlockEntityConsumer;
import com.baremaps.osm.geometry.CreateGeometryConsumer;
import com.baremaps.osm.geometry.ReprojectEntityConsumer;
import com.baremaps.osm.store.DataStoreConsumer;
import com.baremaps.store.LongDataMap;
import com.baremaps.stream.StreamException;
import com.baremaps.stream.StreamUtils;
import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import org.locationtech.jts.geom.Coordinate;

/** A utility class for parsing an OpenStreetMap pbf file. */
public class OsmPbfParser {

  private int buffer = Runtime.getRuntime().availableProcessors();

  private boolean geometry = false;

  private int srid = 4326;

  private LongDataMap<Coordinate> coordinates;

  private LongDataMap<List<Long>> references;

  /**
   * Gets the number of blobs buffered by the parser to parallelize deserialization.
   *
   * @return the size of the buffer
   */
  public int buffer() {
    return buffer;
  }

  /**
   * Sets the number of blobs buffered by the parser to parallelize deserialization.
   *
   * @param buffer the size of the buffer
   * @return the parser
   */
  public OsmPbfParser buffer(int buffer) {
    this.buffer = buffer;
    return this;
  }

  /**
   * Gets the flag enabling the generation of geometries.
   *
   * @return the value of the flag
   */
  public boolean geometries() {
    return geometry;
  }

  /**
   * Sets the flag enabling the generation of geometries.
   *
   * @param geometries the value of the flag
   * @return the parser
   */
  public OsmPbfParser geometries(boolean geometries) {
    this.geometry = geometries;
    return this;
  }

  /**
   * Gets the projection of the geometries generated by this parser.
   *
   * @return the projection of the geometries
   */
  public int projection() {
    return srid;
  }

  /**
   * Sets the projection of the geometries generated by this parser.
   *
   * @param srid the projection of the geometries
   * @return the parser
   */
  public OsmPbfParser projection(int srid) {
    this.srid = srid;
    return this;
  }

  /**
   * Gets the map used to store coordinates for generating geometries.
   *
   * @return the map of coordinates
   */
  public LongDataMap<Coordinate> coordinates() {
    return coordinates;
  }

  /**
   * Sets the map used to store coordinates for generating geometries.
   *
   * @param coordinates the map of coordinates
   * @return the parser
   */
  public OsmPbfParser coordinates(LongDataMap<Coordinate> coordinates) {
    this.coordinates = coordinates;
    return this;
  }

  /**
   * Gets the map used to store references for generating geometries.
   *
   * @return the map of references
   */
  public LongDataMap<List<Long>> references() {
    return references;
  }

  /**
   * Sets the map used to store references for generating geometries.
   *
   * @param references the map of references
   * @return the parser
   */
  public OsmPbfParser references(LongDataMap<List<Long>> references) {
    this.references = references;
    return this;
  }

  /**
   * Creates an ordered stream of blocks.
   *
   * @param inputStream an osm pbf {@link InputStream}
   * @return a stream of blocks
   */
  public Stream<Block> blocks(InputStream inputStream) {
    Stream<Block> blocks =
        StreamUtils.bufferInSourceOrder(
            StreamUtils.stream(new BlobIterator(inputStream)),
            BlockReader::read,
            Runtime.getRuntime().availableProcessors());
    if (geometry) {
      Consumer<Block> cacheBlock = new DataStoreConsumer(coordinates, references);
      Consumer<Entity> createGeometry = new CreateGeometryConsumer(coordinates, references);
      if (srid != 4326) {
        Consumer<Entity> reprojectGeometry = new ReprojectEntityConsumer(4326, srid);
        createGeometry = createGeometry.andThen(reprojectGeometry);
      }
      Consumer<Block> prepareGeometries = new BlockEntityConsumer(createGeometry);
      Function<Block, Block> prepareBlock =
          consumeThenReturn(cacheBlock.andThen(prepareGeometries));
      blocks = blocks.map(prepareBlock);
    }
    return blocks;
  }

  /**
   * Creates an ordered stream of entities.
   *
   * @param inputStream an osm pbf {@link InputStream}
   * @return a stream of blocks
   */
  public Stream<Entity> entities(InputStream inputStream) {
    return blocks(inputStream)
        .flatMap(
            block -> {
              try {
                Stream.Builder<Entity> entities = Stream.builder();
                block.visit(new BlockEntityConsumer(entities::add));
                return entities.build();
              } catch (Exception e) {
                throw new StreamException(e);
              }
            });
  }
}
