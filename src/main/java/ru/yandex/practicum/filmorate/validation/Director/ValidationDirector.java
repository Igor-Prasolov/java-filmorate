package ru.yandex.practicum.filmorate.validation.Director;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;

@Slf4j
public class ValidationDirector {
    public static void validateName(Director director) {
        if (director.getName() == null || director.getName().isBlank()) {
            log.warn("Ошибка: имя режиссёра пустое");
            throw new ValidationException("Имя режиссёра не может быть пустым");
        }
    }

    public static void validateId(Long id) {
        if (id == null) {
            log.warn("Ошибка: ID режиссёра не может быть null");
            throw new ValidationException("ID режиссёра не может быть пустым");
        }
    }

    public static void validateExists(Long id, boolean exists) {
        if (!exists) {
            log.warn("Ошибка: режиссёр с ID {} не найден", id);
            throw new NotFoundException("Режиссёр с ID " + id + " не найден");
        }
    }
}