package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.genre.GenreStorage;
import ru.yandex.practicum.filmorate.dao.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.film.Film;


import ru.yandex.practicum.filmorate.dao.film.FilmsDbStorage;
import ru.yandex.practicum.filmorate.dao.likes.LikesStorage;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.validation.Film.ValidationFilms;

import java.util.*;


@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {

    private final FilmsDbStorage filmStorage;
    private final UserService userService;
    private final LikesStorage likesStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        ValidationFilms.validReleaseDate(film.getReleaseDate());
        validateMpaExists(film, "Ошибка создания фильма: рейтинг с id {} не найден");
        validateGenresExist(film);

        return filmStorage.save(film);
    }

    public Film update(Film film) {
        if (film.getId() == null) {
            log.warn("Ошибка обновления фильма: отсутствует id");
            throw new ValidationException("Отсутствует id");
        }
        validateFilmExist(film.getId(), "Ошибка обновления фильма: фильм с ID {} не найден");
        ValidationFilms.validReleaseDate(film.getReleaseDate());
        validateMpaExists(film, "Ошибка обновления фильма: рейтинг с ID {} не найден");
        validateGenresExist(film);
        return filmStorage.update(film);
    }

    public void addLike(Long filmId, Long userId) {
        validateFilmExist(filmId, "Ошибка лайка: фильм с ID {} не найден");
        validateUserExists(userId, "Ошибка лайка: пользователь с ID {} не найден");

        if (likesStorage.existsLike(filmId, userId)) {
            log.warn("Ошибка: пользователь {} уже лайкнул фильм {}", userId, filmId);
            return;
        }

        likesStorage.addLike(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        validateFilmExist(filmId, "Ошибка удаления лайка: фильм с ID {} не найден");
        validateUserExists(userId, "Ошибка удаления лайка: пользователь с ID {} не найден");

        if (!likesStorage.existsLike(filmId, userId)) {
            log.warn("Ошибка: пользователь {} не лайкал фильм {}", userId, filmId);
            return;
        }

        likesStorage.removeLike(filmId, userId);
        log.info("Пользователь {} убрал лайк с фильма {}", userId, filmId);
    }

    public Collection<Film> findPopularFilms(Integer count) {
        if (count == null || count <= 0) {
            count = 10;
        }
        List<Long> popularId = likesStorage.findPopularFilmId(count);
        List<Film> result = new ArrayList<>();
        for (Long id : popularId) {
            filmStorage.findById(id).ifPresent(result::add);
        }
        return result;
    }

    public Film findById(Long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> {
                    log.warn("Фильм с id {} не найден", id);
                    return new NotFoundException("Фильм с ID " + id + " не найден");
                });
    }


    private void validateFilmExist(Long filmId, String logMessage) {
        if (!filmStorage.existsById(filmId)) {
            log.warn(logMessage, filmId);
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }
    }

    private void validateUserExists(Long userId, String logMessage) {
        if (userService.findUserById(userId) == null) {
            log.warn(logMessage, userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
    }

    private void validateMpaExists(Film film, String logMessage) {
        if (film.getMpa() != null) {
            Long mpaId = film.getMpa().getId();
            if (!mpaStorage.existsById(mpaId)) {
                log.warn(logMessage, mpaId);
                throw new NotFoundException("Рейтинг с id " + mpaId + " не найден");
            }
        }
    }

    private void validateGenresExist(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                if (!genreStorage.existsById(genre.getId())) {
                    log.warn("Жанр с id {} не найден", genre.getId());
                    throw new NotFoundException("Жанр с id " + genre.getId() + " не найден");
                }
            }
        }
    }

}
