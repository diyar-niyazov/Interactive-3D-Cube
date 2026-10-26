# Interactive 3D Cube

A Java implementation of a **rotatable 3D cube rendered from scratch using Java Swing**.
The cube is made of 3D points that are rotated every frame and then projected onto a 2D screen, where they are drawn using Java AWT Graphics.

It also includes a **terminal version** for kitty and other Unix terminals. It
ports the same cube, X/Y rotations, perspective projection, and controls to
Python's standard-library `curses` module. Colored Unicode Braille dots provide
2x4 drawing points per character cell; an ASCII fallback is available.

## Run inside your terminal (Arch Linux / kitty / fish)

Requires Python 3.9+ with curses, supplied by Arch's `python` package. No Java,
pip packages, graphics libraries, or changes to Hyprland are needed for this mode.

```fish
sudo pacman -S --needed git python
git clone https://github.com/diyar-niyazov/Interactive-3D-Cube.git
cd Interactive-3D-Cube
python3 cube_terminal.py
```

If you already cloned the repository, update that checkout before running the
last command. The terminal version fills the current terminal, adjusts to
resizing and cell proportions, and restores the shell when you quit.

To install it as a standalone `cube` command, run these inside the repository:

```fish
install -Dm755 cube_terminal.py ~/.local/bin/cube
fish_add_path ~/.local/bin
cube
```

The installed script is a copy: repeat the `install` command after updating it.
To uninstall, run `rm ~/.local/bin/cube`.

### Terminal controls

| Key | Action |
| --- | --- |
| Arrows | Start rotation on that axis; the opposite arrow stops it, then another press reverses it |
| `Space` | Pause / resume |
| `W` / `S` | Zoom in / out |
| `,` / `.` | Multiply speed by 0.75 / 1.25 (bounded to 0.05x–16x) |
| `1`…`0` | Choose the colors listed below |
| `=` / `-` | Show / hide vertices |
| `R` | Reset |
| `?` or `H` | Show / hide help |
| `Q`, `Esc`, or `Ctrl-C` | Quit |

```fish
cube --fps 30                 # Lower frame rate
cube --ascii                 # Plain ASCII if Braille glyphs look wrong
cube --no-color              # Monochrome
cube --cell-aspect 0.5        # Override character width / height if needed
```

You can also pass these options to `python3 cube_terminal.py`. Use at least a
40-column by 10-row terminal; 80x24 or larger leaves more room for the controls.
Braille mode needs a UTF-8 locale and a font with Braille glyphs. Color shades
depend on the terminal palette; eight-color terminals use approximate colors.
If you get a missing terminal-description error in kitty, install
`kitty-terminfo` with `sudo pacman -S --needed kitty-terminfo`.

### Check the terminal version

```sh
python3 -m unittest discover -s tests -v
```

Tests cover geometry, controls, rasterization, real pseudo-terminal input,
resizing, and terminal restoration on quit, Ctrl-C, and SIGTERM. The integration
tests use `xterm-256color`; set `CUBE_TEST_TERM=xterm-kitty` to exercise kitty's
terminfo when it is installed.

## Demo

https://github.com/user-attachments/assets/0684d920-e3f0-4db1-9e80-3e6e825708c9

## Build & Run the Java desktop version

Requires JDK 17+.
```bash
git clone https://github.com/diyar-niyazov/Interactive-3D-Cube.git
cd Interactive-3D-Cube
javac -d bin src/*.java
java -cp bin Main
```

## Controls

### Rotation

| Key     | Action                                             |
| ------- | -------------------------------------------------- |
| `↑`     | Start/stop rotation around the X-axis upward       |
| `↓`     | Start/stop rotation around the X-axis downward     |
| `←`     | Start/stop rotation around the Y-axis to the left  |
| `→`     | Start/stop rotation around the Y-axis to the right |
| `Space` | Pause/resume rotation                              |

### Perspective

| Key | Action                        |
| --- | ----------------------------- |
| `W` | Move projection plane closer  |
| `S` | Move projection plane farther |

### Line Color

| Key | Color      |
| --- | ---------- |
| `1` | Red        |
| `2` | Orange     |
| `3` | Yellow     |
| `4` | Green      |
| `5` | Blue       |
| `6` | Magenta    |
| `7` | Pink       |
| `8` | Dark Gray  |
| `9` | Light Gray |
| `0` | White      |

### Vertices

| Key | Action        |
| --- | ------------- |
| `=` | Show vertices |
| `-` | Hide vertices |

### Speed

| Key | Action       |
| --- | ------------ |
| ',' | Multiply speed by 0.75 |
| '.' | Multiply speed by 1.25 |
