package ru.yandex.practicum.filmorate.model;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Director {
    private Long id;
    @NotNull(message = "Название не может быть пустым")
    private String name;
}
