package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.film.FilmStorage;
import ru.yandex.practicum.filmorate.dao.genre.GenreStorage;
import ru.yandex.practicum.filmorate.dao.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.dao.likes.LikesStorage;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.validation.Film.ValidationFilms;

import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
public class FilmService {
    private final UserService userService;
    private final LikesStorage likesStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;
    private final FilmStorage filmStorage;

    public FilmService(@Qualifier("filmsDbStorage") FilmStorage filmStorage,
                       UserService userService,
                       LikesStorage likesStorage,
                       MpaStorage mpaStorage,
                       GenreStorage genreStorage) {
        this.filmStorage = filmStorage;
        this.userService = userService;
        this.likesStorage = likesStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }


    public Collection<Film> findAll() {
        log.info("Find all films");
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        ValidationFilms.validReleaseDate(film.getReleaseDate());
        validateMpaExists(film, "Ошибка создания фильма: рейтинг с id {} не найден");
        validateGenresExist(film);
        Film saved = filmStorage.save(film);

        log.info("Film created: name={}, id={}", saved.getName(), saved.getId());
        return saved;
    }

    public Film update(Film film) {
        if (film.getId() == null) {
            log.warn("Update failed: filmId is null");
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

        return likesStorage.findPopularFilm(count);
    }

    public Film findById(Long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> {
                    log.warn("Фильм с id {} не найден", id);
                    return new NotFoundException("Фильм с ID " + id + " не найден");
                });
    }

    public Collection<Film> findCommonFilms(Long userId, Long friendId) {
        validateUserExists(userId, "Ошибка вывода: пользователь с ID {} не найден");
        validateUserExists(friendId, "Ошибка вывода: друг с ID {} не найден");
        return likesStorage.findCommonFilms(userId, friendId);
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
        if (film.getMpa() == null) {
            log.warn("Mpa is null for film: {}", film.getName());
            throw new ValidationException("Ошибка: рейтинг должен быть указан");

        }
        Long mpaId = film.getMpa().getId();
        if (!mpaStorage.existsById(mpaId)) {
            log.warn(logMessage, mpaId);
            throw new NotFoundException("Рейтинг с id " + mpaId + " не найден");
        }
    }


    private void validateGenresExist(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        List<Long> genreId = film.getGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toList());

        List<Genre> existingGenre = genreStorage.findById(genreId);

        if (existingGenre.size() != genreId.size()) {
            log.warn("Some genres not found: expected {}, found {}",
                    genreId.size(), existingGenre.size());
            throw new NotFoundException("Жанры не найдены");
        }
    }

}
