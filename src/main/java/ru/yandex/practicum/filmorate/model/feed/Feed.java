package ru.yandex.practicum.filmorate.model.feed;

import lombok.Data;

//   Класс для функционала событий
@Data
public class Feed {
    private Long eventId;
    private Long userId;
    private EventType eventType;
    private EventOperation operation;
    private Long entityId;
    private Long timestamp;
}
