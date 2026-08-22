package ru.yandex.practicum.filmorate.dao.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mappers.ReviewRowMapper;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;
    private final ReviewRowMapper reviewRowMapper;


    @Override
    public Review create(Review review) {
        String sql = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setLong(3, review.getUserId());
            ps.setLong(4, review.getFilmId());
            ps.setInt(5, review.getUseful());
            return ps;
        }, keyHolder);

        Map<String, Object> keys = keyHolder.getKeys();
        Long reviewsId = (Long) keys.get("ID");
        review.setReviewId(reviewsId);

        return review;
    }


    @Override
    public Review update(Review review) {
        String sql = """
                UPDATE reviews
                SET content = ?,
                    is_positive = ?
                WHERE id = ?
                """;
        jdbcTemplate.update(sql,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId());

        return review;
    }


    @Override
    public void delete(Long reviewId) {
        String sql = "DELETE FROM reviews WHERE id = ?";
        jdbcTemplate.update(sql, reviewId);
    }


    @Override
    public Optional<Review> findById(Long reviewId) {
        String sql = "SELECT * FROM reviews WHERE id = ?";
        try {
            Review review = jdbcTemplate.queryForObject(sql, reviewRowMapper, reviewId);
            return Optional.ofNullable(review);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Ошибка: отзыв с id {} не найден", reviewId);
            return Optional.empty();
        }
    }


    @Override
    public List<Review> findAllByFilmId(Long filmId, int count) {
        if (count <= 0) {
            return Collections.emptyList();
        }
        if (filmId != null) {
            String sql = """
                            SELECT *
                            FROM reviews
                            WHERE film_id = ?
                            ORDER BY useful DESC
                            LIMIT ?
                            """;
            return jdbcTemplate.query(sql, reviewRowMapper, filmId, count);
        }

        String sql = "SELECT * FROM reviews ORDER BY useful DESC LIMIT ?";
        return jdbcTemplate.query(sql, reviewRowMapper, count);
    }

    @Override
    public Boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM reviews WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public void updateUseful(Long reviewId, int useful) {
        String sql = "UPDATE reviews SET useful = ? WHERE id = ?";
        jdbcTemplate.update(sql, useful, reviewId);
    }

}
