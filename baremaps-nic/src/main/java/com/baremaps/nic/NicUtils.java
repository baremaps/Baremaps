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

package com.baremaps.nic;

import java.io.InputStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Utils for RIPE datas */
public class NicUtils {

  private static final Logger logger = LogManager.getLogger(NicUtils.class);

  private NicUtils() {}

  /**
   * Convert an input stream into a NicObject Stream
   *
   * @param inputStream the stream of text input
   * @return Stream of Nic Object
   */
  public static Stream<NicObject> parse(InputStream inputStream) {
    return StreamSupport.stream(new NicSpliterator(inputStream), false);
  }
}
