package net.peerindex.geocoder;

import java.util.Set;

/**
 * @author Enno Shioji (eshioji@gmail.com)
 */
class ConsumedMatch {
    final Integer consumedUpto;
    final Set<Location> match;

    ConsumedMatch(Integer consumedUpto, Set<Location> match) {
        this.consumedUpto = consumedUpto;
        this.match = match;
    }
}
