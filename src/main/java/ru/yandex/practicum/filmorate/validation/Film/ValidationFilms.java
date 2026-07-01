package ru.yandex.practicum.filmorate.validation.Film;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;

@Slf4j
public class ValidationFilms {

    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    public static void validReleaseDate(LocalDate releaseDate) {
        if (releaseDate != null && releaseDate.isBefore(MIN_RELEASE_DATE)) {
            log.warn("Ошибка создания фильма: дата релиза фильма некорректная");
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }

    }
}
