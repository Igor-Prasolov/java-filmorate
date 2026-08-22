package ru.yandex.practicum.filmorate.dao.director;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Repository
@RequiredArgsConstructor
@Slf4j
public class DirectorDbStorage implements DirectorStorage{

    private final JdbcTemplate jdbcTemplate;
    private final DirectorRowMapper directorRowMapper;

    @Override
    public Collection<Director> findAll() {
        String sql = "SELECT * FROM directors";
        return jdbcTemplate.query(sql, directorRowMapper);
    }



    @Override
    public Optional<Director> findById(Long id) {
        String sql = "SELECT * FROM directors WHERE id = ?";
        try {
            Director director = jdbcTemplate.queryForObject(sql, directorRowMapper, id);
            return Optional.ofNullable(director);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            log.warn("Режиссёр с id {} не найден", id);
            return Optional.empty();
        }
    }

    @Override
    public Director save(Director director) {
        String sql = "INSERT INTO directors (name) VALUES (?) ";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        Long directorId = keyHolder.getKey().longValue();
        director.setId(directorId);

        return director;
    }

    @Override
    public Director update(Director director) {
        String sql = """
                UPDATE directors
                SET name = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(sql, director.getName(), director.getId());

        return director;
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM directors WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM directors WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public List<Director> findById(List<Long> id) {
        if(id == null || id.isEmpty()){
            return Collections.emptyList();
        }

        String sql = "SELECT * FROM directors WHERE id IN (" +
                id.stream().map(String::valueOf).collect(Collectors.joining(",")) +
                ")";

        return jdbcTemplate.query(sql, directorRowMapper);
    }
}
