"""Run after compiling. Requires Python 3; no external packages."""
from pathlib import Path
import subprocess
root = Path(__file__).resolve().parents[1]
base = ['java', '-Djava.awt.headless=true', '-cp', str(root / 'build'), 'PresidentialSimulator']
def run(lines, *args):
    p = subprocess.run(base + list(args), input=lines, text=True, capture_output=True, timeout=15)
    assert p.returncode == 0, p.stderr
    assert 'Exception' not in p.stderr, p.stderr
    return p.stdout + p.stderr
checks = [
    ('full console loss', 'You: 127 EV | Opponent: 411 EV | Total: 538', run('1\n1\n' + '5\n' * 8, '--seed', '42', '--no-gui')),
    ('bad input recovery', 'Enter a whole number from 1 to 2.', run('oops\n0\n3\n1\n1\n8\n', '--no-gui')),
    ('EOF cancellation', 'input closed. No election was run.', run('1\n', '--no-gui')),
    ('view actions cost no turns', 'Turn 1/8', run('1\n1\n6\n7\n8\n', '--no-gui')),
    ('headless fallback', 'No desktop detected', run('2\n4\n' + '5\n' * 8, '--seed', '42')),
    ('bad seed', 'Usage:', run('', '--seed', 'not-a-number')),
    ('missing seed', 'Usage:', run('', '--seed')),
    ('unknown flag', 'Usage:', run('', '--unknown')),
    ('cancel state target', 'Campaign cancelled. No election was run.', run('1\n1\n1\n0\n8\n', '--no-gui')),
]
# Win using board-reported objectives, so this exercises the actual text interface.
board = run('1\n3\n8\n', '--seed', '42', '--no-gui')
actions = {'Town hall': '1', 'Field office': '2', 'Outreach event': '3'}
script = ['1', '3', '2', '43', '2', '13'] # Texas and Illinois field offices
for number, name in [('5', 'California'), ('9', 'Florida')]:
    line = next(l for l in board.splitlines() if name in l and 'Complete [' in l)
    tasks = line.split('Complete [', 1)[1].split(']', 1)[0].split(', ')
    for task in tasks: script.extend([actions[task], number])
script.extend(['5', '5'])
checks.append(('full console win', 'You: 270 EV | Opponent: 268 EV | Total: 538', run('\n'.join(script) + '\n', '--seed', '42', '--no-gui')))
for name, expected, output in checks:
    assert expected in output, (name, output)
print(f'PASS: {len(checks)} console integration checks.')
