package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.film.Film;

import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.validation.Film.ValidationFilms;

import java.util.*;


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
        validateFilmExist(film.getId(), "Ошибка обновления фильма: фильм с ID {} не найден");
        ValidationFilms.validReleaseDate(film.getReleaseDate());
        return filmStorage.update(film);
    }

    public void addLike(Long filmId, Long userId) {
        validateFilmExist(filmId, "Ошибка лайка: фильм с ID {} не найден");
        validateUserExists(userId, "Ошибка лайка: пользователь с ID {} не найден");

        likes.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        validateFilmExist(filmId, "Ошибка удаления лайка: фильм с ID {} не найден");
        validateUserExists(userId, "Ошибка удаления лайка: пользователь с ID {} не найден");

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

        List<Long> popular = likes.entrySet().stream()
                .sorted((l1, l2) ->
                        Integer.compare(l2.getValue().size(), l1.getValue().size()))
                .limit(count)
                .map(Map.Entry::getKey)
                .toList();

        List<Film> result = new ArrayList<>();
        for (Long id :popular) {
            Optional<Film> film = filmStorage.findById(id);
            film.ifPresent(result::add);
            }

        return result;
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
}
