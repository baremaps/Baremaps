package com.baremaps.storage.postgres;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.storage.WritableFeatureSet;
import org.apache.sis.storage.event.StoreEvent;
import org.apache.sis.storage.event.StoreListener;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureType;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.Metadata;
import org.opengis.util.GenericName;

public class PostgresTable implements WritableFeatureSet {

  private final FeatureType featureType;

  public PostgresTable(FeatureType featureType) {
    this.featureType = featureType;
  }

  @Override
  public void updateType(FeatureType newType) throws DataStoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void add(Iterator<? extends Feature> features) throws DataStoreException {

  }

  @Override
  public boolean removeIf(Predicate<? super Feature> filter) throws DataStoreException {
    return false;
  }

  @Override
  public void replaceIf(Predicate<? super Feature> filter, UnaryOperator<Feature> updater) throws DataStoreException {

  }

  @Override
  public FeatureType getType() throws DataStoreException {
    return null;
  }

  @Override
  public Stream<Feature> features(boolean parallel) throws DataStoreException {
    return null;
  }

  @Override
  public Optional<Envelope> getEnvelope() throws DataStoreException {
    return Optional.empty();
  }

  @Override
  public Optional<GenericName> getIdentifier() throws DataStoreException {
    return Optional.empty();
  }

  @Override
  public Metadata getMetadata() throws DataStoreException {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T extends StoreEvent> void addListener(Class<T> eventType, StoreListener<? super T> listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T extends StoreEvent> void removeListener(Class<T> eventType, StoreListener<? super T> listener) {
    throw new UnsupportedOperationException();
  }
}
