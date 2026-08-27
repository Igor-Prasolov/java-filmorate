package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Review {
    private Long reviewId;

    @NotBlank(message = "Текст отзыва не может быть пустым")
    @Size(max = 200, message = "Текст отзыва не может превышать 200 символов")
    private String content;

    @NotNull(message = "Тип отзыва должен быть указан")
    private Boolean isPositive;
    private Long userId;
    private Long filmId;
    private Integer useful;
    private LocalDateTime createdAt;
}
