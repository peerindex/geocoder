package net.peerindex.geocoder;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.junit.Test;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.Assert.*;

public class CompressGazetteerTest {

    @Test
    public void testCompress() throws Exception {
        File testGazetteer = new File(GeocoderIndexerTest.class.getResource("test.gazetteer.txt").toURI());
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        Files.readLines(testGazetteer, Charsets.UTF_8, new CompressGazetteer(TestUtils.populationThreshold("population.threshold.txt"), pw));

        File expectedOutput = new File(GeocoderIndexerTest.class.getResource("test.compress.gazetteer.test.result.csv").toURI());
        String expected = Files.toString(expectedOutput, Charsets.UTF_8);

        assertEquals(expected, out.toString());
    }
}