package ru.yandex.practicum.filmorate.dao.feed;

import ru.yandex.practicum.filmorate.model.feed.Feed;

import java.util.Collection;

public interface FeedStorage {

    void addEvent(Feed feed);

    Collection<Feed> getFeedByUser(Long userId);
}
