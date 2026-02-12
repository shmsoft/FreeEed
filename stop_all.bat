@echo off
echo Stopping FreeEed services (Windows)

REM --------------------------------------------------
REM Stop Tomcat
REM --------------------------------------------------
echo Stopping Tomcat...
cd freeeed-tomcat\bin
call shutdown.bat
cd ..\..

REM --------------------------------------------------
REM Stop Solr
REM --------------------------------------------------
echo Stopping Solr...
taskkill /F /FI "WINDOWTITLE eq FreeEed Solr" /T 2>nul
REM Fallback: kill by jar name if window title didn't work or for headless
wmic process where "CommandLine like '%%start.jar%%' and Name like '%%java%%'" call terminate 2>nul


REM --------------------------------------------------
REM Stop Tika
REM --------------------------------------------------
echo Stopping Tika...
taskkill /F /FI "WINDOWTITLE eq FreeEed Tika" /T 2>nul
REM Fallback
wmic process where "CommandLine like '%%tika-server.jar%%' and Name like '%%java%%'" call terminate 2>nul

REM --------------------------------------------------
REM Stop FreeEed Player
REM --------------------------------------------------
echo Stopping FreeEed Player...
taskkill /F /FI "WINDOWTITLE eq FreeEed Player" /T 2>nul
REM Fallback
wmic process where "CommandLine like '%%FreeEedUI%%' and Name like '%%java%%'" call terminate 2>nul

REM --------------------------------------------------
REM Stop Python Backend
REM --------------------------------------------------
echo Stopping Python Backend...
taskkill /F /FI "WINDOWTITLE eq FreeEed Python Backend" /T 2>nul
REM Fallback
wmic process where "CommandLine like '%%uvicorn%%' and Name like '%%python%%'" call terminate 2>nul


echo All services stopped.
