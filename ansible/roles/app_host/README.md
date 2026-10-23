# Роль `app_host`

Приводит чистый сервер (VPS или домашнюю машину) к состоянию, из которого
репозиторий `chess-server-springboot` можно деплоить: пользователь + SSH-ключ из CI, docker,
рабочая копия репозитория, `.env` со сгенерированным паролем БД, systemd-юнит
для автозапуска стека. Опционально — хостовый nginx и ufw.

## Запуск

```bash
cd ansible
cp inventory.example.ini inventory.ini     # подставить хост
ansible-galaxy collection install -r requirements.yml
ansible-playbook -K -i inventory.ini site.yml
```

`-K` спрашивает пароль sudo интерактивно: пароли в файлах не хранятся.
Проверка без изменений: `ansible-playbook -K --check --diff -i inventory.ini site.yml`.

## Что делает роль

| Тег | Действие |
|---|---|
| `user` | юзер `app_host_deploy_user` (по умолчанию `deploy`), его SSH-ключи, опционально sudo без пароля |
| `docker` | пакеты `docker.io` + `docker-compose-v2` (только если docker не установлен), сервис включён, юзеры добавлены в группу `docker` |
| `app` | клон репозитория в `app_host_app_dir`, `.env` из шаблона с паролем-`lookup('password')`, systemd-юнит `chess-server.service` |
| `nginx` | хостовый nginx (по умолчанию выключен — в compose уже есть свой) |
| `firewall` | ufw: deny incoming, разрешены SSH/HTTP/интерфейс tailscale (по умолчанию выключен) |

## Важные переменные

```yaml
app_host_ssh_public_keys: ["ssh-ed25519 AAAA... deploy@mks"]  # обязательна
app_host_app_dir: /opt/chess-server
app_host_repo_url: https://github.com/makatt/chess-server-springboot.git
app_host_manage_firewall: false    # включать осознанно: меняет сетевой доступ
app_host_install_nginx: false      # в compose уже есть nginx
app_host_start_stack: false        # поднимать контейнеры сразу после провижининга
```

## Разделение ответственности

* **Ansible** — первичный провижининг хоста и `.env` (секреты генерируются, в git не попадают).
* **GitHub Actions (`cd.yml` → `scripts/deploy.sh`)** — релизы: обновление рабочей копии
  (`git reset --hard origin/main`), `docker compose pull`, перезапуск и проверка health.
