package ru.yandex.practicum.filmorate.validation.User;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

@Slf4j
public class ValidationUser {

    public static void validUniqueEmail(String email, Long id, Collection<User> users) {
        for (User u : users) {
            if (id != null && u.getId().equals(id)) {
                continue;
            }
            if (u.getEmail().equals(email)) {
                log.warn("Ошибка: попытка использовать существующий Email = {}", email);
                throw new ValidationException("Этот Email уже используется");
            }
        }
    }

    public static void setNameIsBlank(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }


}
