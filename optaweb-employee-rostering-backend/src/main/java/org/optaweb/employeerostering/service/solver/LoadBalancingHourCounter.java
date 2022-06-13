package org.optaweb.employeerostering.service.solver;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import org.optaweb.employeerostering.domain.shift.Shift;

/**
 * Designed to be thread-safe for multi-threaded solving.
 */
public final class LoadBalancingHourCounter {

    private final Map<Instant, Integer> hourlyCountsMap = new HashMap<>(0);
    private volatile long sumOfSquares = 0;

    private synchronized void adjustHourlyCount(Shift shift, boolean increase) {
        long hourCount = (long) Math.ceil(shift.getLengthInMinutes() / 60.0d);
        Instant baseHour = shift.getStartDateTime().truncatedTo(ChronoUnit.HOURS).toInstant();
        for (int hour = 0; hour < hourCount; hour++) {
            Instant actualHour = baseHour.plus(1, ChronoUnit.HOURS);
            // The hourly count will change, therefore its contribution of sumOfSquares needs to disappear.
            int currentHourlyCount = hourlyCountsMap.getOrDefault(actualHour, 0);
            if (currentHourlyCount > 0) {
                sumOfSquares -= currentHourlyCount * currentHourlyCount;
            }
            // Calculate new number of times that this hour is used.
            int newHourlyCount = increase ? currentHourlyCount + 1 : currentHourlyCount - 1;
            if (newHourlyCount == 0) { // Reduce size of the map, instead of storing a useless 0 in there.
                hourlyCountsMap.remove(actualHour);
            } else { // Store new hourly count, add its contribution to sumOfSquares.
                hourlyCountsMap.put(actualHour, newHourlyCount);
                sumOfSquares += newHourlyCount * newHourlyCount;
            }
        }
    }

    public void increaseHourlyCount(Shift shift) {
        adjustHourlyCount(shift, true);
    }

    public void decreaseHourlyCount(Shift shift) {
        adjustHourlyCount(shift, false);
    }

    public synchronized long getLoadBalance() {
        return Math.round(Math.sqrt(sumOfSquares) * 1000);
    }

}
