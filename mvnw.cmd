@REM ----------------------------------------------------------------------------
@REM Student Budget Tracker Maven Wrapper Script for Windows
@REM ----------------------------------------------------------------------------
@echo off
setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.

@REM Prefer local extracted maven if present
if exist "%DIRNAME%apache-maven-3.9.6\bin\mvn.cmd" (
    call "%DIRNAME%apache-maven-3.9.6\bin\mvn.cmd" %*
    exit /b %ERRORLEVEL%
)

@REM Check system PATH mvn
where mvn >nul 2>nul
if %ERRORLEVEL% equ 0 (
    mvn %*
    exit /b %ERRORLEVEL%
)

@REM Fallback: run java with maven wrapper jar if present
if exist "%DIRNAME%.mvn\wrapper\maven-wrapper.jar" (
    java -jar "%DIRNAME%.mvn\wrapper\maven-wrapper.jar" %*
    exit /b %ERRORLEVEL%
)

echo [ERROR] Maven not found. Please install Maven or check apache-maven-3.9.6.
exit /b 1
