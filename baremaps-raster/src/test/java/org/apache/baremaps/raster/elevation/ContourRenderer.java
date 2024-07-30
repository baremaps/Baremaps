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

package org.apache.baremaps.raster.elevation;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

public class ContourRenderer {

  public static void main(String[] args) throws IOException {
    // Load the image
    var path = Path.of("")
        .toAbsolutePath()
        .resolveSibling("baremaps/baremaps-raster/src/test/resources/fuji.png")
        .toAbsolutePath().toFile();
    var image = ImageIO.read(path);

    // Downscale the image by a factor of 16
    image = resizeImage(image, 32, 32);

    // Convert the image to a grid
    double[] grid = ElevationUtils.imageToGrid(image);

    List<Geometry> contour =
        new ContourTracer(grid, image.getWidth(), image.getHeight(), true, true)
            .traceContours(0, 9000, 100);

    // Scale the image back to its original size
    image = resizeImage(image, image.getWidth() * 16, image.getHeight() * 16);

    // Scale the contour back to its original size
    contour = contour.stream()
        .map(polygon -> {
          var coordinates = Stream.of(polygon.getCoordinates())
              .map(c -> new Coordinate(c.getX() * 16, c.getY() * 16))
              .toArray(Coordinate[]::new);
          return (Geometry) new GeometryFactory().createPolygon(coordinates);
        })
        .toList();

    // Smooth the contour with the Chaikin algorithm
    contour = contour.stream()
        .map(polygon -> {
          var coordinates =
              new ChaikinSmoother(polygon.getCoordinates(), 0, 0, 512, 512).smooth(2, 0.25);
          return (Geometry) new GeometryFactory().createPolygon(coordinates);
        })
        .toList();

    // Create a frame to display the contours
    JFrame frame = new JFrame("Contour Lines");
    frame.setSize(image.getWidth() + 20, image.getHeight() + 20);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.add(new ContourCanvas(image, contour));
    frame.setVisible(true);
  }

  private static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth,
      int targetHeight) {
    Image resultingImage =
        originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_DEFAULT);
    BufferedImage outputImage =
        new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
    outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
    return outputImage;
  }

  // Custom Canvas to draw the contours
  static class ContourCanvas extends Canvas {

    Image image;

    List<Geometry> contour;

    public ContourCanvas(Image image, List<Geometry> contour) {
      this.image = image;
      this.contour = contour;
    }

    @Override
    public void paint(Graphics g) {

      // Draw the image
      g.drawImage(image, 10, 10, null);

      g.setColor(Color.RED);
      for (Geometry contour : contour) {
        List<Point> points = Stream.of(contour.getCoordinates())
            .map(p -> new Point((int) p.getX() + 10, (int) p.getY() + 10))
            .toList();
        for (int i = 0; i < points.size() - 1; i++) {
          Point p1 = points.get(i);
          Point p2 = points.get(i + 1);
          g.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
      }
    }
  }
}
