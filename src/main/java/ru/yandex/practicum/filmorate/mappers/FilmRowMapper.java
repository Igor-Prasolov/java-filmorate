package ru.yandex.practicum.filmorate.mappers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.MPA;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FilmRowMapper implements RowMapper<Film> {

    private final JdbcTemplate jdbcTemplate;
    private final MpaRowMapper mpaRowMapper;
    private final GenreRowMapper genreRowMapper;

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        Long mpaId = rs.getObject("mpa_id", Long.class);
        if (mpaId != null) {
            try {
                String mpaSql = "SELECT * FROM mpa WHERE id = ?";
                MPA mpa = jdbcTemplate.queryForObject(mpaSql, mpaRowMapper, mpaId);
                film.setMpa(mpa);
            } catch (EmptyResultDataAccessException e) {
                log.warn("MPA с id {} не найден", mpaId);
            }
        }

        String genresSql = """
            SELECT g.id, g.name
            FROM genres g
            JOIN film_genre fg ON g.id = fg.genre_id
            WHERE fg.film_id = ?
            ORDER BY g.id
            """;
        List<Genre> genres = jdbcTemplate.query(genresSql, genreRowMapper, film.getId());
        film.setGenres(new LinkedHashSet<>(genres));

        return film;
    }
}
