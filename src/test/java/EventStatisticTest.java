import clock.SettableClock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import statistic.EventStatistic;
import statistic.RpmStatistic;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class EventStatisticTest {
    private Instant now;
    private SettableClock clock;
    private EventStatistic eventsStatistic;

    private final static int HOUR_TO_MINUTES = 60;
    private final static int HOUR_TO_SECONDS = 3600;
    private final static double MAX_ERROR = 1e-9;

    @BeforeEach
    public void setUp() {
        now = Instant.now();
        clock = new SettableClock(now);
        eventsStatistic = new RpmStatistic(clock);
    }

    @Test
    public void testEmptyAllEventStatistic() {
        var rpms = eventsStatistic.getAllEventStatistic();

        Assertions.assertEquals(rpms.size(), 0.0);
    }

    @Test
    public void testEventStatisticByName() {
        String eventName = "event";

        final int INC_AMOUNT = 100;

        for (int inc = 0; inc <= INC_AMOUNT; inc++) {
            double rpmActual = eventsStatistic.getEventStatisticByName(eventName);

            double rpmExpected = inc / (double) HOUR_TO_MINUTES;

            Assertions.assertEquals(rpmExpected, rpmActual, MAX_ERROR);

            eventsStatistic.incEvent(eventName);
        }
    }

    @Test
    public void testAllEventStatistic() {
        eventsStatistic.incEvent("firstEvent");
        clock.setNow(now.plus(1, ChronoUnit.MINUTES));
        Assertions.assertEquals(1.0 / 60.0, eventsStatistic.getEventStatisticByName("firstEvent"), MAX_ERROR);

        eventsStatistic.incEvent("secondEvent");
        clock.setNow(now.plus(2, ChronoUnit.MINUTES));
        Assertions.assertEquals(1.0 / 60.0, eventsStatistic.getEventStatisticByName("firstEvent"), MAX_ERROR);
        Assertions.assertEquals(1.0 / 60.0, eventsStatistic.getEventStatisticByName("secondEvent"), MAX_ERROR);

        eventsStatistic.incEvent("firstEvent");
        Assertions.assertEquals(2.0 / 60.0, eventsStatistic.getEventStatisticByName("firstEvent"), MAX_ERROR);

        clock.setNow(now.plus(1, ChronoUnit.HOURS));
        Assertions.assertEquals(1.0 / 60.0, eventsStatistic.getEventStatisticByName("firstEvent"), MAX_ERROR);
        Assertions.assertEquals(1.0 / 60.0, eventsStatistic.getEventStatisticByName("secondEvent"), MAX_ERROR);

        clock.setNow(now.plus(61, ChronoUnit.MINUTES));
        Assertions.assertEquals(1.0 / 60.0, eventsStatistic.getEventStatisticByName("firstEvent"), MAX_ERROR);
        Assertions.assertEquals(0.0, eventsStatistic.getEventStatisticByName("secondEvent"), MAX_ERROR);
    }
}
