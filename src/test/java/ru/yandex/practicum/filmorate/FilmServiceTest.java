package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class FilmServiceTest {

    private FilmService filmService;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(new InMemoryUserStorage());
        filmService = new FilmService(new InMemoryFilmStorage(), userService);
    }

    private Film createFilm(String name, String description, LocalDate releaseDate, int duration) {
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(releaseDate);
        film.setDuration(duration);
        return filmService.create(film);
    }

    private User createUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userService.create(user);
    }

    @Test
    void shouldAddLike() {
        Film film = createFilm("Film", "Desc", LocalDate.of(2020, 1, 1), 120);
        User user = createUser("user@mail.ru", "login");

        filmService.addLike(film.getId(), user.getId());

        Collection<Film> popular = filmService.findPopularFilms(10);
        assertFalse(popular.isEmpty());
        assertEquals(film, popular.iterator().next());
    }

    @Test
    void shouldThrowIfAddLikeFilmNotFound() {
        User user = createUser("user@mail.ru", "login");
        assertThrows(NotFoundException.class, () -> filmService.addLike(999L, user.getId()));
    }

    @Test
    void shouldThrowIfAddLikeUserNotFound() {
        Film film = createFilm("Film", "Desc", LocalDate.of(2020, 1, 1), 120);
        assertThrows(NotFoundException.class, () -> filmService.addLike(film.getId(), 999L));
    }

    @Test
    void shouldRemoveLike() {
        Film film = createFilm("Film", "Desc", LocalDate.of(2020, 1, 1), 120);
        User user = createUser("user@mail.ru", "login");

        filmService.addLike(film.getId(), user.getId());
        filmService.removeLike(film.getId(), user.getId());

        Collection<Film> popular = filmService.findPopularFilms(10);
        assertTrue(popular.isEmpty());
    }

    @Test
    void shouldThrowIfRemoveLikeFilmNotFound() {
        User user = createUser("user@mail.ru", "login");
        assertThrows(NotFoundException.class, () -> filmService.removeLike(999L, user.getId()));
    }

    @Test
    void shouldThrowIfRemoveLikeUserNotFound() {
        Film film = createFilm("Film", "Desc", LocalDate.of(2020, 1, 1), 120);
        assertThrows(NotFoundException.class, () -> filmService.removeLike(film.getId(), 999L));
    }

    @Test
    void shouldReturnPopularFilms() {
        Film film1 = createFilm("Film1", "Desc", LocalDate.of(2020, 1, 1), 120);
        Film film2 = createFilm("Film2", "Desc", LocalDate.of(2020, 1, 1), 120);
        User user1 = createUser("user1@mail.ru", "login1");
        User user2 = createUser("user2@mail.ru", "login2");

        filmService.addLike(film1.getId(), user1.getId());
        filmService.addLike(film1.getId(), user2.getId());
        filmService.addLike(film2.getId(), user1.getId());

        Collection<Film> popular = filmService.findPopularFilms(10);

        assertEquals(2, popular.size());
        assertEquals(film1, popular.iterator().next());
    }

    @Test
    void shouldReturnEmptyListIfNoFilms() {
        Collection<Film> popular = filmService.findPopularFilms(10);
        assertTrue(popular.isEmpty());
    }

    @Test
    void shouldReturnLimitedPopularFilms() {
        Film film1 = createFilm("Film1", "Desc", LocalDate.of(2020, 1, 1), 120);
        Film film2 = createFilm("Film2", "Desc", LocalDate.of(2020, 1, 1), 120);
        Film film3 = createFilm("Film3", "Desc", LocalDate.of(2020, 1, 1), 120);
        User user1 = createUser("user1@mail.ru", "login1");
        User user2 = createUser("user2@mail.ru", "login2");
        User user3 = createUser("user3@mail.ru", "login3");

        filmService.addLike(film1.getId(), user1.getId());
        filmService.addLike(film1.getId(), user2.getId());
        filmService.addLike(film2.getId(), user1.getId());
        filmService.addLike(film3.getId(), user1.getId());

        Collection<Film> popular = filmService.findPopularFilms(2);

        assertEquals(2, popular.size());
        assertTrue(popular.contains(film1));
        assertTrue(popular.contains(film2));
        assertFalse(popular.contains(film3));
    }
}
