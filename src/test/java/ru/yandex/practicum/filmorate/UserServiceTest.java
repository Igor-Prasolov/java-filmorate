package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        UserStorage userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);
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
    void shouldCreateUser() {
        User user = createUser("test@mail.ru", "login1");
        assertNotNull(user.getId());
        assertEquals("test@mail.ru", user.getEmail());
    }

    @Test
    void shouldThrowIfEmailDuplicate() {
        createUser("test@mail.ru", "login1");
        User duplicate = new User();
        duplicate.setEmail("test@mail.ru");
        duplicate.setLogin("login2");
        duplicate.setName("Name");
        duplicate.setBirthday(LocalDate.of(1990, 1, 1));

        assertThrows(ValidationException.class, () -> userService.create(duplicate));
    }

    @Test
    void shouldUpdateUser() {
        User user = createUser("test@mail.ru", "login1");
        User updated = new User();
        updated.setId(user.getId());
        updated.setEmail("new@mail.ru");
        updated.setLogin("newLogin");
        updated.setName("New Name");
        updated.setBirthday(LocalDate.of(2000, 1, 1));

        User result = userService.update(updated);
        assertEquals("new@mail.ru", result.getEmail());
        assertEquals("newLogin", result.getLogin());
        assertEquals("New Name", result.getName());
        assertEquals(LocalDate.of(2000, 1, 1), result.getBirthday());
    }

    @Test
    void shouldThrowIfUpdateUserNotFound() {
        User user = new User();
        user.setId(999L);
        assertThrows(NotFoundException.class, () -> userService.update(user));
    }

    @Test
    void shouldAddFriend() {
        User user1 = createUser("user1@mail.ru", "login1");
        User user2 = createUser("user2@mail.ru", "login2");

        userService.addFriend(user1.getId(), user2.getId());

        Collection<User> friends1 = userService.findFriends(user1.getId());
        Collection<User> friends2 = userService.findFriends(user2.getId());

        assertTrue(friends1.contains(user2));
        assertTrue(friends2.contains(user1));
        assertEquals(1, friends1.size());
        assertEquals(1, friends2.size());
    }

    @Test
    void shouldThrowIfAddFriendUserNotFound() {
        assertThrows(NotFoundException.class, () -> userService.addFriend(999L, 1L));
    }

    @Test
    void shouldThrowIfAddSelfAsFriend() {
        User user = createUser("user@mail.ru", "login");
        assertThrows(ValidationException.class, () -> userService.addFriend(user.getId(), user.getId()));
    }

    @Test
    void shouldRemoveFriend() {
        User user1 = createUser("user1@mail.ru", "login1");
        User user2 = createUser("user2@mail.ru", "login2");
        userService.addFriend(user1.getId(), user2.getId());

        userService.removeFriend(user1.getId(), user2.getId());

        Collection<User> friends1 = userService.findFriends(user1.getId());
        Collection<User> friends2 = userService.findFriends(user2.getId());

        assertTrue(friends1.isEmpty());
        assertTrue(friends2.isEmpty());
    }

    @Test
    void shouldReturnFriends() {
        User user1 = createUser("user1@mail.ru", "login1");
        User user2 = createUser("user2@mail.ru", "login2");
        userService.addFriend(user1.getId(), user2.getId());

        Collection<User> friends = userService.findFriends(user1.getId());

        assertEquals(1, friends.size());
        assertTrue(friends.contains(user2));
    }

    @Test
    void shouldReturnCommonFriends() {
        User user1 = createUser("user1@mail.ru", "login1");
        User user2 = createUser("user2@mail.ru", "login2");
        User common = createUser("common@mail.ru", "common");

        userService.addFriend(user1.getId(), common.getId());
        userService.addFriend(user2.getId(), common.getId());

        Collection<User> commonFriends = userService.findCommonFriends(user1.getId(), user2.getId());

        assertEquals(1, commonFriends.size());
        assertTrue(commonFriends.contains(common));
    }
}
