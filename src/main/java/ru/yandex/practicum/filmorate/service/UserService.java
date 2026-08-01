package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.user.Friendship;
import ru.yandex.practicum.filmorate.model.user.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import ru.yandex.practicum.filmorate.validation.User.ValidationUser;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final Map<Long, Set<Friendship>> friends = new HashMap<>();

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

        Friendship f1 = new Friendship();
        f1.setUserId(userId);
        f1.setFriendId(friendId);
        f1.setStatus(FriendshipStatus.PENDING);

        Friendship f2 = new Friendship();
        f2.setUserId(friendId);
        f2.setFriendId(userId);
        f2.setStatus(FriendshipStatus.PENDING);

        friends.computeIfAbsent(userId, k -> new HashSet<>()).add(f1);
        friends.computeIfAbsent(friendId, k -> new HashSet<>()).add(f2);
        log.info("Пользователь {} отправил запрос на добавление в друзья пользователю {}", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            log.warn("Ошибка удаления из друзей: пользователь удаляет сам себя");
            throw new ValidationException("Вы пытаетесь удалить самого себя из друзей");
        }
        validateUserExists(userId, "Ошибка удаления из друзей: пользователь с ID {} не найден");
        validateUserExists(friendId, "Ошибка удаления из друзей: пользователь с ID {} не найден");

        Set<Friendship> userFriends = friends.get(userId);
        if (userFriends != null) {
            for (Friendship f : userFriends) {
                if (f.getFriendId().equals(friendId)) {
                    userFriends.remove(f);
                    break;
                }
            }
        }
        Set<Friendship> friendFriends = friends.get(friendId);
        if (friendFriends != null) {
            for (Friendship f : friendFriends) {
                if (f.getFriendId().equals(userId)) {
                    friendFriends.remove(f);
                    break;
                }
            }
        }
        log.info("Пользователи {} и {} больше не друзья", userId, friendId);
    }

    public Collection<User> findFriends(Long userId) {
        validateUserExists(userId, "Ошибка поиска друга: пользователь с ID {} не найден");

        Set<Friendship> friendIds = friends.getOrDefault(userId, new HashSet<>());
        List<User> result = new ArrayList<>();
        for (Friendship f : friendIds) {
            Optional<User> user = userStorage.findById(f.getFriendId());
            user.ifPresent(result::add);
        }
        return result;
    }

    public Collection<User> findCommonFriends(Long userId, Long otherId) {
        validateUserExists(userId, "Ошибка поиска общих друзей: пользователь с ID {} не найден");
        validateUserExists(otherId, "Ошибка поиска общих друзей: пользователь с ID {} не найден");

        Set<Friendship> userFriends = friends.getOrDefault(userId, new HashSet<>());
        Set<Friendship> otherFriends = friends.getOrDefault(otherId, new HashSet<>());

        Set<Long> userFriendId = new HashSet<>();
        for (Friendship f : userFriends) {
            userFriendId.add(f.getFriendId());
        }

        Set<Long> otherFriendId = new HashSet<>();
        for (Friendship f : otherFriends) {
            otherFriendId.add(f.getFriendId());
        }

        Set<Long> commonIds = new HashSet<>(userFriendId);
        commonIds.retainAll(otherFriendId);

        List<User> result = new ArrayList<>();
        for (Long id : commonIds) {
            Optional<User> user = userStorage.findById(id);
            user.ifPresent(result::add);
        }
        return result;
    }

    public void approveFriendRequest(Long userId, Long friendId) {
        validateUserExists(userId, "Ошибка подтверждения запроса в друзья: пользователь с ID {} не найден");
        validateUserExists(friendId, "Ошибка подтверждения запроса в друзья: пользователь с ID {} не найден");

        Set<Friendship> userFriends = friends.get(userId);
        if (userFriends != null) {
            for (Friendship f : userFriends) {
                if (f.getFriendId().equals(friendId)) {
                    if (f.getStatus() == FriendshipStatus.PENDING) {
                        f.setStatus(FriendshipStatus.CONFIRMED);
                    }
                }
            }
        }

        Set<Friendship> friendFrends = friends.get(friendId);
        if (friendFrends != null) {
            for (Friendship f : friendFrends) {
                if (f.getFriendId().equals(userId)) {
                    if (f.getStatus() == FriendshipStatus.PENDING) {
                        f.setStatus(FriendshipStatus.CONFIRMED);
                    }
                }
            }
        }

        log.info("Пользователь {} подтвердил дружбу с пользователем {}", userId, friendId);
    }

    private void validateUserExists(Long userId, String logMessage) {
        Optional<User> user = userStorage.findById(userId);
        if (user.isEmpty()) {
            log.warn(logMessage, userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
    }
}
