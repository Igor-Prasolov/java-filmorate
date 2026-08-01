package ru.yandex.practicum.filmorate.model.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class User {
    private Long id;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Email должен содержать символ @")
    private String email;

    @NotBlank(message = "Login не может быть пустым")
    @Pattern(regexp = "\\S+", message = "Login не может содержать пробелы")

    private String login;
    private String name;

    @PastOrPresent(message = "Дата рождения некорректна")
    private LocalDate birthday;

}
