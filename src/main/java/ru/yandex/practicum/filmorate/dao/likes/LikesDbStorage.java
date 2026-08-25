package ru.yandex.practicum.filmorate.dao.likes;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.film.FilmsDbStorage;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
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
    public boolean existsLike(Long filmId, Long userId) {
        String sql = "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, filmId, userId);
        return count != null && count > 0;
    }

    @Override
    public List<Film> searchFilms(String query, String by) {
        String searchPattern = "%" + query.toLowerCase() + "%";
        String sql;
        List<Film> films;

        if ("title".equals(by)) {
            sql = """
                    SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id,
                           COUNT(l.user_id) as likes_count
                    FROM films f
                    LEFT JOIN likes l ON f.id = l.film_id
                    WHERE LOWER(f.name) LIKE ?
                    GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id
                    ORDER BY likes_count DESC
                    """;
            films = jdbcTemplate.query(sql, filmRowMapper, searchPattern);

        } else if ("director".equals(by)) {
            sql = """
                    SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id,
                           COUNT(l.user_id) as likes_count
                    FROM films f
                    JOIN film_directors fd ON f.id = fd.film_id
                    JOIN directors d ON fd.director_id = d.id
                    LEFT JOIN likes l ON f.id = l.film_id
                    WHERE LOWER(d.name) LIKE ?
                    GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id
                    ORDER BY likes_count DESC
                    """;
            films = jdbcTemplate.query(sql, filmRowMapper, searchPattern);

        } else {
            sql = """
                    SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id,
                           COUNT(l.user_id) as likes_count
                    FROM films f
                    LEFT JOIN film_directors fd ON f.id = fd.film_id
                    LEFT JOIN directors d ON fd.director_id = d.id
                    LEFT JOIN likes l ON f.id = l.film_id
                    WHERE LOWER(f.name) LIKE ?
                       OR LOWER(d.name) LIKE ?
                    GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id
                    ORDER BY likes_count DESC
                    """;
            films = jdbcTemplate.query(sql, filmRowMapper, searchPattern, searchPattern);
        }

        for (Film film : films) {
            filmsDbStorage.loadFilmMpaAndGenres(film);
        }


        return films;
    }

    @Override
    public List<Film> findPopularFilm(int limit, Long genreId, Integer year) {
        StringBuilder sql = new StringBuilder("""
                SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id
                FROM films AS f
                LEFT JOIN likes AS l ON f.id = l.film_id
                """);

        if (genreId != null) {
            sql.append("JOIN film_genre AS fg ON f.id = fg.film_id ");
        }
        sql.append("WHERE 1=1");

        if (genreId != null) {
            sql.append("AND fg.genre_id = ?");
        }
        if (year != null) {
            sql.append("AND EXTRACT(YEAR FROM f.release_date) = ?");
        }
        sql.append("""
                GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id
                ORDER BY COUNT(l.user_id) DESC
                LIMIT ?
                """);

        List<Object> params = new ArrayList<>();
        if (genreId != null) {
            params.add(genreId);
        }
        if (year != null) {
            params.add(year);
        }
        params.add(limit);

        List<Film> filmList = jdbcTemplate.query(sql.toString(), filmRowMapper, params.toArray());
        for (Film film : filmList) {
            filmsDbStorage.loadFilmMpaAndGenres(film);
        }
        return filmList;

    }

    @Override
    public List<Film> findFilmsByDirectorSorted(Long directorId, String sortBy) {
        String orderBy = "";

        if ("likes".equals(sortBy)) {
            orderBy = "ORDER BY likes_count DESC";
        } else if ("year".equals(sortBy)) {
            orderBy = "ORDER BY f.release_date ASC";
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

    @Override
    public List<Film> findRecommendations(Long userId) {
        String sql = """
                SELECT f.id,
                       f.name,
                       f.description,
                       f.release_date,
                       f.duration,
                       f.mpa_id
                FROM films f
                JOIN likes l ON l.film_id = f.id
                WHERE l.user_id = (
                    SELECT l2.user_id
                    FROM likes l1
                    JOIN likes l2 ON l1.film_id = l2.film_id
                    WHERE l1.user_id = ?
                      AND l2.user_id <> ?
                    GROUP BY l2.user_id
                    ORDER BY COUNT(*) DESC, l2.user_id
                    LIMIT 1
                )
                AND NOT EXISTS (
                    SELECT 1
                    FROM likes user_likes
                    WHERE user_likes.user_id = ?
                      AND user_likes.film_id = f.id
                )
                ORDER BY f.id
                """;

        List<Film> films = jdbcTemplate.query(
                sql,
                filmRowMapper,
                userId,
                userId,
                userId
        );

        for (Film film : films) {
            filmsDbStorage.loadFilmMpaAndGenres(film);
        }

        return films;
    }

}
