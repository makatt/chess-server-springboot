# chess-server-springboot — частые операции. `make help` покажет список.
COMPOSE ?= docker compose
IMAGE   ?= chess-server:local
ENV     ?= .env
APP_DIR ?= $(CURDIR)

.DEFAULT_GOAL := help
.PHONY: help init build up down restart logs ps test verify pull deploy clean

help: ## показать список целей
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) \
		| awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-10s\033[0m %s\n", $$1, $$2}'

init: ## создать .env из .env.example (секреты задаются вручную)
	@test -f $(ENV) || { cp .env.example $(ENV); echo "создан $(ENV) — задайте POSTGRES_PASSWORD"; }

build: init ## собрать образ приложения локально
	$(COMPOSE) build

up: init ## поднять стек (nginx + app + postgres) в фоне
	$(COMPOSE) up -d --build
	@echo "API:      http://127.0.0.1:$${HTTP_PORT:-8080}/swagger-ui.html"
	@echo "health:   http://127.0.0.1:$${HTTP_PORT:-8080}/actuator/health"

down: ## остановить стек (volume с данными сохраняется)
	$(COMPOSE) down

restart: ## перезапустить приложение
	$(COMPOSE) restart app

logs: ## хвост логов всех сервисов
	$(COMPOSE) logs -f --tail=100

ps: ## состояние контейнеров и healthcheck
	$(COMPOSE) ps

test: ## интеграционные тесты (Testcontainers поднимет postgres сам)
	./mvnw -B -ntp verify

verify: ## проверить, что сервис отвечает через nginx
	@curl -fsS http://127.0.0.1:$${HTTP_PORT:-8080}/actuator/health && echo

pull: ## скачать образ APP_IMAGE из реестра
	$(COMPOSE) pull app

deploy: ## деплой образа из GHCR: make deploy IMAGE=ghcr.io/makatt/chess-server-springboot:sha-...
	APP_DIR=$(APP_DIR) scripts/deploy.sh $(IMAGE)

clean: ## остановить стек и удалить volume с данными БД (деструктивно)
	$(COMPOSE) down -v
