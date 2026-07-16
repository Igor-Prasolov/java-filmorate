package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.validation.Film.ValidationFilms;

import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final Map<Long, Set<Long>> likes = new HashMap<>();

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        ValidationFilms.validReleaseDate(film.getReleaseDate());
        return filmStorage.save(film);
    }

    public Film update(Film film) {
        if (film.getId() == null) {
            log.warn("Ошибка обновления фильма: отсутствует id");
            throw new ValidationException("Отсутствует id");
        }
        if (!filmStorage.existsById(film.getId())) {
            log.warn("Ошибка обновления фильма: фильм с ID {} не найден", film.getId());
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }
        ValidationFilms.validReleaseDate(film.getReleaseDate());
        return filmStorage.update(film);
    }

    public void addLike(Long filmId, Long userId) {
        if (!filmStorage.existsById(filmId)) {
            log.warn("Ошибка лайка: фильм с ID {} не найден", filmId);
            throw new NotFoundException("Фильм не найден");
        }
        if (userService.findUserById(userId).isEmpty()) {
            log.warn("Ошибка лайка: пользователь с ID {} не найден", userId);
            throw new NotFoundException("Пользователь не найден");
        }

        likes.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        if (!filmStorage.existsById(filmId)) {
            log.warn("Ошибка удаления лайка: фильм с ID {} не найден", filmId);
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }
        if (userService.findUserById(userId).isEmpty()) {
            log.warn("Ошибка удаления лайка: пользователь с ID {} не найден", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        Set<Long> filmLikes = likes.get(filmId);
        if (filmLikes != null) {
            filmLikes.remove(userId);
            if (filmLikes.isEmpty()) {
                likes.remove(filmId);
            }
        }
        log.info("Пользователь {} убрал лайк с фильма {}", userId, filmId);
    }

    public Collection<Film> findPopularFilms(Integer count) {
        if (count == null || count <= 0) {
            count = 10;
        }

        return filmStorage.findAll().stream()
                .filter(film -> likes.getOrDefault(film.getId(), Set.of()).size() > 0)
                .sorted((f1, f2) -> {
                    int likes1 = likes.getOrDefault(f1.getId(), Set.of()).size();
                    int likes2 = likes.getOrDefault(f2.getId(), Set.of()).size();
                    return Integer.compare(likes2, likes1);
                })
                .limit(count)
                .collect(Collectors.toList());
    }
}
