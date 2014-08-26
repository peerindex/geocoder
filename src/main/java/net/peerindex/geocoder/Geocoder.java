package net.peerindex.geocoder;

import com.google.common.base.*;
import com.google.common.collect.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * @author Enno Shioji (eshioji@gmail.com)
 */
public class Geocoder {
    private final ImmutableMap<ImmutableList<String>, Set<Location>> index;

    // Special rules
    private final Location loc5128581;
    private final Location loc5368361;

    private final Tokenizer tokenizer = new Tokenizer();

    public Geocoder(){
        try {
            try (
                    BufferedReader gazetteer = new BufferedReader(new InputStreamReader(Geocoder.class.getResourceAsStream("compressed.gazetteer.txt"), Charsets.UTF_8));
                    BufferedReader pThreshold = new BufferedReader(new InputStreamReader(Geocoder.class.getResourceAsStream("population.threshold.txt"), Charsets.UTF_8));
                    BufferedReader activityShare = new BufferedReader(new InputStreamReader(Geocoder.class.getResourceAsStream("online.activity.share.txt"), Charsets.UTF_8));
            ) {
                GeoDbIndexer indexer = new GeoDbIndexer(gazetteer, pThreshold, activityShare);
                Set<Location> locations = indexer.extractLocations();
                index = indexer.index(locations);
                loc5128581 = tryFindById(index, 5128581);
                loc5368361 = tryFindById(index, 5368361);
            }
        }catch (IOException e){
            // Not supposed to happen
            throw new AssertionError(e);
        }

    }


    public Geocoder(ImmutableMap<ImmutableList<String>, Set<Location>> index) {
        this.index = index;
        loc5128581 = tryFindById(index, 5128581);
        loc5368361 = tryFindById(index, 5368361);
    }

    private Location tryFindById(Map<ImmutableList<String>, Set<Location>> locations, int id) {
        for (Location location : Iterables.concat(locations.values())) {
            if(location.getGeonameId() == id){
                return location;
            }
        }
        return null;
    }

    public Location resolve(String freeTextLocation) {

        ImmutableList<String> tokenizedQuery = tokenizer.tokenize(freeTextLocation);
        List<Set<Location>> candidates = match(tokenizedQuery);

        Location ret = disambiguate(tokenizedQuery, candidates);
        return ret;
    }



    private Location disambiguate(List<String> tokenizedQuery, List<Set<Location>> candidates) {
        if (candidates.size() <= 0) {
            return null;
        }

        Set<Location> consistent = new HashSet<>();
        Set<Location> inconsistent = new HashSet<>();

        List<Set<Location>> westernOrder = new ArrayList<>(candidates);
        List<Set<Location>> invertedOrder = new ArrayList<>(candidates);
        Collections.reverse(invertedOrder);

        pickoutLocationsWithConsistency(consistent, inconsistent, westernOrder);

        pickoutLocationsWithConsistency(consistent, inconsistent, invertedOrder);


        Comparator<Location> byPriorDesc = new Comparator<Location>() {
            @Override
            public int compare(Location location1, Location location2) {
                // Descending!
                return -1 * location1.getWeight().compareTo(location2.getWeight());
            }
        };


        if (consistent.size() > 0) {
            List<Location> finalCandidates = new ArrayList<>(consistent);
            Collections.sort(finalCandidates, byPriorDesc);
            return pickOne(tokenizedQuery, finalCandidates);
        } else if (inconsistent.size() > 0) {
            // Fallback to inconsistent matches
            List<Location> finalCandidates = new ArrayList<>(inconsistent);
            Collections.sort(finalCandidates, byPriorDesc);
            return pickOne(tokenizedQuery, finalCandidates);
        } else {
            // No candidates
            return null;
        }
    }

    private Location pickOne(List<String> tokenizedQuery, List<Location> finalCandidates) {
        Location firstCandidate = finalCandidates.get(0);

        // Special rules that improve the results
        // TODO better documentation
        if (loc5128581 != null && firstCandidate.getGeonameId() == 5128638 && (tokenizedQuery.contains("NY"))) {
            return loc5128581;
        } else if (loc5368361 != null && firstCandidate.getGeonameId() == 4331987 && tokenizedQuery.contains("LA")) {
            return loc5368361;
        }
        return finalCandidates.get(0);
    }


    private void pickoutLocationsWithConsistency(Set<Location> consistent, Set<Location> inconsistent, List<Set<Location>> candidates) {
        for (Location finest : candidates.get(0)) {
            Queue<Set<Location>> queue = new LinkedList<>(candidates.subList(1, candidates.size()));
            while (true) {
                Set<Location> coarser = queue.poll();
                if (coarser == null || coarser.size() <= 0) {
                    // We exhausted the coarser location candidates
                    // successfully finding parents. This is a proper match
                    consistent.add(finest);
                    break;
                }
                Location parent = Iterables.find(coarser, new Contains(finest), null);
                if (parent != null) {
                    // Found a parent, good
                    continue;
                } else {
                    // Did not find a parent.
                    // Not a consistent match
                    inconsistent.add(finest);
                    break;
                }
            }
        }
    }


    public List<Set<Location>> match(final ImmutableList<String> input) {
        List<Set<Location>> candidates = new ArrayList<>();
        ImmutableList<String> probe = input;
        while(true){
            ConsumedMatch consumed_match = searchGreedily(probe);
            Integer consumedUpTo = consumed_match.consumedUpto;
            Set<Location> match = consumed_match.match;
            if(!match.isEmpty()){
                probe = probe.subList(consumedUpTo, probe.size());
                candidates.add(match);
            }else if(probe.size() > 1){
                // Drop the left most token
                probe = probe.subList(1,probe.size());
            }else{
                // Can't match anymore
                break;
            }
        }
        return candidates;
    }

    private ConsumedMatch searchGreedily(final ImmutableList<String> input) {
        for (int i = input.size(); i >= 1; i--) {
            ImmutableList<String> probe_key = input.subList(0, i);
            Set<Location> candidates = index.get(probe_key);
            if (candidates != null) {
                return new ConsumedMatch(i, candidates);
            }
        }
        return new ConsumedMatch(-1, new HashSet<Location>());
    }



    /**
     * Does the parent contain the child?
     */
    private class Contains implements Predicate<Location> {
        private final Location child;

        public Contains(Location child) {
            this.child = child;
        }

        @Override
        public boolean apply(Location parent) {
            if (child.getFeatureCodeCategory().compareTo(parent.getFeatureCodeCategory()) <= 0) {
                return false;
            }
            for (FeatureCodeCategory featureCodeCategory : EnumSet.range(FeatureCodeCategory.PCL, parent.getFeatureCodeCategory())) {
                String childCode = child.getCodes().get(featureCodeCategory);
                String parentCode = parent.getCodes().get(featureCodeCategory);
                if (childCode == null || parentCode == null) {
                    continue;
                } else if (!childCode.equals(parentCode)) {
                    return false;
                }
            }
            return true;
        }
    }
}
