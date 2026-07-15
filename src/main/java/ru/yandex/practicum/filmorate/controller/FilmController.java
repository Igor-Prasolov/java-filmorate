package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.utils.GeneratorID;
import ru.yandex.practicum.filmorate.validation.Film.ValidationFilms;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Запрос на получения всех фильмов");
        return films.values();
    }

    @PostMapping
    public Film createNewFilm(@Valid @RequestBody Film film) {

        ValidationFilms.validReleaseDate(film.getReleaseDate());
        film.setId(GeneratorID.getNextId(films));
        films.put(film.getId(), film);
        log.info("Создан новый фильм с id = {}", film.getId());
        return film;
    }


    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        if (newFilm.getId() == null) {
            log.warn("Ошибка обновления фильма: отсутсвует id");
            throw new ValidationException("Отсутсвует id");
        }
        if (!films.containsKey(newFilm.getId())) {
            log.warn("Ошибка обновления фильма: попытка несуществующего фильмы с ID = {}", newFilm.getId());
            throw new NotFoundException("Фильм с id: " + newFilm.getId() + " не найден");
        }

        ValidationFilms.validReleaseDate(newFilm.getReleaseDate());

        Film oldFilm = films.get(newFilm.getId());
        oldFilm.setName(newFilm.getName());
        oldFilm.setDescription(newFilm.getDescription());
        oldFilm.setReleaseDate(newFilm.getReleaseDate());
        oldFilm.setDuration(newFilm.getDuration());

        log.info("Обновлён фильм с id = {}", oldFilm.getId());
        return oldFilm;
    }

}
