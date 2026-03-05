@echo off
setlocal

:: Wrapper script to run the Control Panel on Windows

:: Get the directory of this script
cd /d "%~dp0"

:: ---- Ensure %USERPROFILE%\.freeeed\.env exists on first launch ----
set "ENV_DIR=%USERPROFILE%\.freeeed"
set "ENV_PATH=%ENV_DIR%\.env"

if not exist "%ENV_DIR%" mkdir "%ENV_DIR%"

if not exist "%ENV_PATH%" (
    echo Creating default config at %ENV_PATH%...
    (
        echo # AI Advisor Configuration
        echo OPENAI_API_KEY=
        echo CHROMA_PERSIST_DIR=chroma_data
        echo LLM_MODEL=gpt-4o-mini
        echo CHROMA_EMBED_MODEL=text-embedding-3-small
        echo TOP_K=10
        echo PORT=8000
    ) > "%ENV_PATH%"
    echo IMPORTANT: Please edit %ENV_PATH% and add your OPENAI_API_KEY before starting.
)

:: Launch the Control Panel using Java.
:: We include the processing jar in the classpath
java -cp "FreeEed\target\*;FreeEed\target\lib\*;FreeEed\target\dependency\*;FreeEed\*" org.freeeed.ui.ControlPanelUI
