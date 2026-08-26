package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.film.FilmStorage;
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
    private final FilmStorage filmStorage;

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
        log.info("Пользователь создан: email={}, id={}", saved.getEmail(), saved.getId());
        return saved;
    }

    public User update(User user) {
        if (user.getId() == null) {
            log.warn("Ошибка обновления пользователя: userId is null");
            throw new ValidationException("ID не может быть пустым");
        }

        validateService.validateUserExists(user.getId(), "пользователь обновлен");
        validateUniqueEmail(user.getEmail(), user.getId(), "update");
        ValidationUser.setNameIsBlank(user);

        User updated = userStorage.update(user);
        log.info("Обновление пользователя: email={}, id={}", updated.getEmail(), updated.getId());
        return updated;
    }

    public void addFriend(Long userId, Long friendId) {
        validateService.validateUserExists(userId, "ошибка добавления в друзья");
        validateService.validateUserExists(friendId, "ошибка добавления в друзья");
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

        log.info("Друг добавлен: userId={}, friendId={}", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            log.warn("Ошибка удаления из друзей: userId == friendId, userId={}", friendId);
            throw new ValidationException("Нельзя удалить самого себя из друзей");
        }
        validateService.validateUserExists(userId, "Ошибка удаления из друзей");
        validateService.validateUserExists(friendId, "Ошибка удаления из друзей");
        friendshipStorage.removeFriend(userId, friendId);

        feedService.addEvent(
                userId,
                EventType.FRIEND,
                EventOperation.REMOVE,
                friendId
        );

        log.info("Друг удален: userId={}, friendId={}", userId, friendId);
    }

    public void removeUserById(Long userId) {
        validateService.validateUserExists(userId, "Пользователь c id {} при удалении по id не найден");
        userStorage.deleteById(userId);
    }

    public Collection<User> findFriends(Long userId) {
        validateService.validateUserExists(userId, "Ошибка в поиске друга");

        log.info("Поиск друга у  userId={}", userId);
        return friendshipStorage.findFriends(userId);
    }

    public Collection<User> findCommonFriends(Long userId, Long otherId) {
        if (userId.equals(otherId)) {
            log.warn("Ошибка поиска общих друзей: userId == otherId, userId={}", userId);
            throw new ValidationException("Нельзя искать общих друзей с самим собой");
        }
        validateService.validateUserExists(userId, "Ошибка поиска общих друзей");
        validateService.validateUserExists(otherId, "Ошибка поиска общих друзей");

        log.info("Поиск общих друзей: userId={}, otherId={}", userId, otherId);
        return friendshipStorage.findCommonFriend(userId, otherId);
    }


    private void validateUniqueEmail(String email, Long userId, String operation) {
        Optional<User> user = userStorage.findByEmail(email);
        if (user.isPresent() && !user.get().getId().equals(userId)) {
            log.warn("Email уже использовался: email={}, existingUserId={}, operation{}",
                    email, user.get().getId(), operation);
            throw new ValidationException("Этот Email уже используется");
        }
    }

    public Collection<Film> getRecommendations(Long userId) {
        validateService.validateUserExists(
                userId,
                "Ошибка получения рекомендаций: пользователь с ID {} не найден"
        );


        List<Film> films = likesStorage.findRecommendations(userId);
        for (Film film : films) {
            filmStorage.loadFilmMpaAndGenres(film);
        }
        return films;
    }

}
