package ru.yandex.practicum.filmorate.dao.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {

    Review create(Review review);

    Review update(Review review);

    void delete(Long reviewId);

    Optional<Review> findById(Long reviewId);

    List<Review> findAllByFilmId(Long filmId, int count);

    Boolean existsById(Long id);

    void updateUseful(Long reviewId, int useful);

}
