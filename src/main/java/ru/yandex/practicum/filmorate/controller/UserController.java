package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User crateNewUser(@Valid @RequestBody User user) {
        for (User u : users.values()) {
            if (u.getEmail().equals(user.getEmail())) {
                log.warn("Ошибка создания пользователя: такой Email уже сущуествует");
                throw new ValidationException("Этот Email уже используется");
            }
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        user.setId(getNextId());
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
            throw new NotFoundException("Пользователь с ID: " + newUser.getId() + " не найден");
        }
        for (User u : users.values()) {
            if (u.getId() != newUser.getId() && u.getEmail().equals(newUser.getEmail())) {
                log.warn("Ошибка обновления пользователя: повторяющиеся Email");
                throw new ValidationException("Этот Email уже используется");
            }
        }

        User oldUser = users.get(newUser.getId());

        if (newUser.getName() == null || newUser.getName().isBlank()) {
            newUser.setName(newUser.getLogin());
        }
        oldUser.setName(newUser.getName());
        oldUser.setBirthday(newUser.getBirthday());
        oldUser.setEmail(newUser.getEmail());
        oldUser.setLogin(newUser.getLogin());

        log.info("Обновлен пользователь с ID = {}", oldUser.getId());
        return oldUser;
    }


    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }


}
