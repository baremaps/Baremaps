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

/** Representation of Nic Attribute. */
public class NicAttribute {

  private String name;
  private String value;

  /**
   * Constructor.
   *
   * @param name - attribute name
   * @param value - attribute value
   */
  public NicAttribute(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public NicAttribute() {}

  /**
   * Getter for name property.
   *
   * @return Attribute name
   */
  public String name() {
    return name;
  }

  /**
   * Getter for value property.
   *
   * @return Attribute value
   */
  public String value() {
    return value;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return name + ": " + value;
  }
}
