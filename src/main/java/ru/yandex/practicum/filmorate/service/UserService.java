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

    public Optional<User> findUserById(Long userId) {
        return userStorage.findById(userId);
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
        if (!userStorage.existsById(user.getId())) {
            log.warn("Пользователь с ID {} не найден", user.getId());
            throw new NotFoundException("Пользователь с ID " + user.getId() + " не найден");
        }

        ValidationUser.validUniqueEmail(user.getEmail(), user.getId(), userStorage.findAll());
        ValidationUser.setNameIsBlank(user);
        return userStorage.update(user);
    }

    public void addFriend(Long userId, Long friendId) {
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
        if (!userStorage.existsById(friendId)) {
            throw new NotFoundException("Пользователь с ID " + friendId + " не найден");
        }
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить себя в друзья");
        }

        friends.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);
        friends.computeIfAbsent(friendId, k -> new HashSet<>()).add(userId);
        log.info("Пользователи {} и {} стали друзьями", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
        if (!userStorage.existsById(friendId)) {
            throw new NotFoundException("Пользователь с ID " + friendId + " не найден");
        }

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
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        Set<Long> friendIds = friends.getOrDefault(userId, new HashSet<>());
        List<User> result = new ArrayList<>();
        for (Long id : friendIds) {
            userStorage.findById(id).ifPresent(result::add);
        }
        return result;
    }

    public Collection<User> findCommonFriends(Long userId, Long otherId) {
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
        if (!userStorage.existsById(otherId)) {
            throw new NotFoundException("Пользователь с ID " + otherId + " не найден");
        }

        Set<Long> userFriends = friends.getOrDefault(userId, new HashSet<>());
        Set<Long> otherFriends = friends.getOrDefault(otherId, new HashSet<>());

        Set<Long> commonIds = new HashSet<>(userFriends);
        commonIds.retainAll(otherFriends);

        List<User> result = new ArrayList<>();
        for (Long id : commonIds) {
            userStorage.findById(id).ifPresent(result::add);
        }
        return result;
    }
}
