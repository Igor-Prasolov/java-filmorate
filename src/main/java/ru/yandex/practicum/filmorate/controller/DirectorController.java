package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {

    private final DirectorService directorService;

    @GetMapping
    public Collection<Director> findAll() {
        log.info("Запрос на получение всех режиссёров");
        return directorService.findAll();
    }// @GetMapping() - вернуть список всех режжисеров

    @GetMapping("/{id}")
    public Director getDirectorById(@PathVariable Long id) {
        log.info("Запрос на получение режиссёра по id={}", id);
        return directorService.findById(id);
    }//@GetMapping("{id}") - вернуть режжисера по id, не забыть проверки на 404

    @PostMapping
    public Director createNewDirector(@Valid @RequestBody Director director) {
        log.info("Запрос на создание нового режиссёра");
        return directorService.create(director);
    }//@PostMapping - создать режжисера, не забыть проверят существует ли такой-же

    @PutMapping
    public Director updateDirector(@Valid @RequestBody Director director) {
        log.info("Запрос на изменение режиссёра");
        return directorService.update(director);
    }//@PutMapping() - изменить режжисера - не забыть проверку, что он существует

    @DeleteMapping("/{id}")
    public void removeDirector(@PathVariable Long id) {
        log.info("Запрос на удаление режиссёра по id={}", id);
        directorService.delete(id);
    }//@DeleteMapping("/{id}") - удалить, не забыть проверку на его существование

}
