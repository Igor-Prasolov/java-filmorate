package ru.yandex.practicum.filmorate.dao.likes;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface LikesStorage {

    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);

    int getLikesCount(Long filmId);

    boolean existsLike(Long filmId, Long userId);

    List<Film> findPopularFilm(int limit);

    List<Film> findCommonFilms(Long userId, Long friendId);
}
