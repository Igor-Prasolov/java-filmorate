package ru.yandex.practicum.filmorate.dao.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;


import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Repository
@RequiredArgsConstructor
@Slf4j
@Qualifier("filmsDbStorage")
public class FilmsDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;
    private final MpaRowMapper mpaRowMapper;
    private final GenreRowMapper genreRowMapper;
    private final DirectorRowMapper directorRowMapper;

    @Override
    public Collection<Film> findAll() {
        String sql = "SELECT * FROM films";

        List<Film> filmList = jdbcTemplate.query(sql, filmRowMapper);

        for (Film film : filmList) {
            loadFilmMpaAndGenres(film);
        }

        return filmList;
    }

    @Override
    public Optional<Film> findById(Long id) {
        String sql = "SELECT * FROM films WHERE id = ?";
        try {
            Film film = jdbcTemplate.queryForObject(sql, filmRowMapper, id);
            loadFilmMpaAndGenres(film);
            return Optional.of(film);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Ошибка: фильм с id {} не найден", id);
            return Optional.empty();
        }
    }

    private void saveGenres(Long filmId, Set<Genre> genres) {
        String sql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
        List<Genre> genreList = new ArrayList<>(genres);
        genreList.sort(Comparator.comparing(Genre::getId));
        List<Object[]> batchArgs = new ArrayList<>();
        for (Genre genre : genreList) {
            batchArgs.add(new Object[]{filmId, genre.getId()});
        }
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    private void saveDirector(Long filmId, Set<Director> directors) {
        String sql = "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)";
        List<Object[]> batchArgs = new ArrayList<>();
        for (Director director : directors) {
            batchArgs.add(new Object[]{filmId, director.getId()});
        }
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    @Override
    public Film save(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setObject(5, extractMpaId(film));
            return ps;
        }, keyHolder);

        Long filmId = keyHolder.getKey().longValue();
        film.setId(filmId);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            saveGenres(filmId, film.getGenres());
        }

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            saveDirector(filmId, film.getDirectors());
        }
        return film;
    }

    @Override
    public Film update(Film film) {
        String sql = """
                UPDATE films
                SET name = ?,
                    description = ?,
                    release_date = ?,
                    duration = ?,
                    mpa_id = ?
                WHERE id = ?
                """;
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                extractMpaId(film),
                film.getId()
        );

        String deleteSql = "DELETE FROM film_genre WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, film.getId());

        String deleteDirectorSql = "DELETE FROM film_directors WHERE film_id = ?";
        jdbcTemplate.update(deleteDirectorSql, film.getId());


        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            saveDirector(film.getId(), film.getDirectors());
        }


        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            saveGenres(film.getId(), film.getGenres());
        }

        return film;
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM films WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM films WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    public void loadFilmMpaAndGenres(Film film) {
        Long mpaId = film.getMpaId();
        if (mpaId != null) {
            String mpaSql = "SELECT * FROM mpa WHERE id = ?";
            MPA mpa = jdbcTemplate.queryForObject(mpaSql, mpaRowMapper, mpaId);
            film.setMpa(mpa);
        }

        String genreSql = """
                SELECT g.id, g.name
                FROM genres AS g
                JOIN film_genre AS fg ON g.id = fg.genre_id
                WHERE fg.film_id = ?
                ORDER BY g.id
                """;
        List<Genre> genreList = jdbcTemplate.query(genreSql, genreRowMapper, film.getId());
        film.setGenres(new LinkedHashSet<>(genreList));

        String directorSql = """
                SELECT d.id, d.name
                FROM directors d
                JOIN film_directors fd ON d.id = fd.director_id
                WHERE fd.film_id = ?
                ORDER BY d.id
                """;
        List<Director> directorList = jdbcTemplate.query(directorSql, directorRowMapper, film.getId());
        film.setDirectors(new LinkedHashSet<>(directorList));

    }

    private Long extractMpaId(Film film) {
        if (film.getMpa() != null) {
            return film.getMpa().getId();
        }
        return null;
    }

}
