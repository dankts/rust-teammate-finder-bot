# Rust Finder Player Bot

Телеграм‑бот на Spring Boot, который помогает игрокам Rust находить тиммейтов: бот регистрирует профиль, позволяет управлять ролями, статусом поиска и отправляет рассылки пользователям.

## Возможности
- `/start` — пошаговая регистрация профиля (никнейм, возраст, часы, статус поиска).
- Просмотр и обновление профиля (смена роли, привязка Steam).
- Поиск тиммейтов с пагинацией.
- Админская рассылка (`/admin <сообщение>`), бан пользователей.

## Технологии
- Java 21, Spring Boot 3.5
- Spring Data JPA + PostgreSQL
- TelegramBots 9.2 (long polling)
- Lombok, Maven

## Быстрый старт
1. Создайте бота через [@BotFather](https://t.me/BotFather) и получите токен.
2. Поднимите PostgreSQL и создайте базу (миграции выполняет Hibernate).
3. Скопируйте `src/main/resources/application-example.properties` в `application.properties` (файл игнорируется Git) или задайте переменные окружения:
   ```properties
   bot.token=${BOT_TOKEN}
   spring.datasource.url=${DATABASE_URL}
   spring.datasource.username=${DATABASE_USERNAME}
   spring.datasource.password=${DATABASE_PASSWORD}
   spring.jpa.hibernate.ddl-auto=update
   ```
4. Запустите приложение:
   ```bash
   mvn spring-boot:run
   ```

## Переменные окружения
- `BOT_TOKEN` — токен Telegram бота.
- `DATABASE_URL` — `jdbc:postgresql://host:port/db`.
- `DATABASE_USERNAME`, `DATABASE_PASSWORD`.
- Дополнительно можно задать `SPRING_PROFILES_ACTIVE`.

## Запуск тестов
```bash
mvn clean test
```

## Публикация на GitHub
1. Убедитесь, что файлы с секретами не попали в репозиторий (`application.properties`, `.env`).
2. Проверьте `git status`, чтобы не коммитить `target/` и IDE‑артефакты (см. `.gitignore`).
3. Добавьте описание репозитория, лицензию и теги на GitHub.
4. По возможности прикрепите скриншоты/гифки работы бота.

## План развития
- Добавить Docker Compose с PostgreSQL.
- Настроить CI (GitHub Actions) для запуска тестов и линтеров.
- Реализовать Webhook‑режим для бота.
- Улучшить систему ролей и модерации.

