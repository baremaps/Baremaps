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

package org.apache.baremaps.flatgeobuf.generated;

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class Column extends Table {
  public static void ValidateVersion() {
    Constants.FLATBUFFERS_24_3_25();
  }

  public static Column getRootAsColumn(ByteBuffer _bb) {
    return getRootAsColumn(_bb, new Column());
  }

  public static Column getRootAsColumn(ByteBuffer _bb, Column obj) {
    _bb.order(ByteOrder.LITTLE_ENDIAN);
    return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb));
  }

  public void __init(int _i, ByteBuffer _bb) {
    __reset(_i, _bb);
  }

  public Column __assign(int _i, ByteBuffer _bb) {
    __init(_i, _bb);
    return this;
  }

  public String name() {
    int o = __offset(4);
    return o != 0 ? __string(o + bb_pos) : null;
  }

  public ByteBuffer nameAsByteBuffer() {
    return __vector_as_bytebuffer(4, 1);
  }

  public ByteBuffer nameInByteBuffer(ByteBuffer _bb) {
    return __vector_in_bytebuffer(_bb, 4, 1);
  }

  public int type() {
    int o = __offset(6);
    return o != 0 ? bb.get(o + bb_pos) & 0xFF : 0;
  }

  public String title() {
    int o = __offset(8);
    return o != 0 ? __string(o + bb_pos) : null;
  }

  public ByteBuffer titleAsByteBuffer() {
    return __vector_as_bytebuffer(8, 1);
  }

  public ByteBuffer titleInByteBuffer(ByteBuffer _bb) {
    return __vector_in_bytebuffer(_bb, 8, 1);
  }

  public String description() {
    int o = __offset(10);
    return o != 0 ? __string(o + bb_pos) : null;
  }

  public ByteBuffer descriptionAsByteBuffer() {
    return __vector_as_bytebuffer(10, 1);
  }

  public ByteBuffer descriptionInByteBuffer(ByteBuffer _bb) {
    return __vector_in_bytebuffer(_bb, 10, 1);
  }

  public int width() {
    int o = __offset(12);
    return o != 0 ? bb.getInt(o + bb_pos) : -1;
  }

  public int precision() {
    int o = __offset(14);
    return o != 0 ? bb.getInt(o + bb_pos) : -1;
  }

  public int scale() {
    int o = __offset(16);
    return o != 0 ? bb.getInt(o + bb_pos) : -1;
  }

  public boolean nullable() {
    int o = __offset(18);
    return o != 0 ? 0 != bb.get(o + bb_pos) : true;
  }

  public boolean unique() {
    int o = __offset(20);
    return o != 0 ? 0 != bb.get(o + bb_pos) : false;
  }

  public boolean primaryKey() {
    int o = __offset(22);
    return o != 0 ? 0 != bb.get(o + bb_pos) : false;
  }

  public String metadata() {
    int o = __offset(24);
    return o != 0 ? __string(o + bb_pos) : null;
  }

  public ByteBuffer metadataAsByteBuffer() {
    return __vector_as_bytebuffer(24, 1);
  }

  public ByteBuffer metadataInByteBuffer(ByteBuffer _bb) {
    return __vector_in_bytebuffer(_bb, 24, 1);
  }

  public static int createColumn(FlatBufferBuilder builder,
      int nameOffset,
      int type,
      int titleOffset,
      int descriptionOffset,
      int width,
      int precision,
      int scale,
      boolean nullable,
      boolean unique,
      boolean primaryKey,
      int metadataOffset) {
    builder.startTable(11);
    Column.addMetadata(builder, metadataOffset);
    Column.addScale(builder, scale);
    Column.addPrecision(builder, precision);
    Column.addWidth(builder, width);
    Column.addDescription(builder, descriptionOffset);
    Column.addTitle(builder, titleOffset);
    Column.addName(builder, nameOffset);
    Column.addPrimaryKey(builder, primaryKey);
    Column.addUnique(builder, unique);
    Column.addNullable(builder, nullable);
    Column.addType(builder, type);
    return Column.endColumn(builder);
  }

  public static void startColumn(FlatBufferBuilder builder) {
    builder.startTable(11);
  }

  public static void addName(FlatBufferBuilder builder, int nameOffset) {
    builder.addOffset(0, nameOffset, 0);
  }

  public static void addType(FlatBufferBuilder builder, int type) {
    builder.addByte(1, (byte) type, (byte) 0);
  }

  public static void addTitle(FlatBufferBuilder builder, int titleOffset) {
    builder.addOffset(2, titleOffset, 0);
  }

  public static void addDescription(FlatBufferBuilder builder, int descriptionOffset) {
    builder.addOffset(3, descriptionOffset, 0);
  }

  public static void addWidth(FlatBufferBuilder builder, int width) {
    builder.addInt(4, width, -1);
  }

  public static void addPrecision(FlatBufferBuilder builder, int precision) {
    builder.addInt(5, precision, -1);
  }

  public static void addScale(FlatBufferBuilder builder, int scale) {
    builder.addInt(6, scale, -1);
  }

  public static void addNullable(FlatBufferBuilder builder, boolean nullable) {
    builder.addBoolean(7, nullable, true);
  }

  public static void addUnique(FlatBufferBuilder builder, boolean unique) {
    builder.addBoolean(8, unique, false);
  }

  public static void addPrimaryKey(FlatBufferBuilder builder, boolean primaryKey) {
    builder.addBoolean(9, primaryKey, false);
  }

  public static void addMetadata(FlatBufferBuilder builder, int metadataOffset) {
    builder.addOffset(10, metadataOffset, 0);
  }

  public static int endColumn(FlatBufferBuilder builder) {
    int o = builder.endTable();
    builder.required(o, 4); // name
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) {
      __reset(_vector, _element_size, _bb);
      return this;
    }

    public Column get(int j) {
      return get(new Column(), j);
    }

    public Column get(Column obj, int j) {
      return obj.__assign(__indirect(__element(j), bb), bb);
    }
  }
}
