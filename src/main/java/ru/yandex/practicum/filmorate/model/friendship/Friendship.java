package ru.yandex.practicum.filmorate.model.friendship;

import lombok.Data;

@Data
public class Friendship {

    private Long id;
    private Long userId;
    private Long friendId;
    private FriendshipStatus status;

}
