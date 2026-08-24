package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.friendship.FriendshipStorage;
import ru.yandex.practicum.filmorate.dao.user.UserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.feed.EventOperation;
import ru.yandex.practicum.filmorate.model.feed.EventType;
import ru.yandex.practicum.filmorate.validation.User.ValidationUser;
import ru.yandex.practicum.filmorate.dao.likes.LikesStorage;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserStorage userStorage;
    private final FriendshipStorage friendshipStorage;
    private final ValidateService validateService;
    private final FeedService feedService;
    private final LikesStorage likesStorage;

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
        validateUniqueEmail(user.getEmail(), null, "create");
        ValidationUser.setNameIsBlank(user);
        User saved = userStorage.save(user);
        log.info("User created: email={}, id={}", saved.getEmail(), saved.getId());
        return saved;
    }

    public User update(User user) {
        if (user.getId() == null) {
            log.warn("Update failed: userId is null");
            throw new ValidationException("ID не может быть пустым");
        }

        validateService.validateUserExists(user.getId(), "update user");
        validateUniqueEmail(user.getEmail(), user.getId(), "update");
        ValidationUser.setNameIsBlank(user);

        User updated = userStorage.update(user);
        log.info("User updated: email={}, id={}", updated.getEmail(), updated.getId());
        return updated;
    }

    public void addFriend(Long userId, Long friendId) {
        validateService.validateUserExists(userId, "add friend");
        validateService.validateUserExists(friendId, "add friend");
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить себя в друзья");
        }
        friendshipStorage.addFriend(userId, friendId);

        feedService.addEvent(
                userId,
                EventType.FRIEND,
                EventOperation.ADD,
                friendId
        );

        log.info("Friend added: userId={}, friendId={}", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            log.warn("Remove friends failed: userId == friendId, userId={}", friendId);
        }
        validateService.validateUserExists(userId, "remove friend");
        validateService.validateUserExists(friendId, "remove friend");
        friendshipStorage.removeFriend(userId, friendId);

        feedService.addEvent(
                userId,
                EventType.FRIEND,
                EventOperation.REMOVE,
                friendId
        );

        log.info("Friend removed: userId={}, friendId={}", userId, friendId);
    }

    public void removeUserById(Long userId) {
        validateService.validateUserExists(userId, "Пользователь c id {} при удалении по id не найден");
        userStorage.deleteById(userId);
    }

    public Collection<User> findFriends(Long userId) {
        validateService.validateUserExists(userId, "find friends");

        log.info("Find friends for userId={}", userId);
        return friendshipStorage.findFriends(userId);
    }

    public Collection<User> findCommonFriends(Long userId, Long otherId) {
        if (userId.equals(otherId)) {
            log.warn("Find common friends failed: userId == otherId, userId={}", userId);
        }
        validateService.validateUserExists(userId, "find common friends");
        validateService.validateUserExists(otherId, "find common friends");

        log.info("Find common friends: userId={}, otherId={}", userId, otherId);
        return friendshipStorage.findCommonFriend(userId, otherId);
    }


    private void validateUniqueEmail(String email, Long userId, String operation) {
        Optional<User> user = userStorage.findByEmail(email);
        if (user.isPresent() && !user.get().getId().equals(userId)) {
            log.warn("Email already used: email={}, existingUserId={}, operation{}",
                    email, user.get().getId(), operation);
            throw new ValidationException("Этот Email уже используется");
        }
    }

    public Collection<Film> getRecommendations(Long userId) {
        validateService.validateUserExists(
                userId,
                "Ошибка получения рекомендаций: пользователь с ID {} не найден"
        );

        return likesStorage.findRecommendations(userId);
    }

}
