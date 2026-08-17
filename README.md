# Explore With Me

Дипломный проект Яндекс Практикума.

Приложение позволяет создавать события, подавать заявки на участие и оставлять комментарии.

## Архитектура

На втором этапе проект был разделён на несколько микросервисов:

- `user-service` — работа с пользователями;
- `event-service` — события, категории и подборки;
- `request-service` — заявки на участие;
- `comment-service` — комментарии;
- `stats-server` — статистика просмотров.

Для инфраструктуры используются:

- `discovery-server` — Eureka Server;
- `config-server` — хранение общих конфигураций;
- `gateway-server` — единая точка входа в приложение.

Сервисы взаимодействуют между собой по HTTP. Для вызовов между основными сервисами используется OpenFeign.

Основные взаимодействия:

- `event-service` обращается к `user-service`, `request-service` и сервису статистики;
- `request-service` обращается к `event-service` и `user-service`;
- `comment-service` обращается к `event-service` и `user-service`.

Конфигурации сервисов находятся в:

`infra/config-server/src/main/resources/config-repo`

## Внутренний API

### user-service

- `GET /internal/users/{userId}`

### event-service

- `GET /internal/events/{eventId}`
- `PATCH /internal/events/{eventId}/confirmed-requests?delta={n}`

### request-service

- `GET /internal/requests/events/{eventId}`
- `PATCH /internal/requests/statuses`

Внутренние эндпоинты используются только для взаимодействия между микросервисами.

## Внешний API

Спецификации внешнего API находятся в корне проекта:

- [API основного сервиса](ewm-main-service-spec.json)
- [API сервиса статистики](ewm-stats-service-spec.json)
