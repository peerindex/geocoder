package net.peerindex.geocoder;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;

import java.io.*;

/**
 * @author Enno Shioji (eshioji@gmail.com)
 */
class CompressGazetteer extends GazetteerParser<Void> {
    private final ImmutableMap<FeatureCodeCategory, Long> populationTh;
    private final PrintWriter out;
    CompressGazetteer(ImmutableMap<FeatureCodeCategory, Long> populationTh, PrintWriter out) {
        this.populationTh = populationTh;
        this.out = out;
    }

    @Override
    protected boolean doProcessLine(String line, Location location) {
        long threshold = populationTh.get(location.getFeatureCodeCategory());

        if(location.getPopulation() < threshold){
            // This location is too small to extractLocations
            return true;
        }

        out.println(line);

        return true;
    }

    @Override
    public Void getResult() {
        return null;
    }

    public static void main(String[] args) throws Exception {
        try (
                PrintWriter out = new PrintWriter("compressed.gazetteer.txt");
                BufferedReader threshold = new BufferedReader(new InputStreamReader(CompressGazetteer.class.getResourceAsStream("population.threshold.txt"), Charsets.UTF_8));
        ) {
            ImmutableMap<FeatureCodeCategory, Long> populationTh = Utils.readPopulationTh(threshold);
            Files.readLines(new File(args[0]), Charsets.UTF_8, new CompressGazetteer(populationTh, out));
        }
    }

}
