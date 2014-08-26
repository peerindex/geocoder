package net.peerindex.geocoder;

import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import com.google.common.io.LineProcessor;
import net.peerindex.geocoder.FeatureCodeCategory;
import net.peerindex.geocoder.Location;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Enno Shioji (eshioji@gmail.com)
 */
abstract class GazetteerParser<T> implements LineProcessor<T> {
    private final Splitter ON_COMMA = Splitter.on(',').omitEmptyStrings().trimResults();
    private final Splitter ON_TAB = Splitter.on('\t');


    /**
     * Parses line from the Geonames download
     * @param read
     * @return
     */
    private Location parse(String read) {
        List<String> rawColumns = ImmutableList.copyOf(ON_TAB.split(read));
        int geonameid = Integer.valueOf(rawColumns.get(0));
        String default_name = rawColumns.get(1);
        String altNameCompressed = rawColumns.get(3);
        Double lat = Double.valueOf(rawColumns.get(4));
        Double lng = Double.valueOf(rawColumns.get(5));
        Set<String> alternateNames = ImmutableSet.copyOf(ON_COMMA.split(altNameCompressed));
        String featureCode = rawColumns.get(7);
        String countryCode = rawColumns.get(8);
        String adm1Code = Strings.emptyToNull(rawColumns.get(10));
        String adm2Code = Strings.emptyToNull(rawColumns.get(11));
        String adm3Code = Strings.emptyToNull(rawColumns.get(12));
        String adm4Code = Strings.emptyToNull(rawColumns.get(13));
        Long population = Long.valueOf(rawColumns.get(14));

        if (Strings.isNullOrEmpty(featureCode) || Strings.isNullOrEmpty(countryCode)) {
            // Invalid entry, skip
            return null;
        }

        FeatureCodeCategory featureCodeCategory = FeatureCodeCategory.belongsTo(featureCode);
        if(featureCodeCategory == null){
            // Not for indexing
            return null;
        }


        Location location = new Location();
        location.setGeonameId(geonameid);
        location.setDefaultName(default_name);
        location.setLat(lat);
        location.setLng(lng);
        location.addCode(FeatureCodeCategory.PCL, countryCode);
        putIfPresent(location, FeatureCodeCategory.ADM1, adm1Code);
        putIfPresent(location, FeatureCodeCategory.ADM2, adm2Code);
        putIfPresent(location, FeatureCodeCategory.ADM3, adm3Code);
        putIfPresent(location, FeatureCodeCategory.ADM4, adm4Code);
        location.setPopulation(population);
        location.setFeatureCodeCategory(featureCodeCategory);
        location.setFeatureCode(featureCode);
        location.setNames(getNames(alternateNames, default_name, location.getCodes().get(featureCodeCategory)));

        return location;
    }

    private void putIfPresent(Location location, FeatureCodeCategory category, String code) {
        if(!Strings.isNullOrEmpty(code)){
            location.addCode(category, code);
        }
    }


    private static Iterable<String> noEmpty(Sets.SetView<String> union) {
        return Iterables.filter(union, new Predicate<String>() {
            @Override
            public boolean apply(String input) {
                return input != null && input.length() > 1;
            }
        });

    }


    Set<String> getNames(Set<String> alternateNames, String defaultName, String code) {
        Iterable<String> names = noEmpty(Sets.union(alternateNames, ImmutableSet.of(defaultName)));
        Set<String> b = new HashSet<>();
        for (String name : names) {
            b.add(name);
        }

        if(!Strings.isNullOrEmpty(code)) {
            b.add(code);
        }

        return ImmutableSet.copyOf(b);
    }

    @Override
    public boolean processLine(String line) throws IOException {
        Location location = parse(line);
        if (location == null) {
            // Not for indexing
            return true;
        }

        return doProcessLine(line, location);
    }

    protected abstract boolean doProcessLine(String line, Location location);

}
