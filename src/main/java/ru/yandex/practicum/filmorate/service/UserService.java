package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import ru.yandex.practicum.filmorate.validation.User.ValidationUser;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final Map<Long, Set<Long>> friends = new HashMap<>();

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User findUserById(Long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Ошибка поиска пользователя по ID: пользователь с ID {} не найден", userId);
                    throw new NotFoundException("Пользователь с ID " + userId + " не найден");
                });
    }

    public User create(User user) {
        ValidationUser.validUniqueEmail(user.getEmail(), null, userStorage.findAll());
        ValidationUser.setNameIsBlank(user);
        return userStorage.save(user);
    }

    public User update(User user) {
        if (user.getId() == null) {
            log.warn("Ошибка обновления пользователя: отсутствует ID");
            throw new ValidationException("ID не может быть пустым");
        }
        validateUserExists(user.getId(), "Ошибка обновления пользователя: пользователь с ID {} не найден");

        ValidationUser.validUniqueEmail(user.getEmail(), user.getId(), userStorage.findAll());
        ValidationUser.setNameIsBlank(user);
        return userStorage.update(user);
    }

    public void addFriend(Long userId, Long friendId) {
        validateUserExists(userId,
                "Ошибка добавления в друзья пользователя: пользователь с ID {} не найден");
        validateUserExists(friendId,
                "Ошибка добавления в друзья пользователя: пользователь с ID {} не найден");
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить себя в друзья");
        }

        friends.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);
        friends.computeIfAbsent(friendId, k -> new HashSet<>()).add(userId);
        log.info("Пользователи {} и {} стали друзьями", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            log.warn("Ошибка удаления из друзей: пользователь удаляет сам себя");
            throw new ValidationException("Вы пытаетесь удалить самого себя из друзей");
        }
        validateUserExists(userId, "Ошибка удаления из друзей: пользователь с ID {} не найден");
        validateUserExists(friendId, "Ошибка удаления из друзей: пользователь с ID {} не найден");

        Set<Long> userFriends = friends.get(userId);
        if (userFriends != null) {
            userFriends.remove(friendId);
        }
        Set<Long> friendFriends = friends.get(friendId);
        if (friendFriends != null) {
            friendFriends.remove(userId);
        }
        log.info("Пользователи {} и {} больше не друзья", userId, friendId);
    }

    public Collection<User> findFriends(Long userId) {
        validateUserExists(userId, "Ошибка поиска друга: пользователь с ID {} не найден");

        Set<Long> friendIds = friends.getOrDefault(userId, new HashSet<>());
        List<User> result = new ArrayList<>();
        for (Long id : friendIds) {
            Optional<User> user = userStorage.findById(id);
            user.ifPresent(result::add);
        }
        return result;
    }

    public Collection<User> findCommonFriends(Long userId, Long otherId) {
        validateUserExists(userId, "Ошибка поиска общих друзей: пользователь с ID {} не найден");
        validateUserExists(otherId, "Ошибка поиска общих друзей: пользователь с ID {} не найден");

        Set<Long> userFriends = friends.getOrDefault(userId, new HashSet<>());
        Set<Long> otherFriends = friends.getOrDefault(otherId, new HashSet<>());

        Set<Long> commonIds = new HashSet<>(userFriends);
        commonIds.retainAll(otherFriends);

        List<User> result = new ArrayList<>();
        for (Long id : commonIds) {
            Optional<User> user = userStorage.findById(id);
            user.ifPresent(result::add);
        }
        return result;
    }

    private void validateUserExists(Long userId, String logMessage) {
        Optional<User> user = userStorage.findById(userId);
        if (user.isEmpty()) {
            log.warn(logMessage, userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
    }
}
