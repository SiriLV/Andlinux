# AndLinux

[Русский](#русский) | [English](#english)

---

## Русский

**AndLinux** — мобильная Linux-терминальная среда для Android без root-доступа.

Проект делает упор на стабильный запуск Alpine Linux через proot, удобную работу с Android-клавиатурой, выбор shell и полноценные цветовые темы для терминала и интерфейса приложения. AndLinux работает как обычное приложение: ставится из APK, не требует разблокировки загрузчика, не модифицирует систему и не требует root.

```text
Версия: 1.6 (stable)
Разработчик: SiriLV
Лицензия: MIT
Package ID: com.term.andlinux
Root-доступ: не требуется
Основная среда: Alpine Linux (последний стабильный релиз)
SDK: min 26 (Android 8.0), target 28 (Fdroid) / 35 (PlayStore)
```

### Ключевые возможности

- **Alpine Linux внутри Android через proot.** Полноценный userspace-дистрибутив Alpine, запускаемый без root через `proot` с предзагруженным `libtalloc.so.2`.
- **Отдельный режим Android shell.** При необходимости можно работать в чистом Android shell, не входя в Alpine.
- **Несколько терминальных сессий.** Сессии переключаются через боковое меню, каждая имеет свой рабочий режим.
- **Виртуальные терминальные клавиши.** `ESC`, `CTRL`, `ALT`, стрелки, `HOME`, `END`, `PGUP`, `PGDN`, `TAB`.
- **Выбор shell для Alpine:**
  - `ash` — BusyBox ash, минимальный и быстрый shell по умолчанию.
  - `bash` — GNU Bash, лучшая совместимость со скриптами.
  - `fish` — современный интерактивный shell с автодополнением.
  - `zsh` — мощный настраиваемый shell.
- **Login-shell запуск** для выбранного shell.
- **Автоматическая установка выбранного shell**, если его ещё нет в Alpine (через `apk add`).
- **Настраиваемый размер текста терминала** (10–20sp).
- **Настраиваемый scrollback** терминала.
- **Пользовательский шрифт** (любой `.ttf`, рекомендуется моноширинный).
- **Пользовательский фон** терминала с автоматическим выбором контраста текста.
- **Прозрачность фона** от 0 до 1.
- **Переключатели status bar, title bar и virtual keys**.
- **Настраиваемые keyboard shortcuts** для paste, new/close/switch session.
- **Темы**, которые меняют и терминал, и интерфейс приложения (Material 3).
- **Динамический выбор последнего стабильного релиза Alpine** при первой установке — больше не привязан к конкретной версии.

### Темы

AndLinux 1.5 поддерживает набор встроенных цветовых схем:

```text
Default
Dracula
Nord
Solarized Dark
Solarized Light
Gruvbox Dark
Gruvbox Light
One Dark
Tokyo Night
Tokyo Night Light
Catppuccin Mocha
Catppuccin Latte
Monokai
Material Dark
Ayu Dark
Ayu Light
```

Темы применяются к терминалу и основному UI: экрану сессий, настройкам, карточкам, панелям, акцентным цветам и системным bar-флагам. Выбор темы сохраняется в `colors.properties` и применяется при следующем запуске.

### Первый запуск: user и hostname

При первом запуске Alpine приложение предлагает задать видимое имя пользователя и hostname:

```text
AndLinux first setup

User name [user]: siri
Host name [andlinux]: okak

Saved identity: siri@okak
```

После этого prompt будет выглядеть примерно так:

```text
siri@okak:~#
```

Повторно изменить имя можно командой:

```sh
andlinux-identity
```

Эта команда меняет prompt, `/etc/hostname`, `/etc/hosts` и переменные окружения (`USER`, `LOGNAME`, `HOSTNAME`, `ANDLINUX_USER`, `ANDLINUX_HOST`) внутри Alpine. Среда по-прежнему работает через proot — root-доступ не требуется.

### Загрузка Alpine Linux

При первом запуске AndLinux скачивает три файла:

1. `libtalloc.so.2` — библиотека, требуемая `proot`.
2. `proot` — бинарник proot для соответствующей архитектуры.
3. `alpine.tar.gz` — minirootfs последнего стабильного релиза Alpine Linux.

Для Alpine minirootfs URL резолвится **динамически** во время запуска:

```text
https://dl-cdn.alpinelinux.org/alpine/latest-stable/releases/<arch>/
```

Приложение получает листинг директории, ищет файл вида
`alpine-minirootfs-<version>-<arch>.tar.gz`, выбирает файл с наибольшим
номером версии и скачивает его. Благодаря этому AndLinux всегда
устанавливает актуальный стабильный релиз Alpine, не требуя обновления
самого APK при выходе новой минорной версии Alpine.

Поддерживаемые архитектуры:

| Android ABI     | Alpine arch |
|-----------------|-------------|
| `arm64-v8a`     | `aarch64`   |
| `armeabi-v7a`   | `armhf`     |
| `x86_64`        | `x86_64`    |

### Shell

Открой:

```text
Settings -> Default Shell
```

Выбери `ash`, `bash`, `fish` или `zsh`, затем открой новую сессию.

Проверка:

```sh
echo "$SHELL"
echo "$0"
```

### Сборка APK

Основной workflow:

```text
.github/workflows/android.yml
```

Команда сборки:

```sh
./gradlew --no-daemon assembleFdroidRelease -x lintVitalFdroidRelease
```

Artifact в GitHub Actions:

```text
Andlinux-apk/Andlinux.apk
```

Локальный путь после сборки:

```text
app/build/outputs/apk/Fdroid/release/*.apk
```

Для локальной сборки также есть `release-build.sh`:

```sh
./release-build.sh
```

Скрипт сам сгенерирует `local.properties` из `ANDROID_HOME` (если переменная окружения задана), запустит `clean` и соберёт Fdroid-release APK.

### Архитектура проекта

```text
.
├── app/                      # application-модуль (entry point)
│   └── build.gradle.kts
├── core/
│   ├── main/                 # основной код AndLinux
│   │   ├── src/main/
│   │   │   ├── assets/
│   │   │   │   ├── init.sh         # init-скрипт Alpine (вызывается proot-ом)
│   │   │   │   └── init-host.sh    # host-сторона: monтировка bind-ов, запуск proot
│   │   │   ├── java/com/rk/
│   │   │   │   ├── terminal/       # UI: terminal screen, settings, customization, downloader
│   │   │   │   ├── libcommons/     # общие утилиты (FileUtil, Utils, …)
│   │   │   │   ├── settings/       # SharedPreferences wrapper
│   │   │   │   ├── update/         # UpdateManager — обновляет init-скрипты при апгрейде
│   │   │   │   ├── crashhandler/   # global crash handler
│   │   │   │   └── karbon_exec/    # интеграция с Termux RUN_COMMAND
│   │   │   └── res/                # темы, strings, layout'ы
│   │   └── build.gradle.kts
│   ├── components/           # переиспользуемые Compose-компоненты (preferences)
│   ├── resources/            # strings.xml и иконки (мультиязычность: en, zh, ar)
│   ├── terminal-emulator/    # форк Termux terminal-emulator
│   └── terminal-view/        # форк Termux terminal-view
├── fastlane/                 # metadata для F-Droid / Play Store
├── .github/
│   ├── workflows/            # CI: build APK, delete old runs, verifyDiff
│   └── scripts/
│       └── prepare_andlinux.py    # build-time patching (теперь практически no-op)
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/libs.versions.toml
├── gradle.properties
└── README.md
```

### Структура каталогов на устройстве

После установки AndLinux использует следующие каталоги внутри своего приватного хранилища:

```text
/data/data/com.term.andlinux/
├── files/                 # Rootfs.andLinux — сюда скачиваются proot, libtalloc, alpine.tar.gz
├── local/
│   ├── bin/               # init, init-host, proot (копия)
│   ├── lib/               # libtalloc.so.2 (копия)
│   └── alpine/            # распакованный Alpine rootfs
│       └── root/          # $HOME внутри Alpine (=AlpineDocumentProvider root)
└── tmp/                   # PROOT_TMP_DIR, чистится при старте
```

Файлы внутри `/data/data/com.term.andlinux/files/` (бывший `Rootfs.reTerminal`) — кеш первой установки. Если их удалить, при следующем запуске AndLinux перекачает их заново.

### Roadmap

Следующие направления:

- Поддержка Arch Linux как отдельного rootfs-профиля.
- Поддержка Debian как отдельного rootfs-профиля.
- Менеджер дистрибутивов:
  - выбор профиля;
  - загрузка или import rootfs;
  - checksum-проверка;
  - отдельная директория для каждого дистрибутива;
  - отдельный init-script;
  - reset/delete/export rootfs.
- Улучшение первого setup-экрана.
- Presets для dev-пакетов.
- Более аккуратный менеджер сессий.

Arch и Debian не входят в версию 1.5. Сначала зафиксирована стабильная Alpine-база.

### Что нового в 1.5 (global stable update)

**Первый проход (основные исправления):**

- **Динамическая загрузка последнего стабильного релиза Alpine Linux.** URL миниrootfs резолвится из `https://dl-cdn.alpinelinux.org/alpine/latest-stable/releases/<arch>/` вместо захардкоженного `v3.21/releases/.../alpine-minirootfs-3.21.0-*.tar.gz`.
- **Полное переименование проекта в AndLinux.** Из исходников удалены все упоминания предыдущего названия (`ReTerminal`). `Rootfs.reTerminal` переименован в `Rootfs.andLinux`.
- **Исправлен баг в `showStatusBar`** — на устройствах с Android ≤ 10 status bar скрывался даже когда пользователь просил его показать.
- **Исправлены fallback-пути в `FileUtil`** — `/data/data/com.rk.terminal/...` заменены на корректные `/data/data/com.term.andlinux/...`.
- **Исправлен `CrashHandler`** — пустой `runCatching {}` заменён на корректную запись crash-лога; background-thread crash корректно завершает процесс.
- **Исправлен `KarbonExec.launchInternalTerminal`** — ссылка на несуществующий класс `com.rk.xededitor.ui.activities.terminal.Terminal` заменена на `MainActivity`.
- **Throttle прогресса загрузки** — UI обновляется не чаще ~10 раз в секунду вместо каждого чан-чтения, что убирает main-thread thrash на больших файлах.
- **Partial-download cleanup** — если загрузка упала на середине, недокачанные файлы удаляются, чтобы следующий запуск не использовал мусор.
- **Очистка stale-артефактов**: удалён `out/mapping/cmd-v1.0.1.txt`, каталог `out/` добавлен в `.gitignore`.
- **Обновлены `release-build.sh` и `.github/workflows/verifyDiff.sh`** — больше не ссылаются на `Xed-Editor` и `RohitKushvaha01/ReTerminal`.
- **Обновлён `fastlane` full_description** — теперь корректно описывает AndLinux.
- **Bump version: 1.4.1 → 1.5**, versionCode 11 → 12.

**Второй проход (quality cleanup):**

- **Исправлен NPE-risk в `TerminalScreen.kt`** — заголовок сессии мог упасть с NPE, если `sessionBinder` отключался во время рендера. Заменено на null-safe string interpolation.
- **Исправлен неправильный ключ Termux RUN_COMMAND** в `KarbonExec.runCommandTermux` — использовался `com.termux.RUN_COMMAND_SERVICE.EXTRA_WORKDIR` (молча игнорируется Termux), заменён на правильный `com.termux.RUN_COMMAND_WORKDIR`.
- **Исправлен известный краш в `SessionService.terminateSession`** (помечен комментарием `//crash is here`) — заменён хрупкий null-emulator guard на runCatching вокруг `finishIfRunning()`, теперь закрытие одной сессии не может уронить весь сервис.
- **Удалён мёртвый код**: фейковые `/proc/stat` и `/proc/vmstat` строки в `Data.kt` (остатки от удалённого btop fake-procfs shim), неиспользуемая `var isAppInBackground`, неиспользуемые настройки `Settings.github` и `Settings.ignore_storage_permission`, orphan-строки `use_shizuku*` и весь `input_mode_*` family (UI никогда не подключался).
- **Переписан `UpdateManager.onUpdate`** — убран избыточный `if (exists) delete; if (!exists) recreate` танец, теперь init-скрипты всегда перезаписываются из assets и делаются executable.
- **Переименован env var** `RET_DEFAULT_SHELL` → `ANDLINUX_DEFAULT_SHELL` (в обоих местах: `MkSession.kt` и `init.sh`) для консистентности нейминга.
- **Очищены unused imports** в `TerminalBackEnd.kt`, `MkSession.kt`, `SessionService.kt`, `FileUtil.kt`, `MainActivityNavHost.kt`, `KarbonExec.kt`.
- **Локализация**: добавлены `exit_action`, `session_running_one`, `session_running_many` строки в en/zh/ar. Notification service и downloader теперь используют локализованные строки вместо захардкоженного английского.
- **Проверено**: 0 неиспользуемых строковых ресурсов, 0 неиспользуемых импортов в отредактированных файлах.

### Что нового в 1.6 (stable)

**Исправлены баги:**

- **`showHorizontalToolbar` неправильно инициализировался** из `Settings.toolbar` вместо `Settings.toolbar_in_horizontal`. После рестарта приложения горизонтальный тулбар сбрасывался в состояние обычного тулбара.
- **`changeSession()` перетирал цвета темы терминала.** При переключении сессий в слоты 256/258 принудительно ставился `colorOnSurface`, игнорируя выбранную пользователем терминальную тему (Dracula, Nord и т.д.). Теперь Default-тема использует `getViewColor()`, именованные темы — `mColors.reset()`.
- **NPE-риски в `TerminalScreen.kt`** — все `mainActivityActivity.sessionBinder!!` заменены на null-safe доступ через локальную переменную `binder`. Если сервис отваливается mid-render — больше не крашит.
- **NPE в `TerminalBackEnd.onKeyDown`** — `activity.sessionBinder?.terminateSession(activity.sessionBinder!!.getService()...)` — аргумент вычислялся до short-circuit. Теперь `binder` сначала проверяется на null.
- **Memory leak в `MainActivity.onResume`** — `addOnGlobalLayoutListener` добавлялся при каждом `onResume` и никогда не удалялся. Слушатели копились. Теперь добавляется один раз и удаляется в `onDestroy`.
- **`CrashHandler` не устанавливался** — строка `Thread.setDefaultUncaughtExceptionHandler(CrashHandler)` была закомментирована в `App.kt`. Теперь crash-логи пишутся в `filesDir/crash.log`.
- **`App.kt` StrictMode убивал executor-поток** — `violation.cause?.let { throw it }` выбрасывал исключение в penaltyListener, останавливая дальнейший репортинг. Теперь только логирует.
- **`MkSession` переписывал init-скрипты на каждую сессию** — избыточно, `UpdateManager.onUpdate()` уже это делает при старте приложения. Удалено.
- **`MkSession` форвардил null env vars** — `System.getenv()` мог вернуть null, в env попадало `"KEY=null"`. Теперь null-значения фильтруются.
- **`Downloader` удалял уже существующие файлы при ошибке** — если загрузка падала, удалялись ВСЕ файлы, включая уже скачанные. Теперь удаляются только файлы, загруженные в текущем запуске.
- **`AlpineDocumentProvider.isChildDocument`** — `startsWith` без разделителя: `/root` матчило `/rootfile`. Теперь используется path-separator-aware проверка.
- **`AlpineDocumentProvider.deleteDocument`** — `File.delete()` на непустой директории возвращает false → `FileNotFoundException`. Теперь падает обратно на `deleteRecursively()` для директорий.
- **`init.sh` `set -e` убивал терминал** — `apk update` failure (нет сети) убивал скрипт, терминал неработоспособен. `set -e` удалён, `install_packages` теперь graceful-degrades.
- **`init-host.sh` glob `*.so.2`** — если совпадений нет, glob раскрывается в literal string, цикл пытался копировать несуществующий файл. Теперь `[ -e "$sofile" ] || continue` гардит.
- **`SessionService.sessions` был обычным HashMap** — доступ из UI thread, binder thread, onDestroy. ConcurrentHashMap теперь.
- **`Utils.isMainThread()`** — проверял имя потока (`"main"`), что хрупко. Теперь использует `Looper.myLooper() === Looper.getMainLooper()`.
- **`Customization.kt` `getFileNameFromUri().toString()`** — на null возвращал literal "null". Теперь `?: uri.toString()` fallback.
- **Debug `println(session_id)`** — leftover в TerminalScreen.kt. Удалено.
- **Дублирующиеся nested `if (Settings.terminal_theme == "Default")` блоки** — артефакты от `prepare_andlinux.py` regex-патчинга. Внутренний `else` был unreachable. Схлопнуты.

**Удалён мёртвый код:**

- `RunCommandService.kt` — stub с `TODO("Not yet implemented")`, нигде не использовался.
- `LoadingPopup.kt` — никогда не инстанциировался.
- `ActionPopup.kt` — никогда не инстанциировался.
- `ApplicationBackground.kt` — `var isAppInBackground` никогда не читался/писался.

**Инфраструктура:**

- `.github/scripts/prepare_andlinux.py` переписан как полностью идемпотентный — повторный запуск не создаёт дублирующихся if-блоков или двойных импортов.

### Лицензия

MIT. См. [`LICENSE`](LICENSE).

---

## English

**AndLinux** is a mobile Linux terminal environment for Android without root access.

The project focuses on stable Alpine Linux startup through proot, practical Android keyboard behavior, shell selection, and full color themes for both the terminal and the app UI. AndLinux runs as a regular APK: no bootloader unlock, no system modification, no root.

```text
Version: 1.6 (stable)
Developer: SiriLV
License: MIT
Package ID: com.term.andlinux
Root access: not required
Main environment: Alpine Linux (latest stable release)
SDK: min 26 (Android 8.0), target 28 (Fdroid) / 35 (PlayStore)
```

### Key features

- **Alpine Linux on Android through proot.** A full Alpine userspace running unrooted via `proot` with a pre-bundled `libtalloc.so.2`.
- **Separate Android shell mode.** Drop into a plain Android shell without entering Alpine when you need it.
- **Multiple terminal sessions.** Switch sessions from the side drawer; each session has its own working mode.
- **Virtual terminal keys.** `ESC`, `CTRL`, `ALT`, arrows, `HOME`, `END`, `PGUP`, `PGDN`, `TAB`.
- **Alpine shell selector:**
  - `ash` — BusyBox ash, minimal and fastest default shell.
  - `bash` — GNU Bash, best compatibility for scripts.
  - `fish` — modern interactive shell with autosuggestions.
  - `zsh` — powerful configurable shell.
- **Login-shell startup** for the selected shell.
- **Automatic installation of the selected shell** when it is missing from Alpine (via `apk add`).
- **Configurable terminal text size** (10–20sp).
- **Configurable scrollback.**
- **Custom font** (any `.ttf`, monospaced strongly recommended).
- **Custom background** with automatic text-contrast adjustment.
- **Background transparency** from 0 to 1.
- **Status bar, title bar, and virtual keys toggles.**
- **Configurable keyboard shortcuts** for paste, new/close/switch session.
- **Themes** that drive both the terminal and the app UI (Material 3).
- **Dynamic latest-stable Alpine release resolution** at first run — no longer pinned to a specific Alpine version.

### Themes

AndLinux 1.5 includes built-in color schemes:

```text
Default
Dracula
Nord
Solarized Dark
Solarized Light
Gruvbox Dark
Gruvbox Light
One Dark
Tokyo Night
Tokyo Night Light
Catppuccin Mocha
Catppuccin Latte
Monokai
Material Dark
Ayu Dark
Ayu Light
```

Themes are applied to the terminal and the main UI: session drawer, settings, cards, panels, accent colors, and system bar flags. The selected theme is persisted in `colors.properties` and reapplied on next launch.

### First launch: user and hostname

On the first Alpine launch, AndLinux asks for the visible user name and hostname:

```text
AndLinux first setup

User name [user]: siri
Host name [andlinux]: okak

Saved identity: siri@okak
```

The prompt will then look like this:

```text
siri@okak:~#
```

You can change it later with:

```sh
andlinux-identity
```

This changes the prompt, `/etc/hostname`, `/etc/hosts`, and environment variables (`USER`, `LOGNAME`, `HOSTNAME`, `ANDLINUX_USER`, `ANDLINUX_HOST`) inside Alpine. The environment still runs through proot — no root required.

### Alpine Linux download

On first launch AndLinux downloads three files:

1. `libtalloc.so.2` — library required by `proot`.
2. `proot` — proot binary for the device's architecture.
3. `alpine.tar.gz` — the minirootfs of the latest stable Alpine release.

For the Alpine minirootfs the URL is resolved **dynamically** at runtime:

```text
https://dl-cdn.alpinelinux.org/alpine/latest-stable/releases/<arch>/
```

The app fetches the directory listing, looks for files matching
`alpine-minirootfs-<version>-<arch>.tar.gz`, picks the one with the
highest version number, and downloads it. This way AndLinux always
installs the current stable Alpine release without requiring an APK
update each time Alpine ships a new minor version.

Supported architectures:

| Android ABI     | Alpine arch |
|-----------------|-------------|
| `arm64-v8a`     | `aarch64`   |
| `armeabi-v7a`   | `armhf`     |
| `x86_64`        | `x86_64`    |

### Shell

Open:

```text
Settings -> Default Shell
```

Choose `ash`, `bash`, `fish`, or `zsh`, then open a new session.

Check inside Alpine:

```sh
echo "$SHELL"
echo "$0"
```

### Building the APK

Main workflow:

```text
.github/workflows/android.yml
```

Build command:

```sh
./gradlew --no-daemon assembleFdroidRelease -x lintVitalFdroidRelease
```

GitHub Actions artifact:

```text
Andlinux-apk/Andlinux.apk
```

Local APK path:

```text
app/build/outputs/apk/Fdroid/release/*.apk
```

For local builds there is also `release-build.sh`:

```sh
./release-build.sh
```

It will auto-generate `local.properties` from `ANDROID_HOME` (if set), then run `clean` and build the Fdroid-release APK.

### Project architecture

```text
.
├── app/                      # application module (entry point)
│   └── build.gradle.kts
├── core/
│   ├── main/                 # main AndLinux code
│   │   ├── src/main/
│   │   │   ├── assets/
│   │   │   │   ├── init.sh         # Alpine init script (called by proot)
│   │   │   │   └── init-host.sh    # host side: bind mounts, proot launch
│   │   │   ├── java/com/rk/
│   │   │   │   ├── terminal/       # UI: terminal screen, settings, customization, downloader
│   │   │   │   ├── libcommons/     # common utilities (FileUtil, Utils, …)
│   │   │   │   ├── settings/       # SharedPreferences wrapper
│   │   │   │   ├── update/         # UpdateManager — refreshes init scripts on upgrade
│   │   │   │   ├── crashhandler/   # global crash handler
│   │   │   │   └── karbon_exec/    # Termux RUN_COMMAND integration
│   │   │   └── res/                # themes, strings, layouts
│   │   └── build.gradle.kts
│   ├── components/           # reusable Compose components (preferences)
│   ├── resources/            # strings.xml and icons (i18n: en, zh, ar)
│   ├── terminal-emulator/    # Termux terminal-emulator fork
│   └── terminal-view/        # Termux terminal-view fork
├── fastlane/                 # F-Droid / Play Store metadata
├── .github/
│   ├── workflows/            # CI: build APK, delete old runs, verifyDiff
│   └── scripts/
│       └── prepare_andlinux.py    # build-time patching (now essentially a no-op)
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/libs.versions.toml
├── gradle.properties
└── README.md
```

### On-device directory layout

After install, AndLinux uses the following directories inside its private storage:

```text
/data/data/com.term.andlinux/
├── files/                 # Rootfs.andLinux — proot, libtalloc, alpine.tar.gz land here
├── local/
│   ├── bin/               # init, init-host, proot (copy)
│   ├── lib/               # libtalloc.so.2 (copy)
│   └── alpine/            # extracted Alpine rootfs
│       └── root/          # $HOME inside Alpine (=AlpineDocumentProvider root)
└── tmp/                   # PROOT_TMP_DIR, cleaned on start
```

Files inside `/data/data/com.term.andlinux/files/` (formerly `Rootfs.reTerminal`) are a first-install cache. Deleting them forces AndLinux to re-download them on next launch.

### Roadmap

Planned next steps:

- Arch Linux support as a separate rootfs profile.
- Debian support as a separate rootfs profile.
- Distribution manager:
  - profile selection;
  - rootfs download or import;
  - checksum validation;
  - separate directory per distribution;
  - separate init script;
  - reset/delete/export rootfs.
- Improved first-run setup screen.
- Developer package presets.
- Cleaner session manager.

Arch and Debian are not included in 1.5. This version first locks down the stable Alpine base.

### What's new in 1.5 (global stable update)

**First pass (core fixes):**

- **Dynamic latest-stable Alpine Linux download.** The minirootfs URL is now resolved from `https://dl-cdn.alpinelinux.org/alpine/latest-stable/releases/<arch>/` instead of the hardcoded `v3.21/releases/.../alpine-minirootfs-3.21.0-*.tar.gz`.
- **Full rename to AndLinux.** All mentions of the previous name (`ReTerminal`) have been removed from source. `Rootfs.reTerminal` was renamed to `Rootfs.andLinux`.
- **Fixed `showStatusBar` bug** — on Android ≤ 10 the status bar was being hidden even when the user asked to show it.
- **Fixed fallback paths in `FileUtil`** — `/data/data/com.rk.terminal/...` replaced with the correct `/data/data/com.term.andlinux/...`.
- **Fixed `CrashHandler`** — the empty `runCatching {}` was replaced with proper crash-log writing; background-thread crashes now correctly terminate the process.
- **Fixed `KarbonExec.launchInternalTerminal`** — the reference to the non-existent `com.rk.xededitor.ui.activities.terminal.Terminal` class was replaced with `MainActivity`.
- **Throttled download progress** — UI updates happen at most ~10 times per second instead of on every chunk read, eliminating main-thread thrash on large files.
- **Partial-download cleanup** — if a download fails midway, partially-written files are deleted so the next run doesn't use garbage.
- **Stale artifact cleanup**: removed `out/mapping/cmd-v1.0.1.txt`, added `out/` to `.gitignore`.
- **Updated `release-build.sh` and `.github/workflows/verifyDiff.sh`** — no longer reference `Xed-Editor` or `RohitKushvaha01/ReTerminal`.
- **Updated `fastlane` full_description** — now correctly describes AndLinux.
- **Version bump: 1.4.1 → 1.5**, versionCode 11 → 12.

**Second pass (quality cleanup):**

- **Fixed NPE risk in `TerminalScreen.kt`** — session title bar could NPE if `sessionBinder` disconnected mid-render. Replaced with null-safe string interpolation.
- **Fixed wrong Termux RUN_COMMAND extra key** in `KarbonExec.runCommandTermux` — was using `com.termux.RUN_COMMAND_SERVICE.EXTRA_WORKDIR` (silently ignored by Termux), replaced with the correct `com.termux.RUN_COMMAND_WORKDIR`.
- **Fixed known crash in `SessionService.terminateSession`** (marked with `//crash is here` comment) — replaced the brittle null-emulator guard with a runCatching around `finishIfRunning()`, so closing one session can never take down the whole service.
- **Removed dead code**: fake `/proc/stat` and `/proc/vmstat` strings in `Data.kt` (leftovers from the removed btop fake-procfs shim), unused `var isAppInBackground`, unused `Settings.github` and `Settings.ignore_storage_permission` preferences, orphaned `use_shizuku*` strings and the entire `input_mode_*` family (UI was never wired up).
- **Rewrote `UpdateManager.onUpdate`** — removed the redundant `if (exists) delete; if (!exists) recreate` dance; init scripts are now always overwritten from assets and made executable.
- **Renamed env var** `RET_DEFAULT_SHELL` → `ANDLINUX_DEFAULT_SHELL` (in both `MkSession.kt` and `init.sh`) for naming consistency.
- **Cleaned unused imports** in `TerminalBackEnd.kt`, `MkSession.kt`, `SessionService.kt`, `FileUtil.kt`, `MainActivityNavHost.kt`, `KarbonExec.kt`.
- **Localization**: added `exit_action`, `session_running_one`, `session_running_many` strings in en/zh/ar. Notification service and downloader now use localized strings instead of hardcoded English.
- **Verified**: 0 unused string resources, 0 unused imports in edited files.

### What's new in 1.6 (stable)

**Bugs fixed:**

- **`showHorizontalToolbar` was initialized from the wrong setting** — `Settings.toolbar` instead of `Settings.toolbar_in_horizontal`. After app restart, the horizontal toolbar reset to the regular toolbar state.
- **`changeSession()` overwrote terminal theme colors.** When switching sessions, slot 256/258 was force-set to `colorOnSurface`, ignoring the user's selected terminal theme (Dracula, Nord, etc.). Now Default theme uses `getViewColor()`, named themes use `mColors.reset()`.
- **NPE risks in `TerminalScreen.kt`** — every `mainActivityActivity.sessionBinder!!` replaced with null-safe access via a local `binder` variable. If the service disconnects mid-render — no more crash.
- **NPE in `TerminalBackEnd.onKeyDown`** — `activity.sessionBinder?.terminateSession(activity.sessionBinder!!.getService()...)` — the argument was evaluated before the short-circuit. Now `binder` is checked for null first.
- **Memory leak in `MainActivity.onResume`** — `addOnGlobalLayoutListener` was added on every `onResume` and never removed. Listeners stacked up. Now added once and removed in `onDestroy`.
- **`CrashHandler` was never installed** — `Thread.setDefaultUncaughtExceptionHandler(CrashHandler)` was commented out in `App.kt`. Now crash logs land in `filesDir/crash.log`.
- **`App.kt` StrictMode killed the executor thread** — `violation.cause?.let { throw it }` threw inside penaltyListener, stopping further violation reporting. Now only logs.
- **`MkSession` rewrote init scripts on every session** — redundant, `UpdateManager.onUpdate()` already does this on app start. Removed.
- **`MkSession` forwarded null env vars** — `System.getenv()` could return null, putting literal `"KEY=null"` entries into the child env. Now null values are filtered out.
- **`Downloader` deleted pre-existing files on failure** — if a download failed, ALL files were wiped, including ones that already existed. Now only files downloaded in the current run are deleted.
- **`AlpineDocumentProvider.isChildDocument`** — `startsWith` without separator: `/root` matched `/rootfile`. Now uses path-separator-aware check.
- **`AlpineDocumentProvider.deleteDocument`** — `File.delete()` on a non-empty directory returns false → `FileNotFoundException`. Now falls back to `deleteRecursively()` for directories.
- **`init.sh` `set -e` killed the terminal** — `apk update` failure (no network) killed the script, leaving the terminal unusable. `set -e` removed, `install_packages` now graceful-degrades.
- **`init-host.sh` glob `*.so.2`** — when no match, the glob expands to the literal string, the loop tried to copy a non-existent file. Now `[ -e "$sofile" ] || continue` guards it.
- **`SessionService.sessions` was a plain HashMap** — accessed from UI thread, binder thread, onDestroy. Now a ConcurrentHashMap.
- **`Utils.isMainThread()`** — checked thread name (`"main"`), which is fragile. Now uses `Looper.myLooper() === Looper.getMainLooper()`.
- **`Customization.kt` `getFileNameFromUri().toString()`** — returned literal "null" when the function returned null. Now `?: uri.toString()` fallback.
- **Debug `println(session_id)`** — leftover in TerminalScreen.kt. Removed.
- **Duplicated nested `if (Settings.terminal_theme == "Default")` blocks** — artifacts from `prepare_andlinux.py` regex patching. The inner `else` was unreachable. Collapsed.

**Dead code removed:**

- `RunCommandService.kt` — stub with `TODO("Not yet implemented")`, never used.
- `LoadingPopup.kt` — never instantiated.
- `ActionPopup.kt` — never instantiated.
- `ApplicationBackground.kt` — `var isAppInBackground` never read or written.

**Infrastructure:**

- `.github/scripts/prepare_andlinux.py` rewritten as fully idempotent — re-running it does not create duplicated if-blocks or double imports.

### License

MIT. See [`LICENSE`](LICENSE).
