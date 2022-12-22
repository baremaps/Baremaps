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

package org.apache.baremaps.openstreetmap.pbf;

import static org.apache.baremaps.stream.ConsumerUtils.consumeThenReturn;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;
import org.apache.baremaps.collection.LongDataMap;
import org.apache.baremaps.openstreetmap.DataReader;
import org.apache.baremaps.openstreetmap.function.*;
import org.apache.baremaps.openstreetmap.model.Block;
import org.apache.baremaps.stream.StreamUtils;
import org.locationtech.jts.geom.Coordinate;

/** A utility class for reading an OpenStreetMap pbf file. */
public class PbfBlockReader implements DataReader<Block> {

  private int buffer = Runtime.getRuntime().availableProcessors();

  private boolean geometry = false;

  private int srid = 4326;

  private LongDataMap<Coordinate> coordinateMap;

  private LongDataMap<List<Long>> referenceMap;

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
  public PbfBlockReader buffer(int buffer) {
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
  public PbfBlockReader geometries(boolean geometries) {
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
  public PbfBlockReader projection(int srid) {
    this.srid = srid;
    return this;
  }

  /**
   * Gets the map used to store coordinates for generating geometries.
   *
   * @return the map of coordinates
   */
  public LongDataMap<Coordinate> coordinateMap() {
    return coordinateMap;
  }

  /**
   * Sets the map used to store coordinates for generating geometries.
   *
   * @param coordinateMap the map of coordinates
   * @return the parser
   */
  public PbfBlockReader coordinateMap(LongDataMap<Coordinate> coordinateMap) {
    this.coordinateMap = coordinateMap;
    return this;
  }

  /**
   * Gets the map used to store references for generating geometries.
   *
   * @return the map of references
   */
  public LongDataMap<List<Long>> referenceMap() {
    return referenceMap;
  }

  /**
   * Sets the map used to store references for generating geometries.
   *
   * @param referenceMap the map of references
   * @return the parser
   */
  public PbfBlockReader referenceMap(LongDataMap<List<Long>> referenceMap) {
    this.referenceMap = referenceMap;
    return this;
  }

  /**
   * Creates an ordered stream of blocks.
   *
   * @param inputStream an osm pbf {@link InputStream}
   * @return a stream of blocks
   */
  public Stream<Block> read(InputStream inputStream) {
    var blocks = StreamUtils.bufferInSourceOrder(StreamUtils.stream(new BlobIterator(inputStream)),
        new BlobToBlockMapper(), Runtime.getRuntime().availableProcessors());
    if (geometry) {
      // Initialize and chain the entity handlers
      var coordinateMapBuilder = new CoordinateMapBuilder(coordinateMap);
      var referenceMapBuilder = new ReferenceMapBuilder(referenceMap);
      var entityGeometryBuilder = new EntityGeometryBuilder(coordinateMap, referenceMap);
      var entityProjectionTransformer = new EntityProjectionTransformer(4326, srid);
      var entityHandler = coordinateMapBuilder
          .andThen(referenceMapBuilder)
          .andThen(entityGeometryBuilder)
          .andThen(entityProjectionTransformer);

      // Initialize the block mapper
      var blockMapper = consumeThenReturn(new BlockEntitiesHandler(entityHandler));

      blocks = blocks.map(blockMapper);
    }
    return blocks;
  }
}
