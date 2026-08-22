package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.film.FilmStorage;
import ru.yandex.practicum.filmorate.dao.user.UserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;


@Service
@RequiredArgsConstructor
@Slf4j
public class ValidateService {
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;


    public void validateFilmExist(Long filmId, String logMessage) {
        if (!filmStorage.existsById(filmId)) {
            log.warn(logMessage, filmId);
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }
    }


    public void validateUserExists(Long userId, String logMessage) {
        if (!userStorage.existsById(userId)) {
            log.warn(logMessage, userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
    }
}
