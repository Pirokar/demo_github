package com.sequenia.threads;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by yuri on 01.09.2016.
 */
public class MockSharedPrefs implements SharedPreferences {
    HashMap<String, Object> map = new HashMap<>();

    @Override
    public Map<String, ?> getAll() {
        return map;
    }

    @Nullable
    @Override
    public String getString(String key, String defValue) {
        if (map.containsKey(key)) return (String) map.get(key);
        else return defValue;
    }

    @Nullable
    @Override
    public Set<String> getStringSet(String key, Set<String> defValues) {
        if (map.containsKey(key)) return (Set<String>) map.get(key);
        else return defValues;
    }

    @Override
    public int getInt(String key, int defValue) {
        if (map.containsKey(key)) return (int) map.get(key);
        else return defValue;
    }

    @Override
    public long getLong(String key, long defValue) {
        if (map.containsKey(key)) return (long) map.get(key);
        else return defValue;
    }

    @Override
    public float getFloat(String key, float defValue) {
        if (map.containsKey(key)) return (float) map.get(key);
        else return defValue;
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        if (map.containsKey(key)) return (boolean) map.get(key);
        else return defValue;
    }

    @Override
    public boolean contains(String key) {
        return false;
    }

    @Override
    public SharedPreferences.Editor edit() {
        return new SharedPreferences.Editor() {
            @Override
            public SharedPreferences.Editor putString(String key, String value) {
                map.put(key, value);
                return this;
            }

            @Override
            public SharedPreferences.Editor putStringSet(String key, Set<String> values) {
                map.put(key, values);
                return this;
            }

            @Override
            public SharedPreferences.Editor putInt(String key, int value) {
                map.put(key, value);
                return this;
            }

            @Override
            public SharedPreferences.Editor putLong(String key, long value) {
                map.put(key, value);
                return this;
            }

            @Override
            public SharedPreferences.Editor putFloat(String key, float value) {
                map.put(key, value);
                return this;
            }

            @Override
            public SharedPreferences.Editor putBoolean(String key, boolean value) {
                map.put(key, value);
                return this;
            }

            @Override
            public SharedPreferences.Editor remove(String key) {
                map.remove(key);
                return this;
            }

            @Override
            public SharedPreferences.Editor clear() {
                map.clear();
                return this;
            }

            @Override
            public boolean commit() {
                return true;
            }

            @Override
            public void apply() {
            }
        };
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {

    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {

    }
}
