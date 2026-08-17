package ru.yandex.practicum.filmorate.dao.mpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.model.film.MPA;

import java.util.Collection;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;
    private final MpaRowMapper mpaRowMapper;

    @Override
    public Collection<MPA> findAll() {
        String sql = "SELECT * FROM mpa";
        return jdbcTemplate.query(sql, mpaRowMapper);
    }

    @Override
    public Optional<MPA> findById(Long id) {
        String sql = "SELECT * FROM mpa WHERE id = ?";
        try {
            MPA mpa = jdbcTemplate.queryForObject(sql, mpaRowMapper, id);
            return Optional.ofNullable(mpa);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            log.warn("Рейтинг с id {} не найден", id);
            return Optional.empty();
        }
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM mpa WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }
}
