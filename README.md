# Interactive 3D Cube

A Java implementation of a **rotatable 3D cube rendered from scratch using Java Swing**.
The cube is made of 3D points that are rotated every frame and then projected onto a 2D screen, where they are drawn using Java AWT Graphics.

## Demo

https://github.com/user-attachments/assets/0684d920-e3f0-4db1-9e80-3e6e825708c9

## Build & Run

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
| ',' | -0.25x speed |
| '.' | +0.25x speed |

## Build & Run

Requires JDK 17+.

git clone https://github.com/diyar-niyazov/Interactive-3D-Cube.git
cd Interactive-3D-Cube
javac -d bin src/*.java
java -cp bin Main
