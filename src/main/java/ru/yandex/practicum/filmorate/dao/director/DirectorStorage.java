package ru.yandex.practicum.filmorate.dao.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.Optional;

public interface DirectorStorage {

    Collection<Director> findAll();

    Optional<Director> findById(Long id);

    Director create(Director director);

    Director update(Director director);

    void delete(Long id);

    boolean exists(Long id); // можно реализовать в интерфейсе для проверок на 404

}
