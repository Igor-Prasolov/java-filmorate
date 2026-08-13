package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.friendship.FriendshipStorage;
import ru.yandex.practicum.filmorate.dao.user.UserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.user.User;


import ru.yandex.practicum.filmorate.validation.User.ValidationUser;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserStorage userStorage;
    private final FriendshipStorage friendshipStorage;

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User findUserById(Long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID {} не найден", userId);
                    return new NotFoundException("Пользователь с ID " + userId + " не найден");
                });
    }

    public User create(User user) {
        ValidationUser.validUniqueEmail(user.getEmail(), null, userStorage.findAll());
        ValidationUser.setNameIsBlank(user);
        return userStorage.save(user);
    }

    public User update(User user) {
        if (user.getId() == null) {
            log.warn("Ошибка обновления: отсутствует ID");
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
        validateUserExists(userId);
        validateUserExists(friendId);
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить себя в друзья");
        }
        friendshipStorage.addFriend(userId, friendId);
        log.info("Пользователь {} добавил друга {}", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        validateUserExists(userId);
        validateUserExists(friendId);
        friendshipStorage.removeFriend(userId, friendId);
        log.info("Пользователи {} и {} больше не друзья", userId, friendId);
    }

    public Collection<User> findFriends(Long userId) {
        validateUserExists(userId);
        List<Long> friendIds = friendshipStorage.findFriendIds(userId);
        List<User> friends = new ArrayList<>();
        for (Long id : friendIds) {
            userStorage.findById(id).ifPresent(friends::add);
        }
        return friends;
    }

    public Collection<User> findCommonFriends(Long userId, Long otherId) {
        validateUserExists(userId);
        validateUserExists(otherId);
        List<Long> commonIds = friendshipStorage.findCommonFriendIds(userId, otherId);
        List<User> commonFriends = new ArrayList<>();
        for (Long id : commonIds) {
            userStorage.findById(id).ifPresent(commonFriends::add);
        }
        return commonFriends;
    }

    private void validateUserExists(Long userId) {
        if (!userStorage.existsById(userId)) {
            log.warn("Пользователь с ID {} не найден", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
    }
}
