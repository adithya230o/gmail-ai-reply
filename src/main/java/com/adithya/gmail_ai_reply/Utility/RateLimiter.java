package com.adithya.gmail_ai_reply.Utility;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.time.Instant;
import java.time.temporal.ChronoUnit;



/**
 * RateLimiter enforces a per-user request limit within a defined time window.
 * Max requests allowed per user -> 14/minute and 199/day
 */
@Component
public class RateLimiter {

    private final int MAX_PER_MINUTE = 10;
    private final int MAX_PER_DAY = 300;

    private final Map<String, List<Instant>> minuteTracker = new ConcurrentHashMap<>();
    private final Map<String, List<Instant>> dailyTracker = new ConcurrentHashMap<>();

    public int checkRequest(String userKey) {
        Instant now = Instant.now();

        // Day-level check
        dailyTracker.putIfAbsent(userKey, new ArrayList<>());
        List<Instant> dailyRequests = dailyTracker.get(userKey);
        dailyRequests.removeIf(t -> t.isBefore(now.minus(1, ChronoUnit.DAYS)));

        if (dailyRequests.size() >= MAX_PER_DAY) return 3;

        // Minute-level check
        minuteTracker.putIfAbsent(userKey, new ArrayList<>());
        List<Instant> minuteRequests = minuteTracker.get(userKey);
        minuteRequests.removeIf(t -> t.isBefore(now.minusSeconds(60)));

        if (minuteRequests.size() >= MAX_PER_MINUTE) return 2;

        // Add current request
        minuteRequests.add(now);
        dailyRequests.add(now);

        return 1;
    }
}