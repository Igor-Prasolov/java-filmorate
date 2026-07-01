package ru.yandex.practicum.filmorate.utils;

import java.util.Map;

public class GeneratorID {

    public static Long getNextId(Map<Long, ?> classes) {
        long currentMaxId = classes.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
