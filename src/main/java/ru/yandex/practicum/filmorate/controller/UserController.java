package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.utils.GeneratorID;
import ru.yandex.practicum.filmorate.validation.User.ValidationUser;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        log.info("Запрос на получения всех пользователей");
        return users.values();
    }

    @PostMapping
    public User crateNewUser(@Valid @RequestBody User user) {

        ValidationUser.validUniqueEmail(user.getEmail(), null, users);
        ValidationUser.setNameIsBlank(user);

        user.setId(GeneratorID.getNextId(users));
        users.put(user.getId(), user);
        log.info("Создан новый пользователь с id = {}", user.getId());

        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        if (newUser.getId() == null) {
            log.warn("Ошибка обновления пользователя: отсутсвует ID");
            throw new ValidationException("ID не может быть пустым");
        }
        if (!users.containsKey(newUser.getId())) {
            log.warn("Ошибка: попытка обновить несуществующего пользователя с ID = {}", newUser.getId());
            throw new NotFoundException("Пользователь с ID: " + newUser.getId() + " не найден");
        }

        ValidationUser.validUniqueEmail(newUser.getEmail(), newUser.getId(), users);

        User oldUser = users.get(newUser.getId());

        ValidationUser.setNameIsBlank(newUser);
        oldUser.setName(newUser.getName());
        oldUser.setBirthday(newUser.getBirthday());
        oldUser.setEmail(newUser.getEmail());
        oldUser.setLogin(newUser.getLogin());

        log.info("Обновлен пользователь с ID = {}", oldUser.getId());
        return oldUser;
    }

}
