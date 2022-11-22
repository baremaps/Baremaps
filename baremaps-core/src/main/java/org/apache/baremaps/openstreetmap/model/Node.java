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

package org.apache.baremaps.openstreetmap.model;



import java.util.Map;
import org.locationtech.jts.geom.Geometry;

/** Represents a node element in an OpenStreetMap dataset. */
public final class Node extends Element {

  private final double lon;

  private final double lat;

  /**
   * Constructs an OpenStreetMap {@code Node} with the specified parameters.
   *
   * @param id the id
   * @param info the information
   * @param tags the tags
   * @param lon the longitude
   * @param lat the latitude
   */
  public Node(long id, Info info, Map<String, String> tags, double lon, double lat) {
    super(id, info, tags);
    this.lon = lon;
    this.lat = lat;
  }

  /**
   * Constructs an OpenStreetMap {@code Node} with the specified parameters.
   *
   * @param id the id
   * @param info the information
   * @param tags the tags
   * @param lon the longitude
   * @param lat the latitude
   * @param geometry the geometry
   */
  public Node(long id, Info info, Map<String, String> tags, double lon, double lat,
      Geometry geometry) {
    super(id, info, tags, geometry);
    this.lon = lon;
    this.lat = lat;
  }

  /**
   * Returns the longitude.
   *
   * @return the longitude
   */
  public double lon() {
    return lon;
  }

  /**
   * Returns the latitude.
   *
   * @return the latitude
   */
  public double lat() {
    return lat;
  }
}
