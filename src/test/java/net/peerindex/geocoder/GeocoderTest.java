package net.peerindex.geocoder;


import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsonschema.JsonSchema;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * @author Enno Shioji (eshioji@gmail.com)
 */
public class GeocoderTest {
    private Geocoder subject;

    @Before
    public void setUp() throws Exception {
        subject = new Geocoder();
    }


    @Test
    public void priorIsPopulatoinWeightedWithActivityShare(){
        Location la = subject.resolve("LA");
        assertEquals(Long.valueOf(3792621), la.getPopulation());
        assertEquals(0.00622011977112818, la.getWeight(), Math.pow(1, -10));
    }


    @Test
    public void extractSingleConsistentLocation(){
        assertEquals(2635167, subject.resolve("UK").getGeonameId());
        assertEquals(2643741, subject.resolve("LONDON.").getGeonameId());
        assertEquals(5391959, subject.resolve(",san Francisco").getGeonameId());
        assertEquals(5555083, subject.resolve("Little Cottonwood Creek Valley, Utah").getGeonameId());
        assertEquals(4983625, subject.resolve("Living in York Harbor with you").getGeonameId());

    }

    @Test
    public void extractFromNonAscii() throws Exception {
        assertEquals(1859171,subject.resolve("神戶").getGeonameId());
    }


    @Test
    public void preferConsistencyOverPrior() throws Exception {
        assertEquals(4517009, subject.resolve("London, US").getGeonameId());
        assertEquals(4517009, subject.resolve("US, London").getGeonameId());
        assertEquals(4517009, subject.resolve("London@US").getGeonameId());
    }

    @Test
    public void preferPriorIfInconsistent(){
        assertEquals(1269750, subject.resolve("London, India").getGeonameId());
        assertEquals(1269750, subject.resolve("India, London").getGeonameId());
        assertEquals(1269750, subject.resolve("London@India").getGeonameId());
    }


    @Test
    public void performanceTest() throws Exception {
        final MetricRegistry mr = new MetricRegistry();

        Timer.Context c = mr.timer("geocoder.object.initialization").time();
        final Geocoder geocoder = new Geocoder();
        c.stop();

        int threadNum = Runtime.getRuntime().availableProcessors();
        ExecutorService exec = Executors.newFixedThreadPool(threadNum);
        final CountDownLatch gate = new CountDownLatch(threadNum + 1);


        final String[] loc = new String[]{"London", "Ohio", "US", "Tokyo"};

        final AtomicInteger numhit = new AtomicInteger();
        for (int i = 0; i < threadNum; i++) {
            Callable<Void> r = new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    gate.countDown();
                    gate.await();
                    while (!Thread.currentThread().isInterrupted()) {
                        ThreadLocalRandom r = ThreadLocalRandom.current();
                        String probe = "I am " + r.nextInt() + " in " + loc[r.nextInt(loc.length)];

                        Timer.Context c = mr.timer("geocoder.resolve").time();
                        Location loc = geocoder.resolve(probe);
                        c.stop();

                        if (loc != null) {
                            numhit.incrementAndGet();
                        }
                    }
                    return null;
                }
            };
            exec.submit(r);
        }

        gate.countDown();

        Thread.sleep(10000);
        exec.shutdownNow();
        exec.awaitTermination(1, TimeUnit.SECONDS);

        System.out.println("Hit:" + numhit.get());


        ConsoleReporter cr = ConsoleReporter.forRegistry(mr).build();
        cr.report();
    }


    @Test
    public void exampleCode() throws Exception {
        ObjectMapper om = new ObjectMapper();
        om.configure(SerializationFeature.INDENT_OUTPUT, true);

        Geocoder geocoder = new Geocoder();

        System.out.println(om.writeValueAsString(geocoder.resolve("Rancho Cordova, US")));
        System.out.println(om.writeValueAsString(geocoder.resolve("Москва является удивительным")));

        JsonSchema schema = om.generateJsonSchema(Location.class);
        System.out.println(schema.toString());
    }



}
