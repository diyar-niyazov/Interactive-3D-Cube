"""Exercise real terminal input, resize, and cleanup without a desktop."""

import fcntl
import os
from pathlib import Path
import select
import signal
import struct
import subprocess
import sys
import termios
import time
import unittest


SCRIPT = Path(__file__).resolve().parents[1] / "cube_terminal.py"


class TerminalTests(unittest.TestCase):
    def setUp(self):
        self.master, self.slave = os.openpty()
        self.original = termios.tcgetattr(self.slave)
        self.proc = None
        self.resize(24, 80)

    def tearDown(self):
        if self.proc is not None and self.proc.poll() is None:
            self.proc.kill()
            self.proc.wait(timeout=3)
        os.close(self.master)
        os.close(self.slave)

    def resize(self, rows, columns):
        fcntl.ioctl(self.slave, termios.TIOCSWINSZ, struct.pack("HHHH", rows, columns, 0, 0))
        if self.proc is not None:
            self.proc.send_signal(signal.SIGWINCH)

    def start(self, *args, term=None):
        env = dict(os.environ, TERM=term or os.environ.get("CUBE_TEST_TERM", "xterm-256color"),
                   LC_ALL="C.UTF-8")
        for key in ("LINES", "COLUMNS"):
            env.pop(key, None)
        self.proc = subprocess.Popen(
            [sys.executable, str(SCRIPT), "--fps", "30", *args],
            stdin=self.slave, stdout=self.slave, stderr=self.slave,
            env=env, start_new_session=True,
            preexec_fn=lambda: fcntl.ioctl(0, termios.TIOCSCTTY, 0),
        )
        return self.read_until(b"ROTATING")

    def read_until(self, needle):
        data = bytearray()
        deadline = time.monotonic() + 5
        while time.monotonic() < deadline:
            readable, _, _ = select.select([self.master], [], [], 0.05)
            if readable:
                data.extend(os.read(self.master, 65536))
                if needle in data:
                    return bytes(data)
            if self.proc.poll() is not None:
                break
        self.fail(f"Missing {needle!r} in terminal output: {bytes(data)[-1000:]!r}")

    def assert_clean_exit(self, status):
        self.assertEqual(self.proc.wait(timeout=5), status)
        self.assertEqual(termios.tcgetattr(self.slave), self.original)

    def test_interactive_controls_resize_and_quit(self):
        data = self.start()
        self.assertTrue(any("\u2800" <= c <= "\u28ff" for c in data.decode("utf-8")))
        os.write(self.master, b" ")
        self.read_until(b"PAUSED")
        # Application-mode arrows emitted by kitty with keypad enabled.
        os.write(self.master, b"\x1bOA")
        self.read_until(b"ROTATING")
        os.write(self.master, b"?")
        self.read_until(b"Opposite arrow stops it")
        os.write(self.master, b"?")
        self.resize(5, 20)
        self.read_until(b"Enlarge terminal")
        self.resize(40, 120)
        self.read_until(b"ROTATING")
        os.write(self.master, b"q")
        self.assert_clean_exit(0)

    def test_ascii_monochrome_and_escape(self):
        data = self.start("--ascii", "--no-color")
        self.assertIn(b"#", data)
        self.assertFalse(any("\u2800" <= c <= "\u28ff" for c in data.decode("utf-8")))
        os.write(self.master, b"\x1b")
        self.assert_clean_exit(0)

    def test_ctrl_c_restores_terminal(self):
        self.start()
        os.write(self.master, b"\x03")
        self.assert_clean_exit(130)

    def test_termination_signal_restores_terminal(self):
        self.start()
        self.proc.terminate()
        self.assert_clean_exit(143)

    def test_redirected_launch_fails_without_escape_sequences(self):
        result = subprocess.run([sys.executable, str(SCRIPT)], capture_output=True, timeout=5)
        self.assertEqual(result.returncode, 1)
        self.assertIn(b"interactive terminal", result.stderr)
        self.assertNotIn(b"\x1b", result.stdout + result.stderr)


if __name__ == "__main__":
    unittest.main()
