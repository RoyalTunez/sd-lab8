package statistic;

import java.util.Map;

public interface EventStatistic {
    void incEvent(String eventName);

    double getEventStatisticByName(String eventName);

    Map<String, Double> getAllEventStatistic();

    void printStatistic();
}
