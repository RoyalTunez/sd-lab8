package statistic;

import clock.Clock;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RpmStatistic implements EventStatistic {
    private final Clock clock;
    private Map<String, List<Instant>> events = new HashMap<String, List<Instant>>();
    private static final int HOUR_TO_MINUTES = 60;

    public RpmStatistic(Clock clock) {
        this.clock = clock;
    }

    @Override
    public void incEvent(String eventName) {
        removeExpired(eventName);

        if (!events.containsKey(eventName)) {
            events.put(eventName, new ArrayList<>());
        }

        events.get(eventName).add(clock.now());
    }

    @Override
    public double getEventStatisticByName(String eventName) {
        removeExpired(eventName);

        if (!events.containsKey(eventName)) {
            return 0.0;
        }

        return (double)events.get(eventName).size() / (double)HOUR_TO_MINUTES;
    }

    @Override
    public Map<String, Double> getAllEventStatistic() {
        Map<String, Double> eventsRpm = new HashMap<>();

        removeExpired();

        for (var event: events.entrySet()) {
            var eventName = event.getKey();
            var eventRpm = event.getValue().size() / (double)HOUR_TO_MINUTES;

            eventsRpm.put(eventName, eventRpm);
        }

        return eventsRpm;
    }

    @Override
    public void printStatistic() {
        var allEventStatistics =  getAllEventStatistic();

        for (var event: allEventStatistics.entrySet()) {
            var eventName = event.getKey();
            var eventRpm = event.getValue();

            System.out.println(eventName + " : " + eventRpm);
        }
    }

    private void removeExpired(String eventName) {
        if (!events.containsKey(eventName)) {
            return;
        }

        List<Instant> filteredEventInstants = new ArrayList<>();

        var eventInstants = events.get(eventName);

        for (var instant: eventInstants) {
            if (instant.isAfter(clock.now().minus(1, ChronoUnit.HOURS))) {
                filteredEventInstants.add(instant);
            }
        }

        if (filteredEventInstants.isEmpty()) {
            events.remove(eventName);
        } else {
            events.replace(eventName, filteredEventInstants);
        }
    }

    private void removeExpired() {
       var eventsNames = events.keySet();

       for (String eventName: eventsNames) {
           removeExpired(eventName);
       }
    }
}
