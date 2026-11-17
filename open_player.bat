@echo off
echo Restarting FreeEed Player...

REM --------------------------------------------------
REM Always run from this script's own directory.
REM
REM FreeEed\ sits directly under this launcher -- in an installed build (the
REM NSIS installer copies the pack flat into $INSTDIR) and in an extracted zip
REM pack alike. The old line was "cd freeeed_complete_pack\FreeEed", a folder
REM that only exists on the build machine while the pack is being assembled.
REM
REM In an installed build that cd failed, batch carried on regardless, and
REM "start cmd /c freeeed_player.bat" ran in a directory with no such file --
REM so the Player window appeared for a moment and vanished, with the error
REM scrolling past inside a window that closed itself.
REM
REM open_player.sh and start_all.bat already anchor to their own directory
REM this way; the Player's .bat was the one that was missed.
REM --------------------------------------------------
cd /d "%~dp0"

REM --------------------------------------------------
REM Stop FreeEed Player if running
REM --------------------------------------------------
taskkill /F /FI "WINDOWTITLE eq FreeEed Player" /T 2>nul
REM Fallback
wmic process where "CommandLine like '%%FreeEedUI%%' and Name like '%%java%%'" call terminate 2>nul

REM --------------------------------------------------
REM Start FreeEed Player
REM
REM Fail loudly rather than flashing a window: if the layout is ever wrong
REM again, the operator sees why instead of watching the Player disappear.
REM --------------------------------------------------
if not exist "FreeEed\freeeed_player.bat" (
    echo.
    echo ERROR: FreeEed\freeeed_player.bat was not found under "%~dp0"
    echo This launcher must sit in the FreeEed installation root,
    echo alongside the FreeEed\ folder.
    echo.
    pause
    exit /b 1
)

cd /d "%~dp0FreeEed"
start "FreeEed Player" cmd /c freeeed_player.bat
cd /d "%~dp0"
