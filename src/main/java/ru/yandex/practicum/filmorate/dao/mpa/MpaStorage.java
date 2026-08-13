package ru.yandex.practicum.filmorate.dao.mpa;

import ru.yandex.practicum.filmorate.model.film.MPA;

import java.util.Collection;
import java.util.Optional;

public interface MpaStorage {

    Collection<MPA> findAll();

    Optional<MPA> findById(Long id);

    boolean existsById(Long id);
}
