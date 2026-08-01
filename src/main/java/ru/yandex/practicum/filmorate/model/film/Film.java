package ru.yandex.practicum.filmorate.model.film;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;


@Data
public class Film {
    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания - 200")
    private String description;

    @NotNull(message = "Дата релиза не указана")
    @PastOrPresent(message = "Дата релиза некорректна")
    private LocalDate releaseDate;

    @NotNull(message = "Продолжительность фильма не указана")
    @Positive(message = "Продолжительность фильма должна быть положительной")
    private Integer duration;


    private Set<Genre> genres;

    private MPA mpa;

}
