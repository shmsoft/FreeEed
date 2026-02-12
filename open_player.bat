@echo off
echo Restarting FreeEed Player...

REM --------------------------------------------------
REM Stop FreeEed Player if running
REM --------------------------------------------------
taskkill /F /FI "WINDOWTITLE eq FreeEed Player" /T 2>nul
REM Fallback
wmic process where "CommandLine like '%%FreeEedUI%%' and Name like '%%java%%'" call terminate 2>nul

REM --------------------------------------------------
REM Start FreeEed Player
REM --------------------------------------------------
cd FreeEed
start "FreeEed Player" cmd /c freeeed_player.bat
cd ..
