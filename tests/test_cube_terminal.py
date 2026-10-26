import curses
import math
import unittest

from cube_terminal import COLORS, EDGES, Canvas, Cube


class CubeTests(unittest.TestCase):
    def test_rotation_preserves_cube_geometry(self):
        for x, y in ((0, 0), (0.37, 1.21), (math.pi, math.pi / 2)):
            vertices = Cube(x_angle=x, y_angle=y).rotated_vertices()
            for vertex in vertices:
                self.assertAlmostEqual(sum(v * v for v in vertex), 3)
            for start, end in EDGES:
                self.assertAlmostEqual(math.dist(vertices[start], vertices[end]), 2)

    def test_quarter_turn_matches_swing_rotation_order(self):
        cube = Cube(x_angle=math.pi / 2, y_angle=math.pi / 2)
        # (-1, -1, -1) -> rotate X (-1, 1, -1) -> rotate Y (-1, 1, 1).
        for actual, expected in zip(cube.rotated_vertices()[0], (-1, 1, 1)):
            self.assertAlmostEqual(actual, expected)

    def test_speed_is_independent_of_frame_rate(self):
        first, second = Cube(x_direction=1), Cube(x_direction=1)
        for _ in range(30):
            first.advance(1 / 30)
        for _ in range(120):
            second.advance(1 / 120)
        self.assertAlmostEqual(first.x_angle, math.pi / 2)
        self.assertAlmostEqual(first.x_angle, second.x_angle)
        self.assertAlmostEqual(first.y_angle, second.y_angle)

    def test_pause_restores_both_rotation_directions(self):
        cube = Cube(x_direction=1)
        cube.handle_key(" ")
        cube.advance(1)
        self.assertEqual((cube.x_angle, cube.y_angle), (0, 0))
        cube.handle_key(" ")
        self.assertEqual((cube.x_direction, cube.y_direction), (1, -1))

    def test_opposite_arrow_stops_then_reverses(self):
        cube = Cube()
        cube.handle_key(curses.KEY_RIGHT)
        self.assertEqual(cube.y_direction, -1)
        cube.handle_key(curses.KEY_LEFT)
        self.assertEqual(cube.y_direction, 0)
        cube.handle_key(curses.KEY_LEFT)
        self.assertEqual(cube.y_direction, 1)
        cube.handle_key(curses.KEY_UP)
        self.assertEqual(cube.x_direction, 1)
        cube.handle_key(curses.KEY_DOWN)
        self.assertEqual(cube.x_direction, 0)
        cube.handle_key(curses.KEY_DOWN)
        self.assertEqual(cube.x_direction, -1)

    def test_zoom_and_speed_are_bounded(self):
        cube = Cube()
        for _ in range(1000):
            cube.handle_key("w")
            cube.handle_key(",")
        self.assertEqual(cube.distance, 2.5)
        self.assertEqual(cube.speed, 0.05)
        for _ in range(1000):
            cube.handle_key("S")
            cube.handle_key(".")
        self.assertEqual(cube.distance, 50)
        self.assertEqual(cube.speed, 16)

    def test_display_controls_reset_and_quit(self):
        cube = Cube()
        for key in COLORS:
            cube.handle_key(key)
            self.assertEqual(cube.color, key)
        cube.handle_key("=")
        self.assertTrue(cube.show_points)
        cube.handle_key("-")
        self.assertFalse(cube.show_points)
        cube.handle_key("?")
        self.assertTrue(cube.help_visible)
        cube.handle_key("H")
        self.assertFalse(cube.help_visible)
        cube.advance(1)
        cube.handle_key("R")
        self.assertEqual(cube, Cube())
        for key in ("q", "Q", "\x1b", "\x03"):
            self.assertFalse(cube.handle_key(key))

    def test_projection_fits_tall_wide_and_small_terminals(self):
        for width, height, aspect in ((76, 24, 1), (316, 48, 1), (76, 200, 1),
                                       (78, 20, 0.5), (158, 80, 0.8)):
            for x in range(0, 360, 30):
                for y in range(0, 360, 30):
                    cube = Cube(x_angle=math.radians(x), y_angle=math.radians(y), distance=2.5)
                    for px, py in cube.projected_vertices(width, height, aspect):
                        self.assertTrue(0 <= px < width)
                        self.assertTrue(0 <= py < height)

    def test_cell_aspect_compensation(self):
        cube = Cube(distance=3)
        points = cube.projected_vertices(160, 80, 0.8)
        horizontal = abs(points[1][0] - points[0][0]) * 0.8
        vertical = abs(points[3][1] - points[0][1])
        self.assertAlmostEqual(horizontal, vertical)


class CanvasTests(unittest.TestCase):
    def test_braille_dot_positions(self):
        for x, y, expected in ((0, 0, 1), (1, 0, 8), (0, 3, 64), (1, 3, 128)):
            canvas = Canvas(1, 1)
            canvas.pixel(x, y)
            self.assertEqual(list(canvas.lines()), [chr(0x2800 + expected)])
        canvas = Canvas(1, 1)
        for y in range(4):
            for x in range(2):
                canvas.pixel(x, y)
        self.assertEqual(list(canvas.lines()), ["\u28ff"])

    def test_lines_include_both_endpoints_in_every_direction(self):
        for target in ((0, 0), (0, 5), (5, 0), (9, 9), (4, 9), (9, 4), (5, 5)):
            for start, end in (((5, 5), target), (target, (5, 5))):
                canvas = Canvas(10, 10, ascii_mode=True)
                canvas.line(start, end)
                lines = list(canvas.lines())
                self.assertEqual(lines[start[1]][start[0]], "#")
                self.assertEqual(lines[end[1]][end[0]], "#")

    def test_offscreen_pixels_are_clipped(self):
        canvas = Canvas(2, 2, ascii_mode=True)
        canvas.line((-2, 0), (3, 0))
        canvas.pixel(0, -1)
        canvas.pixel(0, 2)
        self.assertEqual(list(canvas.lines()), ["##", "  "])


if __name__ == "__main__":
    unittest.main()
