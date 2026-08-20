package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("directors")
@RequiredArgsConstructor
public class DirectorController {

    // @GetMapping() - вернуть список всех режжисеров

    //@GetMapping("{id}") - вернуть режжисера по id, не забыть проверки на 404

    //@PostMapping - создать режжисера, не забыть проверят существует ли такой-же

    //@PutMapping() - изменить режжисера - не забыть проверку, что он существует

    //@DeleteMapping("/{id}") - удалить, не забыть проверку на его существование

}
