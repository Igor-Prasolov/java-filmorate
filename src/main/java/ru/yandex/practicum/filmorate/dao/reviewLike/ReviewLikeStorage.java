package ru.yandex.practicum.filmorate.dao.reviewLike;

public interface ReviewLikeStorage {

    void addLike(Long reviewId, Long userId);

    void addDislike(Long reviewId, Long userId);

    void removeLike(Long reviewId, Long userId);

    void removeDislike(Long reviewId, Long userId);

    int getUsefulCount(Long reviewId);

    Boolean existsLike(Long reviewId, Long userId);

    Boolean existsDislike(Long reviewId, Long userId);

}
