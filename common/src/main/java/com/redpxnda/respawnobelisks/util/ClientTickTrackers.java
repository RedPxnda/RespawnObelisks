package com.redpxnda.respawnobelisks.util;

import java.util.HashMap;
import java.util.Map;

public class ClientTickTrackers {
    private static Map<String, Integer> tracker = new HashMap<>();

    public static int getTracker(String key) {
        return tracker.getOrDefault(key, -1);
    }

    public static int getOrStartTracker(String key, int amount) {
        if (!tracker.containsKey(key)) tracker.put(key, amount);
        return tracker.get(key);
    }

    public static boolean hasTracker(String key) {
        return tracker.containsKey(key);
    }

    public static void tickTracker(String key) {
        tracker.replace(key, tracker.get(key)+1);
    }

    public static void setTracker(String key, int amount) {
        if (tracker.containsKey(key)) tracker.replace(key, amount);
        else tracker.put(key, amount);
    }
}
