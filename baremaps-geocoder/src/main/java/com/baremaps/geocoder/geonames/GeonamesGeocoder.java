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

package com.baremaps.geocoder.geonames;

import com.baremaps.geocoder.Geocoder;
import com.baremaps.geocoder.IsoCountriesUtils;
import com.baremaps.geocoder.Request;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.Query;

public class GeonamesGeocoder extends Geocoder {

  private final URI data;

  public GeonamesGeocoder(Path index, URI data) throws IOException {
    super(index);
    this.data = data;
  }

  @Override
  protected Analyzer analyzer() {
    return new StandardAnalyzer();
  }

  @Override
  protected Stream<Document> documents() throws IOException {
    CsvMapper mapper = new CsvMapper();
    CsvSchema schema =
        CsvSchema.builder()
            .addColumn("geonameid")
            .addColumn("name")
            .addColumn("asciiname")
            .addColumn("alternatenames")
            .addColumn("latitude")
            .addColumn("longitude")
            .addColumn("featureClass")
            .addColumn("featureCode")
            .addColumn("countryCode")
            .addColumn("cc2")
            .addColumn("admin1Code")
            .addColumn("admin2Code")
            .addColumn("admin3Code")
            .addColumn("admin4Code")
            .addColumn("population")
            .addColumn("elevation")
            .addColumn("dem")
            .addColumn("timezone")
            .addColumn("modificationDate")
            .build()
            .withColumnSeparator('\t');
    MappingIterator<GeonamesRecord> it =
        mapper.readerFor(GeonamesRecord.class).with(schema).readValues(data.toURL().openStream());
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, 0), false)
        .map(
            record -> {
              Document document = new Document();
              document.add(new Field("name", record.getName(), TextField.TYPE_STORED));
              document.add(
                  new Field(
                      "country",
                      IsoCountriesUtils.getCountry(record.getCountryCode()),
                      StringField.TYPE_STORED));
              document.add(
                  new Field("countryCode", record.getCountryCode(), StringField.TYPE_STORED));
              document.add(new StoredField("longitude", record.getLongitude()));
              document.add(new StoredField("latitude", record.getLatitude()));
              return document;
            });
  }

  @Override
  protected Query query(Analyzer analyzer, Request request) throws ParseException {
    BooleanQuery.Builder builder = new Builder();
    builder.add(new QueryParser("name", analyzer).parse(request.query()), Occur.SHOULD);
    builder.add(new QueryParser("country", analyzer).parse(request.query()), Occur.SHOULD);
    builder.add(new QueryParser("countryCode", analyzer).parse(request.query()), Occur.SHOULD);
    return builder.build();
  }
}
