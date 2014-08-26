package net.peerindex.geocoder;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import com.google.common.io.LineProcessor;
import net.peerindex.geocoder.FeatureCodeCategory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Enno Shioji (eshioji@gmail.com)
 */
enum Utils {
    ;

    static ImmutableMap<FeatureCodeCategory, Long> readPopulationTh(BufferedReader br) throws IOException {
        return CharStreams.readLines(br, new LineProcessor<ImmutableMap<FeatureCodeCategory, Long>>() {
            private final ImmutableMap.Builder<FeatureCodeCategory, Long> b = ImmutableMap.builder();

            @Override
            public boolean processLine(String line) throws IOException {
                List<String> parsed = ImmutableList.copyOf(Splitter.on(",").split(line));
                b.put(FeatureCodeCategory.valueOf(parsed.get(0)), Long.parseLong(parsed.get(1)));
                return true;
            }

            @Override
            public ImmutableMap<FeatureCodeCategory, Long> getResult() {
                return b.build();
            }
        });
    }

    static ImmutableMap<String, Double> readActivityShare(BufferedReader pclToActivityShareSrc) throws IOException {
        return CharStreams.readLines(pclToActivityShareSrc, new LineProcessor<ImmutableMap<String, Double>>() {
            final Map<String, Double> acc = new HashMap<>();

            @Override
            public boolean processLine(String line) throws IOException {
                List<String> split = ImmutableList.copyOf(Splitter.on(',').split(line));
                String pcl = split.get(0);
                Double share = Double.valueOf(split.get(1));
                acc.put(pcl, share);
                return true;
            }

            @Override
            public ImmutableMap<String, Double> getResult() {
                return ImmutableMap.copyOf(acc);
            }
        });
    }

}
