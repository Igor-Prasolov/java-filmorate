package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class ReviewLike {
    private Long reviewId;
    private Long userId;
    private Boolean isLike;
}
