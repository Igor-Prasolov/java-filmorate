package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.review.ReviewDbStorage;
import ru.yandex.practicum.filmorate.dao.reviewLike.ReviewLikeStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.feed.EventOperation;
import ru.yandex.practicum.filmorate.model.feed.EventType;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewDbStorage reviewDbStorage;
    private final ValidateService validateService;
    private final ReviewLikeStorage reviewLikeStorage;
    private final FeedService feedService;


    public Review create(Review review) {
        if (review.getUserId() == null) {
            throw new ValidationException("ID пользователя должен быть указан");
        }
        if (review.getFilmId() == null) {
            throw new ValidationException("ID фильма должен быть указан");
        }

        validateService.validateUserExists(review.getUserId(),
                "Пользователь с id {} не найден при создании отзыва");
        validateService.validateFilmExist(review.getFilmId(),
                "Фильм с id {} не найден при создании отзыва");

        review.setUseful(0);
        Review saved = reviewDbStorage.create(review);

        feedService.addEvent(
                saved.getUserId(),
                EventType.REVIEW,
                EventOperation.ADD,
                saved.getFilmId()
        );

        log.info("Пользователь с id {} написал отзыв с id {} на фильм с id {}",
                saved.getUserId(), saved.getReviewId(), saved.getFilmId());
        return saved;
    }


    public Review update(Review review) {
        validateReviewIdNotNull(review.getReviewId());

        validateService.validateUserExists(review.getUserId(),
                "Пользователь с id {} не найден при обновлении отзыва");
        validateService.validateFilmExist(review.getFilmId(),
                "Фильм с id {} не найден при обновлении отзыва");
        validateReviewExists(review.getReviewId(),
                "Отзыв с id {} не найден при обновлении");

        Review oldReview = reviewDbStorage.findById(review.getReviewId())
                .orElseThrow(() -> new NotFoundException("Отзыв с id " + review.getReviewId() + " не найден"));

        Review update = reviewDbStorage.update(review);

        feedService.addEvent(
                oldReview.getUserId(),
                EventType.REVIEW,
                EventOperation.UPDATE,
                oldReview.getFilmId()
        );

        log.info("Отзыв с id {} обновлен", update.getFilmId());

        return update;
    }


    public void delete(Long reviewId) {
        validateReviewIdNotNull(reviewId);
        validateReviewExists(reviewId, "Отзыв с id {} не найден при удалении");

        Review review = reviewDbStorage.findById(reviewId).get();
        Long filmId = review.getFilmId();
        Long userId = review.getUserId();

        reviewDbStorage.delete(reviewId);

        feedService.addEvent(
                userId,
                EventType.REVIEW,
                EventOperation.REMOVE,
                filmId
        );

        log.info("Отзыв с id {} удален", reviewId);
    }


    public Review findById(Long reviewId) {
        validateReviewIdNotNull(reviewId);
        validateReviewExists(reviewId, "Отзыв с id {} не найден при поиске по ID");

        return reviewDbStorage.findById(reviewId).get();
    }


    public Collection<Review> findAllByFilmId(Long filmId, Integer count) {
        if (count == null || count < 0) {
            count = 10;
            log.info("Некорректное значение count - установлено значение по умолчанию: 10");
        }
        if (filmId != null) {
            validateService.validateFilmExist(filmId,
                    "Фильм с id {} не найден при получении отзывов");
        }

        return reviewDbStorage.findAllByFilmId(filmId, count);
    }


    public void addLike(Long reviewId, Long userId) {
        validateReviewExists(reviewId,
                "Попытка поставить лайк несуществующему отзыву с id {}");
        validateService.validateUserExists(userId,
                "Пользователь с id {} не найден при попытке поставить лайк);");
        setReviewLike(reviewId, userId, true);
    }

    public void addDislike(Long reviewId, Long userId) {
        validateReviewExists(reviewId,
                "Попытка поставить дизлайк несуществующему отзыву с id {}");
        validateService.validateUserExists(userId,
                "Пользователь с id {} не найден при попытке поставить дизлайк");
        setReviewLike(reviewId, userId, false);
    }

    public void removeLike(Long reviewId, Long userId) {
        validateReviewExists(reviewId,
                "Попытка удалить лайк с несуществующего отзыва с id {}");
        validateService.validateUserExists(userId,
                "Пользователь с id {} не найден при попытке удалить лайк");
        removeReviewLike(reviewId, userId, true);
    }

    public void removeDislike(Long reviewId, Long userId) {
        validateReviewExists(reviewId,
                "Попытка удалить дизлайк с несуществующего отзыва с id {}");
        validateService.validateUserExists(userId,
                "Пользователь с id {} не найден при попытке удалить дизлайк");
        removeReviewLike(reviewId, userId, false);
    }


    private void validateReviewExists(Long reviewId, String logMessage) {
        if (!reviewDbStorage.existsById(reviewId)) {
            log.warn(logMessage, reviewId);
            throw new NotFoundException("Отзыв с id " + reviewId + " не найден");
        }
    }

    private void validateReviewIdNotNull(Long reviewId) {
        if (reviewId == null) {
            log.warn("Ошибка: reviewId null");
            throw new ValidationException("ID отзыва должен быть указан");
        }
    }

    private void updateReviewUseful(Long reviewId) {
        int useful = reviewLikeStorage.getUsefulCount(reviewId);
        log.info("updateReviewUseful: reviewId={}, new useful={}", reviewId, useful);
        reviewDbStorage.updateUseful(reviewId, useful);
    }

    private void setReviewLike(Long reviewId, Long userId, boolean isLike) {
        if (isLike) {
            if (reviewLikeStorage.existsLike(reviewId, userId)) {
                throw new ValidationException("Вы уже поставили лайк этому отзыву");
            }
            if (reviewLikeStorage.existsDislike(reviewId, userId)) {
                reviewLikeStorage.removeDislike(reviewId, userId);
            }
            reviewLikeStorage.addLike(reviewId, userId);
        } else {
            if (reviewLikeStorage.existsDislike(reviewId, userId)) {
                throw new ValidationException("Вы уже поставили дизлайк этому отзыву");
            }
            if (reviewLikeStorage.existsLike(reviewId, userId)) {
                reviewLikeStorage.removeLike(reviewId, userId);
            }
            reviewLikeStorage.addDislike(reviewId, userId);
        }

        updateReviewUseful(reviewId);
    }

    private void removeReviewLike(Long reviewId, Long userId, boolean isLike) {
        if (isLike) {
            if (!reviewLikeStorage.existsLike(reviewId, userId)) {
                throw new ValidationException("Вы не ставили лайк этому отзыву");
            }
            reviewLikeStorage.removeLike(reviewId, userId);
        } else {
            if (!reviewLikeStorage.existsDislike(reviewId, userId)) {
                throw new ValidationException("Вы не ставили дизлайк этому отзыву");
            }
            reviewLikeStorage.removeDislike(reviewId, userId);
        }
        updateReviewUseful(reviewId);
    }

}
