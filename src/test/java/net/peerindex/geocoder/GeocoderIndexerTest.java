package net.peerindex.geocoder;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Charsets;
import com.google.common.base.Equivalence;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * @author Enno Shioji (eshioji@gmail.com)
 */
public class GeocoderIndexerTest {
    @Test
    public void testIndex() throws Exception {
        ImmutableMap<ImmutableList<String>, Set<Location>> index = TestUtils.testIndex("test.gazetteer.txt");
        {
            String output = new ObjectMapper().writeValueAsString(index);
            String expected = Resources.toString(this.getClass().getResource("test.indexed.locations.result.json"), Charsets.UTF_8);
            JSONAssert.assertEquals(expected, output, false);
        }
    }

    @Test
    public void testLocations() throws Exception {
        Set<Location> locations = TestUtils.testLocations("test.gazetteer.txt");
        {
            String output = new ObjectMapper().writeValueAsString(locations);
            String expected = Resources.toString(this.getClass().getResource("test.extracted.locations.result.json"), Charsets.UTF_8);
            JSONAssert.assertEquals(expected, output, false);
        }
    }
}
