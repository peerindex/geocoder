package net.peerindex.geocoder;


import com.google.common.base.*;
import com.google.common.collect.*;
import com.google.common.io.CharStreams;

import java.io.*;
import java.util.*;

import static com.google.common.base.Preconditions.checkState;


/**
 * This class creates a location extractLocations from a given Gazetteer file
 * @author Enno Shioji (eshioji@gmail.com)
 */
class GeoDbIndexer {
    private final BufferedReader gazetteer;
    private final ImmutableMap<FeatureCodeCategory, Long> populationThreshold;
    private final ImmutableMap<String, Double> pclToActivityShare;

    private final Tokenizer tokenizer = new Tokenizer();

    /**
     *
     * @param gazetteer
     * @param populationThresholdSrc
     */
    GeoDbIndexer(BufferedReader gazetteer, BufferedReader populationThresholdSrc, BufferedReader pclToActivityShareSrc) throws IOException {
        this.gazetteer = gazetteer;
        this.populationThreshold = Utils.readPopulationTh(populationThresholdSrc);
        this.pclToActivityShare = Utils.readActivityShare(pclToActivityShareSrc);

        // Fail if our population threshold map doesn't have threshold for all {@link FeatureCodeCategory}
        checkState(
                Sets.difference(
                        populationThreshold.keySet(),
                        EnumSet.allOf(FeatureCodeCategory.class)
                ).size() == 0,
                "You must provide population threshold for all of " + ImmutableList.copyOf(FeatureCodeCategory.values())
                );

    }

    GeoDbIndexer(BufferedReader gazetteer, ImmutableMap<FeatureCodeCategory, Long> populationThreshold, ImmutableMap<String, Double> activityShare) throws IOException {
        this.gazetteer = gazetteer;
        this.populationThreshold = populationThreshold;
        this.pclToActivityShare = activityShare;

        // Fail if our population threshold map doesn't have threshold for all {@link FeatureCodeCategory}
        checkState(
                Sets.difference(
                        populationThreshold.keySet(),
                        EnumSet.allOf(FeatureCodeCategory.class)
                ).size() == 0,
                "You must provide population threshold for all of " + ImmutableList.copyOf(FeatureCodeCategory.values())
        );

    }


    /**
     * Takes path to Gazetter and population threshold configuration for each feature category to produce a map from location names to locations
     * @return A SetMultimap where key is the normalized name and values are the locations who share that name
     */
    Set<Location> extractLocations() {
        try {
            Set<Location> extracted = CharStreams.readLines(
                    gazetteer,
                    new ExtractLocations(populationThreshold)
            );

            // Raw population stats doesn't work great in the internet as-is. Calibrate for online activity
            calibrateWeight(extracted);

            return extracted;

        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    ImmutableMap<ImmutableList<String>, Set<Location>> index(Set<Location> locations) {
        // We will have at least locations.size number of entries
        Map<ImmutableList<String>, Set<Location>> index = new HashMap<>(locations.size());
        for (Location location : locations) {
            for (String name : location.getNames()) {
                ImmutableList<String> tokens = tokenizer.tokenize(name);
                put(index, tokens, location);
            }
        }
        return ImmutableMap.copyOf(index);
    }

    private void put(Map<ImmutableList<String>, Set<Location>> index, ImmutableList<String> key, Location location) {
        Set<Location> locations = index.get(key);
        if(locations == null){
            locations = new HashSet<>();
            index.put(key, locations);
        }
        locations.add(location);
    }




    private void calibrateWeight(Set<Location> precursor) {

        // Calculate (rough) world population
        Map<String, Double> pclCode_population = pclToPopulation(precursor);
        double worldPopulation = sum(pclCode_population.values());

        // Obtain online activity share per PCL (from an external source)
        Map<String, Double> pcl_onlineActivityShare = pclToActivityShare;

        // Obtain total population for which online activity calibration data is available
        double totalPopulationWithCalibData = totalPopulationWithCalibData(pclCode_population, pcl_onlineActivityShare.keySet());

        // Obtain total population for which online activity calibration isn't available
        double restPopulation = worldPopulation - totalPopulationWithCalibData;


        double totalShareForCalibPCLs = sum(pcl_onlineActivityShare.values());
        double totalShareForNoCalibPCLs = 1.0 - totalShareForCalibPCLs;


        for (Location location : precursor) {
            String country = location.getCodes().get(FeatureCodeCategory.PCL);
            double correctedShare;
            if (pcl_onlineActivityShare.containsKey(country)) {
                // A country with online activity share data
                double incountry = location.getPopulation() / pclCode_population.get(country);
                double country_share = pcl_onlineActivityShare.get(country);
                correctedShare = country_share * incountry;
            } else {
                // Location in a country without online activity share data
                if (!pclCode_population.keySet().contains(country)){
                    // This country is tiny, approximate with zero
                    location.setWeight(0.0);
                    continue;
                }

                double incountry = location.getPopulation() / pclCode_population.get(country);
                double country_share = (pclCode_population.get(country) / restPopulation) * totalShareForNoCalibPCLs;
                correctedShare = country_share * incountry;
            }
            location.setWeight(correctedShare);
        }
    }

    private static double totalPopulationWithCalibData(Map<String, Double> pclCode_population, Set<String> pcl_onlineActivityShare) {
        double totalPopulationWithCalibrationData = 0l;
        for (Map.Entry<String, Double> pop : pclCode_population.entrySet()) {
            if (pcl_onlineActivityShare.contains(pop.getKey())) {
                totalPopulationWithCalibrationData += pop.getValue();
            }
        }
        return totalPopulationWithCalibrationData;
    }


    private static Map<String, Double> pclToPopulation(Set<Location> precursor) {
        Map<String, Double> pclCode_population = new HashMap<String, Double>();
        for (Location locations : precursor) {
            if (locations.getFeatureCodeCategory() == FeatureCodeCategory.PCL) {
                pclCode_population.put(locations.getCodes().get(FeatureCodeCategory.PCL), locations.getPopulation().doubleValue());
            }
        }
        return pclCode_population;
    }

    private static double sum(Collection<Double> values) {
        double sum = 0;
        for (Double value : values) {
            sum += value;
        }
        return sum;
    }

}
