"""Optional POSIX integration check; run after build.sh. Requires Python 3."""
import os, pathlib, sys
if os.name != 'posix':
    print('PTY check requires POSIX; use the Java renderer tests on Windows.')
    sys.exit(0)
import fcntl, pty, termios, struct, subprocess, time, select, re
root = pathlib.Path(__file__).resolve().parents[1]
master, slave = pty.openpty()
fcntl.fcntl(slave, fcntl.F_SETFD, 0)
fcntl.ioctl(slave, termios.TIOCSWINSZ, struct.pack('HHHH', 24, 80, 0, 0))
def setup():
    os.setsid()
    fcntl.ioctl(0, termios.TIOCSCTTY, 0)
p = subprocess.Popen(['java','-jar',str(root/'presidential-simulator.jar'),'--screen','--no-color','--width','100','--height','28'], stdin=slave,stdout=slave,stderr=slave,preexec_fn=setup,cwd=root)
os.close(slave)
captured = bytearray()
def until(wanted):
    end = time.monotonic() + 10
    part = bytearray()
    while wanted not in part and time.monotonic() < end:
        if select.select([master],[],[],.1)[0]:
            chunk = os.read(master,65536)
            part.extend(chunk);captured.extend(chunk)
    assert wanted in part, (wanted,part[-500:])
try:
    until(b'Welcome >')
    os.write(master,b'3\n');until(b'Help topic >')
    os.write(master,b'0\n');until(b'Welcome >')
    os.write(master,b'0\n');until(b'\x1b[?1049l')
    p.wait(timeout=5)
    assert p.returncode == 0
    positions = [(int(y),int(x)) for y,x in re.findall(rb'\x1b\[(\d+);(\d+)H',captured)]
    assert positions and max(y for y,x in positions) <= 23, 'Frame exceeds visible height'
    assert max(x for y,x in positions) <= 80, 'Frame exceeds visible width'
    assert captured.count(b'\x1b[2J') == 1, 'Repeated whole-screen clears'
    # The only newlines are the console echo of our three Enter presses.
    assert captured.count(b'\n') <= 3, 'Renderer appended output rows'
    print('PTY check passed: real 80x24 terminal, capped oversized request, help/back/exit; one initial clear, no renderer linefeeds.')
finally:
    if p.poll() is None:p.kill();p.wait()
    os.close(master)
