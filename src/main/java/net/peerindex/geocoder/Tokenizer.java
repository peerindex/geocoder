package net.peerindex.geocoder;

import com.google.common.base.CharMatcher;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.util.List;
import java.util.Locale;

/**
 * Tokenizer for free text location field. Very rough in order to cope with the wild world of social media
 * @author Enno Shioji (eshioji@gmail.com)
 */
class Tokenizer {

    private final CharMatcher notLetterNorDigit = new CharMatcher() {
        @Override
        public boolean matches(char c) {
            return !Character.isLetter((int) c) && !Character.isDigit((int) c);
        }
    };
    private final Splitter splitter = Splitter.on(notLetterNorDigit).omitEmptyStrings().trimResults();
    
    ImmutableList<String> tokenize(String locationString) {
        // Split into tokens (note that delimiting characters gets removed)
        Iterable<String> split = splitter.split(locationString);


        split = Iterables.transform(split, new Function<String, String>() {
            @Override
            public String apply(String input) {
                if(input.length() < 3){
                    // Do not normalize case if input is too short
                    return input;
                }else{
                    return input.toUpperCase(Locale.ENGLISH);
                }
            }
        });

        return ImmutableList.copyOf(split);
    }

}
