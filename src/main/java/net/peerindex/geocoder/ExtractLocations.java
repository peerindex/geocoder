package net.peerindex.geocoder;

import com.google.common.collect.*;

import java.util.HashSet;
import java.util.Set;

/**
* @author Enno Shioji (eshioji@gmail.com)
*/
class ExtractLocations extends GazetteerParser<Set<Location>> {
    private final ImmutableMap<FeatureCodeCategory, Long> populationTh;
    private final Set<Location> ret = new HashSet<>();

    ExtractLocations(ImmutableMap<FeatureCodeCategory, Long> populationTh) {
        this.populationTh = populationTh;
    }


    @Override
    protected boolean doProcessLine(String line, Location location) {
        long threshold = populationTh.get(location.getFeatureCodeCategory());

        if(location.getPopulation() < threshold){
            // This location is too small to extractLocations
            return true;
        }

        ret.add(location);
        return true;

    }

    @Override
    public Set<Location> getResult() {
        return ret;
    }
}
