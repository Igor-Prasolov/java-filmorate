package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dao.feed.FeedStorage;
import ru.yandex.practicum.filmorate.dao.user.UserStorage;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.feed.EventOperation;
import ru.yandex.practicum.filmorate.model.feed.EventType;
import ru.yandex.practicum.filmorate.model.feed.Feed;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
public class FeedDbStorageTest {

    @Autowired
    private FeedStorage feedStorage;

    @Autowired
    private UserStorage userStorage;

    @Test
    void shouldSaveAndReturnFeedEvent() {
        User user = new User();
        user.setEmail("test@gmail.com");
        user.setLogin("test");
        user.setName("bil");
        user.setBirthday(LocalDate.of(1996,12,11));

        User savedUser = userStorage.save(user);
        Feed feed = new Feed();
        feed.setUserId(savedUser.getId());
        feed.setEventType(EventType.FRIEND);
        feed.setOperation(EventOperation.ADD);
        feed.setEntityId(2L);
        feed.setTimestamp(System.currentTimeMillis());

        feedStorage.addEvent(feed);
        Collection<Feed> events = feedStorage.getFeedByUser(savedUser.getId());

        assertEquals(1, events.size());

        Feed savedEvent = events.iterator().next();

        assertEquals(savedUser.getId(), savedEvent.getUserId());
        assertEquals(EventType.FRIEND, savedEvent.getEventType());
        assertEquals(EventOperation.ADD, savedEvent.getOperation());
        assertEquals(2L, savedEvent.getEntityId());
        assertEquals(feed.getTimestamp(), savedEvent.getTimestamp());
        assertNotNull(savedEvent.getEventId());

    }
}
