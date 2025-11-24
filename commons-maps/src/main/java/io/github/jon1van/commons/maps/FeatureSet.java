package io.github.jon1van.commons.maps;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/// The static Builder is the reason for writing this wrapper.
public class FeatureSet implements Iterable<MapFeature> {

    private final List<MapFeature> features;

    public FeatureSet(Collection<MapFeature> features) {
        this.features = newArrayList(features);
    }

    /// @return An empty FeatureList.
    public static FeatureSet noMapFeatures() {
        return new FeatureSet(newArrayList());
    }

    @Override
    public Iterator<MapFeature> iterator() {
        return features.iterator();
    }
}
