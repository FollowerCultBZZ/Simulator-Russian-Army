package com.example.game.world;

import com.badlogic.gdx.utils.ObjectMap;

public final class Player {

    private final ObjectMap<String, Integer> stats = new ObjectMap<>();

    public void init(ObjectMap<String, Integer> initial) {
        stats.clear();

        if (initial == null) {
            return;
        }

        for (ObjectMap.Entry<String, Integer> entry : initial) {
            if (entry.key == null || entry.key.trim().isEmpty()) {
                throw new IllegalArgumentException("Player stat key cannot be empty");
            }
            if (entry.value == null) {
                throw new IllegalArgumentException("Player stat value cannot be null: " + entry.key);
            }
            stats.put(entry.key, entry.value);
        }
    }

    public int get(String key) {
        if (key == null || key.trim().isEmpty()) {
            return 0;
        }
        return stats.get(key, 0);
    }

    public void set(String key, int value) {
        validateKey(key);
        stats.put(key, value);
    }

    public void add(String key, int delta) {
        validateKey(key);
        stats.put(key, get(key) + delta);
    }

    public boolean meets(ObjectMap<String, Integer> requires) {
        if (requires == null || requires.size == 0) {
            return true;
        }

        for (ObjectMap.Entry<String, Integer> entry : requires) {
            if (entry.key == null || entry.key.trim().isEmpty()) {
                throw new IllegalArgumentException("Requirement key cannot be empty");
            }
            if (entry.value == null) {
                throw new IllegalArgumentException("Requirement value cannot be null: " + entry.key);
            }
            if (get(entry.key) < entry.value) {
                return false;
            }
        }

        return true;
    }

    public void apply(ObjectMap<String, Integer> effects) {
        if (effects == null || effects.size == 0) {
            return;
        }

        for (ObjectMap.Entry<String, Integer> entry : effects) {
            if (entry.key == null || entry.key.trim().isEmpty()) {
                throw new IllegalArgumentException("Effect key cannot be empty");
            }
            if (entry.value == null) {
                throw new IllegalArgumentException("Effect value cannot be null: " + entry.key);
            }
            add(entry.key, entry.value);
        }
    }

    private void validateKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Player stat key cannot be empty");
        }
    }
}
