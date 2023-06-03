package com.redpxnda.respawnobelisks.registry.block.entity.theme;

import java.util.HashMap;
import java.util.Map;

public class ObeliskThemeData {
    private final Map<String, Float> trackers = new HashMap<>();
    private final Map<String, Long> timeTrackers = new HashMap<>();
    private final Map<String, Object> specialTrackers = new HashMap<>();

    public float get(String key) {
        return get(key, 0f);
    }
    public float get(String key, float ifFailed) {
        Float value = trackers.get(key);
        if (value == null) {
            value = ifFailed;
            trackers.put(key, ifFailed);
        }
        return value;
    }
    public void put(String str, float value) {
        trackers.put(str, value);
    }

    public long getLong(String key) {
        return getLong(key, 0L);
    }
    public long getLong(String key, long ifFailed) {
        Long value = timeTrackers.get(key);
        if (value == null) {
            value = ifFailed;
            timeTrackers.put(key, ifFailed);
        }
        return value;
    }
    public void putLong(String str, long value) {
        timeTrackers.put(str, value);
    }
    public Object getDynamic(String key) {
        return specialTrackers.get(key);
    }
    public void putDynamic(String str, Object value) {
        specialTrackers.put(str, value);
    }

    public ObeliskThemeData() {
    }
}
