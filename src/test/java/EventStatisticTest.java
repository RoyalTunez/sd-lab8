import clock.SettableClock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import statistic.EventStatistic;
import statistic.RpmStatistic;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class EventStatisticTest {
    private Instant now;
    private SettableClock clock;
    private EventStatistic eventsStatistic;

    private final static int HOUR_TO_MINUTES = 60;
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

    @Test
    public void testRandomStatistic() {
        final int TEST_SIZE = 10000;
        final int EVENTS_SIZE = 200;

        Random random = new Random();

        Map<String, Integer> eventsCounter = new HashMap<>();

        for (int eventId = 0; eventId <= EVENTS_SIZE; eventId++) {
            eventsCounter.put(String.valueOf(eventId), 0);
        }

        Deque<String> eventList = new ArrayDeque<>();

        for (int it = 0; it < TEST_SIZE; it++) {
            clock.setNow(now.plus(it, ChronoUnit.MINUTES));

            String eventName = String.valueOf(random.nextInt(EVENTS_SIZE));

            eventList.addLast(eventName);

            int oldNewEventCounter = 0;

            try {
                oldNewEventCounter = eventsCounter.get(eventName);
            } catch (Exception e) {
                System.out.println(eventName);
            }

            eventsCounter.replace(eventName, oldNewEventCounter + 1);

            if (eventList.size() > HOUR_TO_MINUTES) {
                String expiredEventName = eventList.pollFirst();

                int oldExpiredEventCounter = eventsCounter.get(expiredEventName);

                eventsCounter.replace(expiredEventName, oldExpiredEventCounter - 1);
            }

            eventsStatistic.incEvent(eventName);

            for (var eventCounter : eventsCounter.entrySet()) {
                String checkEventName = eventCounter.getKey();
                double expectedEventRpm = (double)eventCounter.getValue() / HOUR_TO_MINUTES;

                Assertions.assertEquals(expectedEventRpm, eventsStatistic.getEventStatisticByName(checkEventName), MAX_ERROR * (it + 1));
            }
        }
    }
}
