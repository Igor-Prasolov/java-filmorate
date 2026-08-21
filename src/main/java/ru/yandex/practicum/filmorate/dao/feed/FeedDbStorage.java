package ru.yandex.practicum.filmorate.dao.feed;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.feed.EventOperation;
import ru.yandex.practicum.filmorate.model.feed.EventType;
import ru.yandex.practicum.filmorate.model.feed.Feed;

import java.util.Collection;

@Repository
@RequiredArgsConstructor
public class FeedDbStorage implements FeedStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void addEvent(Feed feed) {
        String sql = """
                INSERT INTO feed_events
                (user_id, event_type, operation, entity_id, timestamp)
                VALUES (?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                feed.getUserId(),
                feed.getEventType().name(),
                feed.getOperation().name(),
                feed.getEntityId(),
                feed.getTimestamp()
        );
    }

    @Override
    public Collection<Feed> getFeedByUser(Long userId) {
        String sql = """
                SELECT event_id,
                       user_id,
                       event_type,
                       operation,
                       entity_id,
                       timestamp
                FROM feed_events
                WHERE user_id = ?
                ORDER BY timestamp, event_id
                """;
        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    Feed feed = new Feed();
                    feed.setEventId(rs.getLong("event_id"));
                    feed.setUserId(rs.getLong("user_id"));
                    feed.setEventType(EventType.valueOf(rs.getString("event_type")));
                    feed.setOperation(EventOperation.valueOf(rs.getString("operation")));
                    feed.setEntityId(rs.getLong("entity_id"));
                    feed.setTimestamp(rs.getLong("timestamp"));
                    return feed;
                },
                userId

        );
    }
}
