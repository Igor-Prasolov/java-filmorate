package ru.yandex.practicum.filmorate;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.feed.EventOperation;
import ru.yandex.practicum.filmorate.model.feed.EventType;
import ru.yandex.practicum.filmorate.model.feed.Feed;
import ru.yandex.practicum.filmorate.service.FeedService;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
public class FeedServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private FeedService feedService;

    @Autowired
    private FilmService filmService;

    @Test
    void shouldCreateFriendAddEvent() {
        User user = new User();
        user.setEmail("user1@gmail.com");
        user.setLogin("user1");
        user.setName("User One");
        user.setBirthday(LocalDate.of(1995, 1, 1));

        User savedUser = userService.create(user);

        User user2 = new User();
        user2.setEmail("user2@gmail.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1993, 1, 1));

        User savedFriend = userService.create(user2);

        userService.addFriend(savedUser.getId(), savedFriend.getId());

        Collection<Feed> events = feedService.getFeedByUser(savedUser.getId());

        assertEquals(1, events.size());
        Feed event = events.iterator().next();

        assertEquals(savedUser.getId(), event.getUserId());
        assertEquals(EventType.FRIEND, event.getEventType());
        assertEquals(EventOperation.ADD, event.getOperation());
        assertEquals(savedFriend.getId(), event.getEntityId());
        assertNotNull(event.getEventId());

    }

    @Test
    void shouldCreateFriendRemoveEvent() {
        User user = new User();
        user.setEmail("user1@gmail.com");
        user.setLogin("user1");
        user.setName("User One");
        user.setBirthday(LocalDate.of(1995, 1, 1));

        User savedUser = userService.create(user);

        User user2 = new User();
        user2.setEmail("user2@gmail.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1993, 1, 1));

        User savedFriend = userService.create(user2);

        userService.addFriend(savedUser.getId(), savedFriend.getId());
        userService.removeFriend(savedUser.getId(), savedFriend.getId());

        Collection<Feed> events = feedService.getFeedByUser(savedUser.getId());

        assertEquals(2, events.size());

        List<Feed> eventList = new ArrayList<>(events);
        Feed removeEvent = eventList.get(1);

        assertEquals(EventType.FRIEND, removeEvent.getEventType());
        assertEquals(EventOperation.REMOVE, removeEvent.getOperation());
        assertEquals(savedUser.getId(), removeEvent.getUserId());
        assertEquals(savedFriend.getId(), removeEvent.getEntityId());
        assertNotNull(removeEvent.getEventId());
    }

    @Test
    void shouldCreateLikeAddEvent() {
        User user = new User();
        user.setEmail("likeadd@gmail.com");
        user.setLogin("likeadd");
        user.setName("Like User");
        user.setBirthday(LocalDate.of(1995, 1, 1));

        User savedUser = userService.create(user);

        MPA mpa = new MPA();
        mpa.setId(1L);

        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setMpa(mpa);

        Film savedFilm = filmService.create(film);

        filmService.addLike(savedFilm.getId(), savedUser.getId());

        Collection<Feed> events = feedService.getFeedByUser(savedUser.getId());

        assertEquals(1, events.size());

        Feed event = events.iterator().next();

        assertEquals(savedUser.getId(), event.getUserId());
        assertEquals(EventType.LIKE, event.getEventType());
        assertEquals(EventOperation.ADD, event.getOperation());
        assertEquals(savedFilm.getId(), event.getEntityId());
        assertNotNull(event.getEventId());
        assertNotNull(event.getTimestamp());
    }

    @Test
    void shouldCreateLikeRemoveEvent() {
        User user = new User();
        user.setEmail("likeremove@gmail.com");
        user.setLogin("likeremove");
        user.setName("Like Remove User");
        user.setBirthday(LocalDate.of(1995, 1, 1));

        User savedUser = userService.create(user);

        MPA mpa = new MPA();
        mpa.setId(1L);

        Film film = new Film();
        film.setName("Remove Like Film");
        film.setDescription("Test description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setMpa(mpa);

        Film savedFilm = filmService.create(film);

        filmService.addLike(savedFilm.getId(), savedUser.getId());
        filmService.removeLike(savedFilm.getId(), savedUser.getId());

        Collection<Feed> events = feedService.getFeedByUser(savedUser.getId());

        assertEquals(2, events.size());

        List<Feed> eventList = new ArrayList<>(events);
        Feed removeEvent = eventList.get(1);

        assertEquals(savedUser.getId(), removeEvent.getUserId());
        assertEquals(EventType.LIKE, removeEvent.getEventType());
        assertEquals(EventOperation.REMOVE, removeEvent.getOperation());
        assertEquals(savedFilm.getId(), removeEvent.getEntityId());
        assertNotNull(removeEvent.getEventId());
        assertNotNull(removeEvent.getTimestamp());
    }

}
