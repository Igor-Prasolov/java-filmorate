package ru.yandex.practicum.filmorate.dao.likes;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface LikesStorage {

    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);

    boolean existsLike(Long filmId, Long userId);

    List<Film> findCommonFilms(Long userId, Long friendId);

    List<Film> findFilmsByDirectorSorted(Long directorId, String sortBy);

    List<Film> searchFilms(String query, String by);

    List<Film> findRecommendations(Long userId);

    List<Film> findPopularFilm(int limit, Long genreId, Integer year);
}
