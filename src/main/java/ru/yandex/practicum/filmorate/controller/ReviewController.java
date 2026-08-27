package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping("/{id}")
    public Review findReviewById(@PathVariable Long id) {
        log.info("Запрос на поиск отзыва по id");
        return reviewService.findById(id);
    }

    @GetMapping
    public Collection<Review> getReviewsByFilmId(
            @RequestParam(required = false) Long filmId,
            @RequestParam(required = false) Integer count) {
        log.info("Запрос на получение отзывов по id фильма");
        return reviewService.findAllByFilmId(filmId, count);
    }

    @PostMapping
    public Review crateNewReview(@Valid @RequestBody Review review) {
        log.info("Запрос на создание нового отзыва");
        return reviewService.create(review);
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        log.info("Запрос на изменение отзыва");
        return reviewService.update(review);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id,
                        @PathVariable Long userId) {
        log.info("Запрос на добавление лайка отзыву");
        reviewService.addLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable Long id,
                           @PathVariable Long userId) {
        log.info("Запрос на добавление дизлайка отзыву");
        reviewService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id,
                           @PathVariable Long userId) {
        log.info("Запрос на удаления лайка с отзыва");
        reviewService.removeLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislike(@PathVariable Long id,
                              @PathVariable Long userId) {
        log.info("Запрос на удаления дизлайка с отзыва");
        reviewService.removeDislike(id, userId);
    }


    @DeleteMapping("/{id}")
    public void removeReview(@PathVariable Long id) {
        log.info("Запрос на удаление отзыва");
        reviewService.delete(id);
    }
}
