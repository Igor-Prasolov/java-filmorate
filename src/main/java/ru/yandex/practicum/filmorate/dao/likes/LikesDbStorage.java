package ru.yandex.practicum.filmorate.dao.likes;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.film.FilmsDbStorage;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class LikesDbStorage implements LikesStorage {

    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;
    private final FilmsDbStorage filmsDbStorage;

    @Override
    public List<Film> findCommonFilms(Long userId, Long friendId) {
        String sql = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id,COUNT (l.user_id) as likes_count
                FROM films f
                INNER JOIN likes l ON f.id = l.film_id
                WHERE  f.id IN (
                       SELECT film_id FROM likes WHERE user_id = ?
                       INTERSECT
                       SELECT film_id FROM likes WHERE user_id = ?
                       )
                       GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id
                       ORDER BY likes_count DESC
                """;

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, userId, friendId);
        for (Film film : films) {
            filmsDbStorage.loadFilmMpaAndGenres(film);
        }
        return films;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(Long filmdId, Long userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmdId, userId);
    }

    @Override
    public int getLikesCount(Long filmId) {
        String sql = "SELECT COUNT(*) FROM likes WHERE film_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, filmId);
    }

    @Override
    public boolean existsLike(Long filmId, Long userId) {
        String sql = "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, filmId, userId);
        return count != null && count > 0;
    }

    @Override
    public List<Film> findPopularFilm(int limit) {
        String sql = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id
                FROM films f
                LEFT JOIN likes l ON f.id = l.film_id
                GROUP BY f.id
                ORDER BY COUNT(l.user_id) DESC
                LIMIT ?
                """;

        return jdbcTemplate.query(sql, filmRowMapper, limit);
    }

    @Override
    public List<Film> findFilmsByDirectorSorted(Long directorId, String sortBy) {
        String orderBy = "";

        if ("likes".equals(sortBy)) {
            orderBy = "ORDER BY likes_count DESC";
        } else if ("year".equals(sortBy)) {
            orderBy = "ORDER BY f.release_date DESC";
        } else {
            throw new ValidationException("Некорректный параметр sortBy. Используйте 'likes' или 'year'");
        }

        String sql = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id,
                       COUNT(l.user_id) as likes_count
                FROM films f
                JOIN film_directors fd ON f.id = fd.film_id
                LEFT JOIN likes l ON f.id = l.film_id
                WHERE fd.director_id = ?
                GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id
                """ + orderBy;

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, directorId);

        for (Film film : films) {
            filmsDbStorage.loadFilmMpaAndGenres(film);
        }

        return films;
    }

}
