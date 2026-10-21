@echo off
title Push Student Budget Tracker to GitHub
echo ========================================================
echo   Pushing Student Budget Tracker to GitHub
echo ========================================================
echo Repository: https://github.com/naomi-dereje/Budget-Tracker-.git
echo Branch:     main
echo ========================================================
echo.
echo You have two options to authenticate:
echo.
echo   Option 1: Paste a GitHub Personal Access Token (PAT) below.
echo             (Generate at https://github.com/settings/tokens with 'repo' scope)
echo.
echo   Option 2: Press ENTER to attempt normal browser sign-in.
echo.
set /p TOKEN="Enter your GitHub Token (or press ENTER): "

if "%TOKEN%"=="" (
    echo.
    echo Attempting push with default Git credentials...
    tools\git\cmd\git.exe push -u origin main
) else (
    echo.
    echo Pushing with provided Personal Access Token...
    tools\git\cmd\git.exe push https://naomi-dereje:%TOKEN%@github.com/naomi-dereje/Budget-Tracker-.git main
)

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================================
    echo   SUCCESS! Pushed to https://github.com/naomi-dereje/Budget-Tracker-
    echo ========================================================
) else (
    echo.
    echo --------------------------------------------------------
    echo If push failed:
    echo   1. If the remote repository has initial commits, you may need force push:
    echo      tools\git\cmd\git.exe push -f https://naomi-dereje:%TOKEN%@github.com/naomi-dereje/Budget-Tracker-.git main
    echo   2. Double check your GitHub token has the 'repo' scope checked.
    echo --------------------------------------------------------
)

echo.
pause
