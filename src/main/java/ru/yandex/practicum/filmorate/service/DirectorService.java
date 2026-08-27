package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.director.DirectorStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.validation.Director.ValidationDirector;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class DirectorService {

    private final DirectorStorage directorStorage;

    public Collection<Director> findAll() {
        return directorStorage.findAll();
    }

    public Director findById(Long id) {
        return directorStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Режиссёр с id " + id + " не найден"));
    }

    public Director create(Director director) {

        ValidationDirector.validateName(director);

        Director saved = directorStorage.save(director);

        log.info("Director created: name={}, id={}", saved.getName(), saved.getId());
        return saved;
    }

    public Director update(Director director) {

        ValidationDirector.validateId(director.getId());
        ValidationDirector.validateName(director);
        ValidationDirector.validateExists(director.getId(), directorStorage.existsById(director.getId()));

        return directorStorage.update(director);
    }

    public void delete(Long id) {
        ValidationDirector.validateId(id);
        ValidationDirector.validateExists(id, directorStorage.existsById(id));
        directorStorage.deleteById(id);
    }
}
