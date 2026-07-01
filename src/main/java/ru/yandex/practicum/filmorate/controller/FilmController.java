package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/film")
public class FilmController {
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film createNewFilm(@Valid @RequestBody Film film) {

        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.warn("Ошибка создания фильма: дата релиза фильма некорректная");
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        film.setId(getNextId());
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
            throw new NotFoundException("Фильм с id: " + newFilm.getId() + " не найден");
        }
        if (newFilm.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.warn("Ошибка обновления фильма: дата релиза фильма некорректна");
            throw new ValidationException("Дата релиза - не раньше 28 декабря 1895 года");
        }

        Film oldFilm = films.get(newFilm.getId());
        oldFilm.setName(newFilm.getName());
        oldFilm.setDescription(newFilm.getDescription());
        oldFilm.setReleaseDate(newFilm.getReleaseDate());
        oldFilm.setDuration(newFilm.getDuration());

        log.info("Обновлён фильм с id = {}", oldFilm.getId());
        return oldFilm;
    }


    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
