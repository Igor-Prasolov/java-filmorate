package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.user.User;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }


    @Test
    void userShouldBeValid() {
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("user123");
        user.setName("Иван");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }


    @Test
    void userEmailShouldNotBeBlank() {
        User user = new User();
        user.setEmail("");
        user.setLogin("user123");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }


    @Test
    void userEmailShouldContainAtSymbol() {
        User user = new User();
        user.setEmail("usermail.ru");
        user.setLogin("user123");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }


    @Test
    void userLoginShouldNotBeBlank() {
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }


    @Test
    void userLoginShouldNotContainSpaces() {
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("user 123");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }


    @Test
    void userBirthdayShouldNotBeFuture() {
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("user123");
        user.setBirthday(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }


}
