# java-filmorate

Template repository for Filmorate project.

## Схема базы данных

![Схема базы данных](docs/ExampleOfADatabaseTable.png)

## Примеры SQL-запросов для основных операций

### 1. Получить все фильмы

```sql
SELECT *
FROM Films;
```

### 2. Получить топ-10 популярных фильмов (по количеству лайков)

```sql
SELECT f.id,
       f.name,
       COUNT(l.user_id) AS likes_count
FROM Films AS f
LEFT JOIN Likes AS l ON f.id = l.film_id
GROUP BY f.id, f.name
ORDER BY likes_count DESC
LIMIT 10;
```

### 3. Получить список общих друзей двух пользователей (userId = 1, otherId = 2)

```sql
SELECT u.id,
       u.email,
       u.login,
       u.name
FROM Users AS u
JOIN Friends AS f1 ON u.id = f1.friend_id
JOIN Friends AS f2 ON u.id = f2.friend_id
WHERE f1.user_id = 1
  AND f2.user_id = 2;
```

### 4. Получить список друзей пользователя (userId = 1)

```sql
SELECT u.id,
       u.email,
       u.login,
       u.name
FROM Users AS u
JOIN Friends AS f ON u.id = f.friend_id
WHERE f.user_id = 1;
```

### 5. Получить пользователя по ID

```sql
SELECT *
FROM Users
WHERE id = 1;
```

### 6. Получить фильм по ID

```sql
SELECT *
FROM Films
WHERE id = 1;
```