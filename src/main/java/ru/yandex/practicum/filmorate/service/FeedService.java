package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.feed.FeedStorage;
import ru.yandex.practicum.filmorate.model.feed.EventOperation;
import ru.yandex.practicum.filmorate.model.feed.EventType;
import ru.yandex.practicum.filmorate.model.feed.Feed;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedStorage feedStorage;
    private final ValidateService validateService;

    public void addEvent(Long userId, EventType eventType, EventOperation eventOperation, Long entityId) {
        Feed feed = new Feed();
        feed.setUserId(userId);
        feed.setEventType(eventType);
        feed.setOperation(eventOperation);
        feed.setEntityId(entityId);
        feed.setTimestamp(System.currentTimeMillis());

        feedStorage.addEvent(feed);
    }

    public Collection<Feed> getFeedByUser(Long userId) {
        validateService.validateUserExists(userId, "Пользователь с ID {} не найден при получении ленты");
        return feedStorage.getFeedByUser(userId);
    }
}
