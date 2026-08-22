package ru.yandex.practicum.filmorate.dao.reviewLike;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mappers.ReviewLikeRowMapper;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ReviewLikeDbStorage implements ReviewLikeStorage {

    private final JdbcTemplate jdbcTemplate;
    private final ReviewLikeRowMapper reviewLikeRowMapper;


    @Override
    public void addLike(Long reviewId, Long userId) {
        String sql = "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, reviewId, userId, true);
    }

    @Override
    public void addDislike(Long reviewId, Long userId) {
        String sql = "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, reviewId, userId, false);
    }

    @Override
    public void removeLike(Long reviewId, Long userId) {
        String sql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_like = true";
        jdbcTemplate.update(sql, reviewId, userId);
    }

    @Override
    public void removeDislike(Long reviewId, Long userId) {
        String sql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_like = false";
        jdbcTemplate.update(sql, reviewId, userId);
    }

    @Override
    public int getUsefulCount(Long reviewId) {
        String likesSql = "SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND is_like = true";
        Integer likes = jdbcTemplate.queryForObject(likesSql, Integer.class, reviewId);

        String dislikeSql = "SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND is_like = false";
        Integer dislike = jdbcTemplate.queryForObject(dislikeSql, Integer.class, reviewId);

        if (likes == null) {
            likes = 0;
        }
        if (dislike == null) {
            dislike = 0;
        }

        log.info("getUsefulCount: reviewId={}, likes={}, dislikes={}, useful={}",
                reviewId, likes, dislike, likes - dislike);
        return likes - dislike;
    }

    @Override
    public Boolean existsLike(Long reviewId, Long userId) {
        String sql = "SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND user_id = ? AND is_like = true";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, reviewId, userId);
        log.info("existsLike: reviewId={}, userId={}, count={}", reviewId, userId, count);
        return count != null && count > 0;
    }

    @Override
    public Boolean existsDislike(Long reviewId, Long userId) {
        String sql = "SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND user_id = ? AND is_like = false";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, reviewId, userId);
        log.info("existsDislike: reviewId={}, userId={}, count={}", reviewId, userId, count);
        return count != null && count > 0;
    }

}
