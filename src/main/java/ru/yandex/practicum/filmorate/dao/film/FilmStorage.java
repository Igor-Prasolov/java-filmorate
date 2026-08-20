package ru.yandex.practicum.filmorate.dao.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Collection<Film> findAll();

    Optional<Film> findById(Long id);

    Film save(Film film);

    Film update(Film film);

    void deleteById(Long id);

    boolean existsById(Long id);
}
