package ru.yandex.practicum.filmorate.dao.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DirectorStorage {

    Collection<Director> findAll();

    Optional<Director> findById(Long id);

    Director save(Director director);

    Director update(Director director);

    void deleteById(Long id);

    boolean existsById(Long id);

    List<Director> findById(List<Long> id);// можно реализовать в интерфейсе для проверок на 404

}
