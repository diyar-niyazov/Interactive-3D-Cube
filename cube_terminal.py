#!/usr/bin/env python3
"""The Swing cube's rotation and perspective math, drawn in a Unix terminal."""

import argparse
import curses
from dataclasses import dataclass
import fcntl
import locale
import math
import os
import signal
import struct
import sys
import termios
import time


VERTICES = (
    (-1, -1, -1), (1, -1, -1), (1, 1, -1), (-1, 1, -1),
    (-1, -1, 1), (1, -1, 1), (1, 1, 1), (-1, 1, 1),
)
EDGES = (
    (0, 1), (1, 2), (2, 3), (3, 0),
    (4, 5), (5, 6), (6, 7), (7, 4),
    (0, 4), (1, 5), (2, 6), (3, 7),
)
# xterm's 256-color palette, with standard eight-color fallbacks.
COLORS = {
    "1": (196, curses.COLOR_RED, "red"),
    "2": (214, curses.COLOR_YELLOW, "orange"),
    "3": (226, curses.COLOR_YELLOW, "yellow"),
    "4": (46, curses.COLOR_GREEN, "green"),
    "5": (33, curses.COLOR_BLUE, "blue"),
    "6": (201, curses.COLOR_MAGENTA, "magenta"),
    "7": (213, curses.COLOR_MAGENTA, "pink"),
    "8": (240, curses.COLOR_WHITE, "dark gray"),
    "9": (250, curses.COLOR_WHITE, "light gray"),
    "0": (15, curses.COLOR_WHITE, "white"),
}
HELP = (
    "INTERACTIVE 3D CUBE",
    "",
    "Arrows     Start rotation on an axis.",
    "           Opposite arrow stops it; press again to reverse.",
    "Space      Pause / resume",
    "W / S      Zoom in / out",
    ", / .      Slower / faster (x0.75 / x1.25)",
    "1..0       Red, orange, yellow, green, blue, magenta,",
    "           pink, dark gray, light gray, white",
    "= / -      Show / hide vertices",
    "R          Reset",
    "? / H      Toggle this help",
    "Q / Esc    Quit (Ctrl-C also works)",
)


@dataclass
class Cube:
    x_angle: float = 0.0
    y_angle: float = 0.0
    x_direction: int = 0
    y_direction: int = -1
    resume_directions: tuple = (0, -1)
    distance: float = 3.0
    speed: float = 1.0
    color: str = "4"
    show_points: bool = False
    help_visible: bool = False

    @property
    def paused(self):
        return not (self.x_direction or self.y_direction)

    def advance(self, seconds):
        # Same 90 degrees/second base speed as Panel.java, independent of FPS.
        step = seconds * math.pi / 2 * self.speed
        self.x_angle = (self.x_angle + step * self.x_direction) % math.tau
        self.y_angle = (self.y_angle + step * self.y_direction) % math.tau

    def handle_key(self, key):
        """Return False to quit. Direction keys match the Swing controls."""
        if isinstance(key, str):
            key = key.lower()
        if key in ("q", "\x1b", "\x03"):
            return False
        directions = {
            curses.KEY_UP: ("x_direction", 1),
            curses.KEY_DOWN: ("x_direction", -1),
            curses.KEY_LEFT: ("y_direction", 1),
            curses.KEY_RIGHT: ("y_direction", -1),
        }
        if key in directions:
            axis, direction = directions[key]
            current = getattr(self, axis)
            setattr(self, axis, 0 if current == -direction else direction)
        elif key == " ":
            if self.paused:
                self.x_direction, self.y_direction = self.resume_directions
            else:
                self.resume_directions = (self.x_direction, self.y_direction)
                self.x_direction = self.y_direction = 0
        elif key == "w":
            self.distance = max(2.5, round(self.distance - 0.1, 10))
        elif key == "s":
            self.distance = min(50.0, round(self.distance + 0.1, 10))
        elif key == ",":
            self.speed = max(0.05, self.speed * 0.75)
        elif key == ".":
            self.speed = min(16.0, self.speed * 1.25)
        elif key in COLORS:
            self.color = key
        elif key == "=":
            self.show_points = True
        elif key == "-":
            self.show_points = False
        elif key in ("?", "h"):
            self.help_visible = not self.help_visible
        elif key == "r":
            self.__dict__.update(Cube().__dict__)
        return True

    def rotated_vertices(self):
        cx, sx = math.cos(self.x_angle), math.sin(self.x_angle)
        cy, sy = math.cos(self.y_angle), math.sin(self.y_angle)
        result = []
        for x, y, z in VERTICES:
            y, z = y * cx - z * sx, y * sx + z * cx
            x, z = x * cy + z * sy, -x * sy + z * cy
            result.append((x, y, z))
        return result

    def projected_vertices(self, width, height, pixel_aspect):
        # One terminal cell is usually about twice as tall as it is wide.
        scale = 0.46 * min(height - 1, (width - 1) * pixel_aspect)
        return [
            ((width - 1) / 2 + x / (z + self.distance) * scale / pixel_aspect,
             (height - 1) / 2 - y / (z + self.distance) * scale)
            for x, y, z in self.rotated_vertices()
        ]


class Canvas:
    """Rasterize lines into 2x4 Braille dots or one ASCII pixel per cell."""

    DOTS = ((1, 8), (2, 16), (4, 32), (64, 128))

    def __init__(self, columns, rows, ascii_mode=False):
        self.ascii_mode = ascii_mode
        self.x_dots, self.y_dots = (1, 1) if ascii_mode else (2, 4)
        self.width, self.height = columns * self.x_dots, rows * self.y_dots
        self.cells = [bytearray(columns) for _ in range(rows)]

    def pixel(self, x, y):
        if 0 <= x < self.width and 0 <= y < self.height:
            bit = 1 if self.ascii_mode else self.DOTS[y % 4][x % 2]
            self.cells[y // self.y_dots][x // self.x_dots] |= bit

    def line(self, start, end):
        x0, y0 = map(round, start)
        x1, y1 = map(round, end)
        dx, dy = abs(x1 - x0), -abs(y1 - y0)
        sx, sy = (1 if x0 < x1 else -1), (1 if y0 < y1 else -1)
        error = dx + dy
        while True:
            self.pixel(x0, y0)
            if (x0, y0) == (x1, y1):
                break
            twice = 2 * error
            if twice >= dy:
                error += dy
                x0 += sx
            if twice <= dx:
                error += dx
                y0 += sy

    def lines(self):
        for row in self.cells:
            if self.ascii_mode:
                yield "".join("#" if bit else " " for bit in row)
            else:
                yield "".join(chr(0x2800 + bit) if bit else " " for bit in row)


def cell_aspect(fd):
    """Use terminal pixel dimensions when available (including kitty)."""
    try:
        rows, columns, pixels_x, pixels_y = struct.unpack(
            "HHHH", fcntl.ioctl(fd, termios.TIOCGWINSZ, b"\0" * 8)
        )
        if all((rows, columns, pixels_x, pixels_y)):
            return (pixels_x / columns) / (pixels_y / rows)
    except (OSError, ValueError):
        pass
    return 0.5


def palette(no_color):
    attributes = {key: 0 for key in COLORS}
    if no_color or not curses.has_colors():
        return attributes
    background = curses.COLOR_BLACK
    try:
        curses.use_default_colors()
        background = -1  # Preserve kitty's background and transparency.
    except curses.error:
        pass
    for pair, (key, (extended, basic, _)) in enumerate(COLORS.items(), 1):
        if pair >= curses.COLOR_PAIRS:
            break
        curses.init_pair(pair, extended if curses.COLORS >= 256 else basic, background)
        attributes[key] = curses.color_pair(pair)
    return attributes


def put(screen, y, x, text, attr=0):
    """Clip text and tolerate a resize between measuring and drawing."""
    rows, columns = screen.getmaxyx()
    if 0 <= y < rows and 0 <= x < columns - 1:
        try:
            screen.addnstr(y, x, text, columns - x - 1, attr)
        except curses.error:
            pass


def draw(screen, cube, args, attributes):
    rows, columns = screen.getmaxyx()
    screen.erase()
    if rows < 10 or columns < 40:
        put(screen, 0, 0, "Enlarge terminal to 40x10. Q quits.")
    elif cube.help_visible:
        for y, line in enumerate(HELP):
            put(screen, y + 1, 2, line)
        put(screen, rows - 1, 1, "Press ? or H to return; Q quits.")
    else:
        canvas = Canvas(columns - 2, rows - 4, args.ascii)
        aspect = args.cell_aspect or cell_aspect(sys.stdout.fileno())
        points = cube.projected_vertices(
            canvas.width, canvas.height, aspect * canvas.y_dots / canvas.x_dots
        )
        for start, end in EDGES:
            canvas.line(points[start], points[end])
        attr = attributes[cube.color]
        for row, line in enumerate(canvas.lines(), 1):
            put(screen, row, 1, line, attr)
        if cube.show_points:
            for x, y in points:
                put(screen, 1 + round(y) // canvas.y_dots,
                    1 + round(x) // canvas.x_dots, "o", attributes["0"] | curses.A_BOLD)
        put(screen, 0, 1, "INTERACTIVE 3D CUBE", curses.A_BOLD)
        state = "PAUSED" if cube.paused else "ROTATING"
        put(screen, rows - 3, 1,
            f"{state}  {cube.speed:.2f}x  distance {cube.distance:.1f}  {COLORS[cube.color][2]}")
        put(screen, rows - 2, 1, "Arrows rotate | Space pause | W/S zoom | ,/. speed")
        put(screen, rows - 1, 1, "0-9 color | =/- vertices | R reset | ? help | Q quit")
    screen.refresh()


def run(screen, args):
    try:
        curses.curs_set(0)
    except curses.error:
        pass
    curses.set_escdelay(25)
    screen.nodelay(True)
    screen.keypad(True)
    attributes = palette(args.no_color)
    cube = Cube()
    last_frame = time.monotonic()
    while True:
        frame_start = time.monotonic()
        cube.advance(min(frame_start - last_frame, 0.1))
        last_frame = frame_start
        # Bound work per frame even if a large paste arrives.
        for _ in range(64):
            try:
                key = screen.get_wch()
            except curses.error:
                break
            if not cube.handle_key(key):
                return
        draw(screen, cube, args, attributes)
        time.sleep(max(0.0, 1 / args.fps - (time.monotonic() - frame_start)))


def positive_aspect(value):
    aspect = float(value)
    if not math.isfinite(aspect) or not 0.1 <= aspect <= 2:
        raise argparse.ArgumentTypeError("cell aspect must be between 0.1 and 2")
    return aspect


def stop_on_signal(signum, _frame):
    # Raising unwinds curses.wrapper so echo and the cursor are restored.
    raise SystemExit(128 + signum)


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--ascii", action="store_true", help="use ASCII instead of Braille dots")
    parser.add_argument("--fps", type=int, choices=range(1, 121), default=60, metavar="1-120",
                        help="frame rate (default: 60; use 30 to reduce CPU use)")
    parser.add_argument("--no-color", action="store_true", help="disable colors")
    parser.add_argument("--cell-aspect", type=positive_aspect, metavar="RATIO",
                        help="cell width/height (default: auto-detect, or 0.5)")
    args = parser.parse_args(argv)
    if not sys.stdin.isatty() or not sys.stdout.isatty():
        parser.exit(1, "cube: run this in an interactive terminal, without a pipe or redirect.\n")
    if os.environ.get("TERM", "dumb") in ("", "dumb"):
        parser.exit(1, "cube: a terminal such as kitty is required (TERM is unset or dumb).\n")
    locale.setlocale(locale.LC_ALL, "")
    try:
        "\u28ff".encode(locale.getpreferredencoding(False))
    except UnicodeEncodeError:
        args.ascii = True
    # Check before wrapper: initscr can exit the process on unknown terminals.
    try:
        curses.setupterm(fd=sys.stdout.fileno())
    except curses.error:
        parser.exit(1, "cube: terminal description missing; install your terminal's terminfo.\n"
                       "On Arch with kitty: sudo pacman -S --needed kitty-terminfo\n")
    previous = {}
    try:
        for sig in (signal.SIGTERM, signal.SIGHUP):
            previous[sig] = signal.signal(sig, stop_on_signal)
        curses.wrapper(run, args)
    except KeyboardInterrupt:
        return 130
    except curses.error as error:
        print(f"cube: terminal error: {error}", file=sys.stderr)
        return 1
    finally:
        for sig, handler in previous.items():
            signal.signal(sig, handler)
    return 0


if __name__ == "__main__":
    sys.exit(main())
