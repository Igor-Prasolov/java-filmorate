package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.director.DirectorStorage;
import ru.yandex.practicum.filmorate.dao.film.FilmStorage;
import ru.yandex.practicum.filmorate.dao.genre.GenreStorage;
import ru.yandex.practicum.filmorate.dao.likes.LikesStorage;
import ru.yandex.practicum.filmorate.dao.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.dao.likes.LikesStorage;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.feed.EventOperation;
import ru.yandex.practicum.filmorate.model.feed.EventType;
import ru.yandex.practicum.filmorate.validation.Film.ValidationFilms;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class FilmService {
    private final LikesStorage likesStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;
    private final FilmStorage filmStorage;
    private final FeedService feedService;
    private final DirectorStorage directorStorage;
    private final ValidateService validateService;

    public FilmService(@Qualifier("filmsDbStorage") FilmStorage filmStorage,
                       LikesStorage likesStorage,
                       MpaStorage mpaStorage,
                       GenreStorage genreStorage,
                       ValidateService validateService,
                       FeedService feedService,
                       DirectorStorage directorStorage) {
        this.filmStorage = filmStorage;
        this.likesStorage = likesStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
        this.validateService = validateService;
        this.feedService = feedService;
        this.directorStorage = directorStorage;
    }


    public Collection<Film> findAll() {
        log.info("Find all films");
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        ValidationFilms.validReleaseDate(film.getReleaseDate());
        validateMpaExists(film, "Ошибка создания фильма: рейтинг с id {} не найден");
        validateGenresExist(film);
        validateDirectorsExist(film);
        Film saved = filmStorage.save(film);

        log.info("Film created: name={}, id={}", saved.getName(), saved.getId());
        return saved;
    }

    public Film update(Film film) {
        if (film.getId() == null) {
            log.warn("Update failed: filmId is null");
            throw new ValidationException("Отсутствует id");
        }
        validateService.validateFilmExist(film.getId(), "Ошибка обновления фильма: фильм с ID {} не найден");
        ValidationFilms.validReleaseDate(film.getReleaseDate());
        validateMpaExists(film, "Ошибка обновления фильма: рейтинг с ID {} не найден");
        validateGenresExist(film);
        validateDirectorsExist(film);
        return filmStorage.update(film);
    }

    public void addLike(Long filmId, Long userId) {
        validateService.validateFilmExist(filmId, "Ошибка лайка: фильм с ID {} не найден");
        validateService.validateUserExists(userId, "Ошибка лайка: пользователь с ID {} не найден");

//        if (likesStorage.existsLike(filmId, userId)) {
//            log.warn("Ошибка: пользователь {} уже лайкнул фильм {}", userId, filmId);
//            throw new ValidationException("Пользователь уже поставил лайк этому фильму");
//        }

        likesStorage.addLike(filmId, userId);

        feedService.addEvent(
                userId,
                EventType.LIKE,
                EventOperation.ADD,
                filmId
        );

        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        validateService.validateFilmExist(filmId, "Ошибка удаления лайка: фильм с ID {} не найден");
        validateService.validateUserExists(userId, "Ошибка удаления лайка: пользователь с ID {} не найден");

        if (!likesStorage.existsLike(filmId, userId)) {
            log.warn("Ошибка: пользователь {} не лайкал фильм {}", userId, filmId);
            throw new ValidationException("Пользователь не лайкал фильм");
        }

        likesStorage.removeLike(filmId, userId);

        feedService.addEvent(
                userId,
                EventType.LIKE,
                EventOperation.REMOVE,
                filmId
        );

        log.info("Пользователь {} убрал лайк с фильма {}", userId, filmId);
    }


    public Collection<Film> findPopularFilms(Integer count, Long genreId, Integer year) {
        if (count == null || count <= 0) {
            count = 10;
        }
        if (genreId != null) {
            genreStorage.findById(genreId)
                    .orElseThrow(() -> new NotFoundException("Жанр с ID " + genreId + " не найден"));
        }
        if (year != null && year < 1895) {
            throw new ValidationException("Год не может быть меньше 1895");
        }

        return likesStorage.findPopularFilm(count, genreId, year);
    }

    public Collection<Film> searchFilms(String query, String by) {

        if (query == null || query.isBlank()) {
            log.warn("Поиск отклонён: query пустой или null");
            throw new ValidationException("Параметр query не может быть пустым");
        }


        if (by == null || by.isBlank()) {
            log.warn("Поиск отклонён: by пустой или null");
            throw new ValidationException("Параметр by не может быть пустым");
        }


        String[] byParts = by.split(",");
        boolean hasTitle = false;
        boolean hasDirector = false;

        for (String part : byParts) {
            String trimmed = part.trim().toLowerCase();
            if ("title".equals(trimmed)) {
                hasTitle = true;
            } else if ("director".equals(trimmed)) {
                hasDirector = true;
            } else {
                log.warn("Поиск отклонён: недопустимое значение в by = {}", trimmed);
                throw new ValidationException(
                        "Параметр by должен содержать 'title' и/или 'director'. Получено: " + by
                );
            }
        }


        if (!hasTitle && !hasDirector) {
            log.warn("Поиск отклонён: by не содержит допустимых значений");
            throw new ValidationException(
                    "Параметр by должен содержать 'title' и/или 'director'"
            );
        }


        String normalizedBy;
        if (hasTitle && hasDirector) {
            normalizedBy = "both";
        } else if (hasTitle) {
            normalizedBy = "title";
        } else {
            normalizedBy = "director";
        }

        log.info("Поиск фильмов: query={}, by={}, normalizedBy={}", query, by, normalizedBy);
        return likesStorage.searchFilms(query, normalizedBy);
    }

    public Film findById(Long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> {
                    log.warn("Фильм с id {} не найден", id);
                    return new NotFoundException("Фильм с ID " + id + " не найден");
                });
    }

    public Collection<Film> findCommonFilms(Long userId, Long friendId) {
        validateService.validateUserExists(userId, "Ошибка вывода: пользователь с ID {} не найден");
        validateService.validateUserExists(friendId, "Ошибка вывода: друг с ID {} не найден");
        return likesStorage.findCommonFilms(userId, friendId);
    }

    public Collection<Film> findFilmsByDirector(Long directorId, String sortBy) {

        if (!directorStorage.existsById(directorId)) {
            throw new NotFoundException("Режиссёр с ID " + directorId + " не найден");
        }

        if (!"likes".equals(sortBy) && !"year".equals(sortBy)) {
            throw new ValidationException("Параметр sortBy должен быть 'likes' или 'year'");
        }

        log.info("Поиск фильмов режиссёра id={}, сортировка={}", directorId, sortBy);
        return likesStorage.findFilmsByDirectorSorted(directorId, sortBy);
    }

    public void deleteFilmById(Long id) {
        validateService.validateFilmExist(id, "Фильм с id {} при удалении пользователя не найден");
        filmStorage.deleteById(id);
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

    private void validateDirectorsExist(Film film) {
        if (film.getDirectors() == null || film.getDirectors().isEmpty()) {
            return;
        }
        List<Long> directorId = film.getDirectors().stream()
                .map(Director::getId)
                .collect(Collectors.toList());

        List<Director> existingDirector = directorStorage.findById(directorId);

        if (existingDirector.size() != directorId.size()) {
            log.warn("Some directors not found: expected {}, found {}",
                    directorId.size(), existingDirector.size());
            throw new NotFoundException("Режиссёры не найдены");
        }


    }

}
