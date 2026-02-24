@echo off
setlocal

:: Wrapper script to run the Control Panel on Windows

:: Get the directory of this script
cd /d "%~dp0"

:: Launch the Control Panel using Java.
:: We include the processing jar in the classpath
java -cp "FreeEed\target\*;FreeEed\target\lib\*;FreeEed\target\dependency\*;FreeEed\*" org.freeeed.ui.ControlPanelUI
