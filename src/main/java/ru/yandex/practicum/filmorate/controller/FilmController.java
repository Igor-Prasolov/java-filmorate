package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;


@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Запрос на получения всех фильмов");
        return filmService.findAll();
    }

    @GetMapping("/popular")
    public Collection<Film> getPopularFilm(@RequestParam(required = false) Integer count) {
        log.info("Запрос на получение популярных фильмов");
        return filmService.findPopularFilms(count);
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        log.info("Запрос на получение фильма по id={}", id);
        return filmService.findById(id);
    }

    @GetMapping("/common")
    public Collection<Film> getCommonFilms(@RequestParam Long userId, @RequestParam Long friendId) {
        log.info("Запрос на получение общих фильмов");
        return filmService.findCommonFilms(userId, friendId);
    }

    @GetMapping("/director/{directorId}")
    public Collection<Film> getFilmsByDirector(
            @PathVariable Long directorId,
            @RequestParam String sortBy) {
        log.info("Запрос на получение фильмов режиссёра id={}, сортировка={}", directorId, sortBy);
        return filmService.findFilmsByDirector(directorId, sortBy);
    }

    @PostMapping
    public Film createNewFilm(@Valid @RequestBody Film film) {
        log.info("Запрос на создание нового фильма");
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        log.info("Запрос на обновление фильма", film);
        return filmService.update(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id,
                        @PathVariable Long userId) {
        log.info("Запрос на лайк фильма");
        filmService.addLike(id, userId);
    }

    @DeleteMapping("{id}/like/{userId}")
    public void removeLike(@PathVariable Long id,
                           @PathVariable Long userId) {
        log.info("Запрос на удаление лайка");
        filmService.removeLike(id, userId);
    }

}
