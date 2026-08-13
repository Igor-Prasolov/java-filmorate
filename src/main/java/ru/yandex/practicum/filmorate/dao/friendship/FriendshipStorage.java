package ru.yandex.practicum.filmorate.dao.friendship;

import java.util.List;

public interface FriendshipStorage {
    void addFriend(Long userId, Long friendId);

    void removeFriend(Long userId, Long friendId);

    List<Long> findFriendIds(Long userId);

    List<Long> findCommonFriendIds(Long userId, Long otherId);

    boolean areFriends(Long userId, Long friendId);
}
