package com.redpxnda.respawnobelisks.registry.block.entity.theme;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.Identifier;

public class ThemeLayout {
    private final Map<Identifier, ThemeData> data = new HashMap<>();

    public ThemeLayout() {
    }

    public ThemeData get(String str) {
        return get(new Identifier(str));
    }
    public ThemeData get(Identifier loc) {
        ThemeData dat = data.get(loc);
        if (dat == null) {
            dat = new ThemeData();
            data.put(loc, dat);
        }
        return dat;
    }
    public ThemeData getOrCreate(Identifier loc) {
        ThemeData dat = data.get(loc);
        if (dat == null) {
            dat = new ThemeData();
            data.put(loc, dat);
        }
        return dat;
    }

    public static class ThemeData extends HashMap<String, Object> {
        public ThemeData() {}

        public <T> T get(String key, Class<T> cls, T defaultValue, boolean setup) {
            Object val = get(key);
            if (val == null || !cls.isAssignableFrom(val.getClass())) {
                if (setup) put(key, defaultValue);
                return defaultValue;
            }
            return (T) val;
        }

        public Long getLong(String key, Long def) {
            return get(key, Long.class, def, true);
        }

        public Float getFloat(String key, Float def) {
            return get(key, Float.class, def, true);
        }

        public Double getDouble(String key, Double def) {
            return get(key, Double.class, def, true);
        }
    }
}
