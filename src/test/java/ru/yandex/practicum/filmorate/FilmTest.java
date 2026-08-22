package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ru.yandex.practicum.filmorate.model.Film;


import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilmTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }


    @Test
    void filmShouldBeValid() {
        Film film = new Film();
        film.setName("Начало");
        film.setDescription("Отличный фильм");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);
        film.setGenres(new HashSet<>());

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }


    @Test
    void filmNameShouldNotBeBlank() {
        Film film = new Film();
        film.setName("");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }


    @Test
    void filmDescriptionShouldNotExceed200Chars() {
        Film film = new Film();
        film.setName("Начало");
        film.setDescription("A".repeat(201));
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }


    @Test
    void filmReleaseDateShouldBePresent() {
        Film film = new Film();
        film.setName("Начало");
        film.setDuration(148);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }


    @Test
    void filmDurationShouldBePositive() {
        Film film = new Film();
        film.setName("Начало");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(-10);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }
}
