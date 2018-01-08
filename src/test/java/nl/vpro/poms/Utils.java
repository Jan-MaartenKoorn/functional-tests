package nl.vpro.poms;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michiel Meeuwissen
 */

public class Utils {

    static final Logger log = LoggerFactory.getLogger(Utils.class); 

    private final static Duration WAIT = Duration.ofSeconds(15);

    private static boolean waitUntil(Duration acceptable, Callable<Boolean> r) throws Exception {
        AbstractApiTest.clearCaches();
        Instant start = Instant.now();
        Thread.sleep(Duration.ofSeconds(1).toMillis());
        while (true) {
            if (r.call()) {
                log.info("{} evaluated true", r);
                return true;
            }
            Duration duration = Duration.between(start, Instant.now());
            if (duration.compareTo(acceptable) > 0) {
                log.warn("{} didn't evaluate to true after {} in less than {}", r, duration, acceptable);
                return false;
            }
            log.info("{} didn't evaluate to true yet after {}. Waiting another {}", r, duration, WAIT);
            Thread.sleep(WAIT.toMillis());
        }
    }

    public static boolean waitUntil(Duration acceptable, String callableToDescription, final Callable<Boolean> r) throws Exception {
        log.info("Waiting until " + callableToDescription);
        AbstractApiTest.clearCaches();
        return waitUntil(acceptable, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return r.call();
            }

            public String toString() {
                return "(" + callableToDescription + ")";
            }
        });

    }

    public static <T> T waitUntilNotNull(Duration acceptable, Supplier<T> r) throws Exception {
        return waitUntil(acceptable, r + " != null", r, (o) -> true);
    }

    /**
     * Call supplier until its result evaluates true according to given predicate or the acceptable duration elapses.
     */
    public static <T> T waitUntil(Duration acceptable, String predicateDescription, Supplier<T> r, Predicate<T> predicate) throws Exception {
        final T[] result = (T[]) new Object[1];
        waitUntil(acceptable, predicateDescription, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                result[0] = r.get();
                return result[0] != null && predicate.test(result[0]);
            }

            @Override
            public String toString() {
                return predicate + " supplies: " + r + " current value: " + result[0];
            }
        });
        assertThat(result[0]).isNotNull();
        return result[0];
    }
}

