# Terminal stability fix

## Causes addressed

The prior plain-mode countdown appended a new line per tick. That directly moved the transcript down. Full-screen rendering also used sequential lines and repeated whole-screen clears; an oversized frame could overflow the visible window. The Windows sizing probe used `mode con`, whose buffer geometry is not a reliable visible-window measurement.

The replacement Windows probe opens CONOUT$ and reads the visible rectangle (`srWindow`) from GetConsoleScreenBufferInfo. Microsoft distinguishes [screen-buffer size from the displayed window rectangle](https://learn.microsoft.com/en-us/windows/console/console-screen-buffer-info-str). Cursor-position and save/restore behavior follow [Microsoft's virtual-terminal reference](https://learn.microsoft.com/en-us/windows/console/console-virtual-terminal-sequences).

## Changes

- Full-screen frames use absolute row positions and update only changed rows. Normal redraws do not append newline characters or repeatedly erase the entire screen.
- Countdown ticks modify only the footer timer cells and restore the input cursor, preserving typed input.
- Automatic line wrapping is disabled during the full-screen session and re-enabled on exit. A spare row beneath the input line accommodates normal Enter-key echo.
- Requested launch dimensions are capped to the detected visible viewport. Too-small detected windows receive an enlargement message.
- The Windows double-click launcher explicitly requests full-screen mode; `--plain` can still override it.
- Plain mode remains a transcript. It shows the initial clock budget but does not print a new line for every tick. Real-time expiration still happens.

## Evidence and limits

393 deterministic screen-model assertions passed at 60×20, 80×24, 100×28 and 120×40. They check that 59 successive ticks change only the timer row, preserve partially typed input and cursor position, and produce no scrolling. They also check redraws after Enter, initial-only screen clearing, and restored wrapping on exit.

The 85 UI integration checks still pass, including real elapsed-time expiry, help pause/resume, saving and decision progression. A real Linux PTY check verified viewport clamping, help navigation and clean exit in 80×24 with no renderer linefeeds.

Windows host behavior has not been executed on this Linux environment. Automatic detection of resizing during an active prompt remains future work. This is still a terminal UI with Enter-based input, not a native desktop GUI.
