package ru.yandex.practicum.filmorate.dao.friendship;

import ru.yandex.practicum.filmorate.model.user.User;

import java.util.List;

public interface FriendshipStorage {
    void addFriend(Long userId, Long friendId);

    void removeFriend(Long userId, Long friendId);


    boolean areFriends(Long userId, Long friendId);

    public List<User> findFriends(Long userId);

    public List<User> findCommonFriend(Long userId, Long otherId);
}
