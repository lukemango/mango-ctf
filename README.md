# Capture The Flag (Minecraft Plugin)

A simple Capture The Flag plugin for Paper servers.

## Requirements

- Java 21
- A Paper 1.21.x server
- Git (optional, for cloning)

This project uses the Gradle Wrapper, so you do not need a global Gradle installation.

## Build With Gradle

From the project root (`CaptureTheFlag`):

### Windows (PowerShell)

```powershell
.\gradlew.bat clean shadowJar
```

### macOS / Linux

```bash
chmod +x ./gradlew
./gradlew clean shadowJar
```

## Build Output

After a successful build, the plugin jar is created at:

`build/libs/mango-capturetheflag.jar`

## Install

1. Copy `build/libs/mango-capturetheflag.jar` into your server `plugins/` folder.
2. Start or restart the server.
3. Configure the plugin in:
   - `plugins/mango-capturetheflag/config.yml`
   - `plugins/mango-capturetheflag/messages.yml`

## Basic Commands

### Player Commands

- `/ctf join <team>`
- `/ctf leave`
- `/ctf score`

### Admin Commands

- `/ctf admin set-flag <team>`
- `/ctf admin start`
- `/ctf admin stop`

Admin commands require permission:

- `ctf.admin`

## Default Game Settings

From `config.yml`:

- Teams: `red`, `blue`
- Game time: `600` seconds
- Captures to win: `3`

