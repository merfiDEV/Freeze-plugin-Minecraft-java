# FreezePlugin

![Java](https://img.shields.io/badge/Java-21-red)
![Paper](https://img.shields.io/badge/Paper-1.21.1-orange)
![Build](https://img.shields.io/badge/Build-Maven-brightgreen)

Paper-плагин для мгновенной заморозки и разморозки игроков через команду `/fz`.

## Преимущества

| Плюс | Что дает на практике |
| --- | --- |
| Мгновенная заморозка | Игрок не может двигаться или телепортироваться |
| Надежный контроль позиции | Возвращает к исходной точке при попытке сдвига |
| Авто-очистка | При выходе игрока состояние корректно снимается |
| Удобное управление | Одна команда, автодополнение по онлайн-игрокам |
| Минимум настроек | Никаких конфигов, простая установка |

## Быстрый старт

1. Скачайте или соберите `.jar`.
2. Положите файл в папку `plugins/` вашего сервера Paper.
3. Перезапустите сервер.
4. Используйте `/fz <ник>`.

## Команды

| Команда | Описание |
| --- | --- |
| `/fz <ник>` | Заморозить или разморозить игрока |

## Права

| Permission | Описание | По умолчанию |
| --- | --- | --- |
| `freezeplugin.use` | Доступ к `/fz` | op |

## Совместимость

- Paper API: `1.21.1-R0.1-SNAPSHOT`
- Java: `21`

## Сборка

```bash
cd FreezePlugin
mvn package
```

Готовый файл появится в `FreezePlugin/target/`.

## Структура проекта

- `FreezePlugin/src/main/java/com/freeze/freezeplugin/FreezePlugin.java` — логика плагина
- `FreezePlugin/src/main/resources/plugin.yml` — описание плагина и команд
- `FreezePlugin/pom.xml` — зависимости и сборка Maven

---

# FreezePlugin (English)

![Java](https://img.shields.io/badge/Java-21-red)
![Paper](https://img.shields.io/badge/Paper-1.21.1-orange)
![Build](https://img.shields.io/badge/Build-Maven-brightgreen)

Paper plugin for instantly freezing and unfreezing players with the `/fz` command.

## Benefits

| Benefit | What it means in practice |
| --- | --- |
| Instant freeze | Player cannot move or teleport |
| Reliable position control | Returns to the original point on any movement attempt |
| Auto cleanup | State is properly removed on player logout |
| Simple control | One command, tab-complete for online players |
| Minimal setup | No configs, easy install |

## Quick start

1. Download or build the `.jar`.
2. Place it into your Paper server `plugins/` folder.
3. Restart the server.
4. Use `/fz <name>`.

## Commands

| Command | Description |
| --- | --- |
| `/fz <name>` | Freeze or unfreeze a player |

## Permissions

| Permission | Description | Default |
| --- | --- | --- |
| `freezeplugin.use` | Access to `/fz` | op |

## Compatibility

- Paper API: `1.21.1-R0.1-SNAPSHOT`
- Java: `21`

## Build

```bash
cd FreezePlugin
mvn package
```

The output file will appear in `FreezePlugin/target/`.

## Project structure

- `FreezePlugin/src/main/java/com/freeze/freezeplugin/FreezePlugin.java` — plugin logic
- `FreezePlugin/src/main/resources/plugin.yml` — plugin metadata and commands
- `FreezePlugin/pom.xml` — Maven dependencies and build
