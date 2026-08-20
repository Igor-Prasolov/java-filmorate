package ru.yandex.practicum.filmorate.dao.friendship;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class FriendshipDbStorage implements FriendshipStorage {

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    @Override
    public void addFriend(Long userId, Long friendId) {
        String sql = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);
        log.info("Пользователь {} добавил друга {}", userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        String sql = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
        log.info("Пользователь {} удалил друга {}", userId, friendId);
    }

    @Override
    public List<User> findFriends(Long userId) {
        String sql = """
                SELECT u.id, u.name, u.email, u.login, u.name, u.birthday
                FROM friends AS f
                JOIN users AS u ON f.friend_id = u.id
                WHERE f.user_id = ?
                """;
        return jdbcTemplate.query(sql, userRowMapper, userId);
    }

    @Override
    public List<User> findCommonFriend(Long userId, Long otherId) {
        String sql = """
            SELECT u.id, u.email, u.login, u.name, u.birthday
            FROM friends AS f1
            JOIN friends f2 ON f1.friend_id = f2.friend_id
            JOIN users AS u ON f1.friend_id = u.id
            WHERE f1.user_id = ? AND f2.user_id = ?
            """;
        return jdbcTemplate.query(sql, userRowMapper, userId, otherId);
    }

    @Override
    public boolean areFriends(Long userId, Long friendId) {
        String sql = "SELECT COUNT(*) FROM friends WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, friendId);
        return count != null && count > 0;
    }
}
