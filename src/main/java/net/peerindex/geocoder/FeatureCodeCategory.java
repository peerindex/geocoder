package net.peerindex.geocoder;

import com.google.common.collect.*;

import java.util.Set;


/**
 * This class maps Geonames feature code to our internal grouping
 * @author Enno Shioji (eshioji@gmail.com)
 */
public enum FeatureCodeCategory {
    // DO NOT change the ordering; the order (larger political entry -> smaller political entry)
    // of these enums are used in the code
    PCL(ImmutableSet.of("PCL", "PCLI")),

    ADM1(ImmutableSet.of("ADM1")),
    ADM2(ImmutableSet.of("ADM2")),
    ADM3(ImmutableSet.of("ADM3")),
    ADM4(ImmutableSet.of("ADM4")),

    SUBADM(ImmutableSet.of("ADM5", "ADMD", "PPL", "PPLA", "PPLA2", "PPLA3", "PPLA4", "PPLC", "PPLG", "PPLS"));

    private final Set<String> featureCodes;

    private FeatureCodeCategory(Set<String> featureCodes) {
        this.featureCodes = featureCodes;
    }

    /**
     * Translate Geonames feature code to our internal feature code category
     * @param geonamesFeatureCode
     * @return {@link net.peerindex.geocoder.FeatureCodeCategory} to which this Geonames feature code translates to
     */
    public static FeatureCodeCategory belongsTo(final String geonamesFeatureCode) {
        for (FeatureCodeCategory featureCodeCategory : FeatureCodeCategory.values()) {
            if (featureCodeCategory.featureCodes.contains(geonamesFeatureCode)) {
                return featureCodeCategory;
            }
        }
        return null;
    }


}
