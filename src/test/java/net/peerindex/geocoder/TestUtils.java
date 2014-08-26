package net.peerindex.geocoder;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.*;
import java.util.Set;

/**
 * @author Enno Shioji (eshioji@gmail.com)
 */
public enum TestUtils {
    ;

    public static ImmutableMap<ImmutableList<String>, Set<Location>> testIndex(String gazetteerName, ImmutableMap<String,Double> onlineActivityShare, ImmutableMap<FeatureCodeCategory, Long> populationThreshold) throws IOException {
        BufferedReader testGazetteer = new BufferedReader(new InputStreamReader(GeocoderIndexerTest.class.getResourceAsStream(gazetteerName), Charsets.UTF_8));
        GeoDbIndexer indexer = new GeoDbIndexer(testGazetteer, populationThreshold, onlineActivityShare);
        Set<Location> locations = indexer.extractLocations();
        return indexer.index(locations);
    }


    public static Set<Location> testLocations(String gazetteerName, ImmutableMap<String,Double> onlineActivityShare, ImmutableMap<FeatureCodeCategory, Long> populationThreshold) throws IOException {
        BufferedReader testGazetteer = new BufferedReader(new InputStreamReader(GeocoderIndexerTest.class.getResourceAsStream(gazetteerName), Charsets.UTF_8));
        GeoDbIndexer indexer = new GeoDbIndexer(testGazetteer, populationThreshold, onlineActivityShare);
        return indexer.extractLocations();
    }


    public static ImmutableMap<ImmutableList<String>, Set<Location>> testIndex(String gazetteerName) throws IOException {
        return testIndex(gazetteerName, testActivityShare(), testPopulationThreshold());
    }


    public static Set<Location> testLocations(String gazetteerName) throws IOException {
        return testLocations(gazetteerName, testActivityShare(), testPopulationThreshold());
    }

    private static ImmutableMap<String, Double> testActivityShare() {
        return ImmutableMap.of("US", 0.2, "GB", 0.1);
    }

    public static ImmutableMap<FeatureCodeCategory, Long> testPopulationThreshold(){
        ImmutableMap.Builder<FeatureCodeCategory, Long> b = ImmutableMap.builder();
        b.put(FeatureCodeCategory.PCL, 500000l);
        b.put(FeatureCodeCategory.ADM1, 30000l);
        b.put(FeatureCodeCategory.ADM2, 1000l);
        b.put(FeatureCodeCategory.ADM3, 1000l);
        b.put(FeatureCodeCategory.ADM4, 1000l);
        b.put(FeatureCodeCategory.SUBADM, 1000l);
        return b.build();
    }

    public static ImmutableMap<FeatureCodeCategory, Long> populationThreshold(String populationThresholdFile) throws IOException {
        try(BufferedReader br = new BufferedReader(new InputStreamReader(TestUtils.class.getResourceAsStream(populationThresholdFile), Charsets.UTF_8))){
            return Utils.readPopulationTh(br);
        }
    }
}
