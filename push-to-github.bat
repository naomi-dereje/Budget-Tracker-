@echo off
title Push Student Budget Tracker to GitHub
echo ========================================================
echo   Pushing Student Budget Tracker to GitHub
echo ========================================================
echo Account:    naomi-dereje
echo Repository: student-budget-tracker
echo Remote URL: https://github.com/naomi-dereje/student-budget-tracker.git
echo.
echo Make sure you have created the empty repository on GitHub first:
echo   https://github.com/new  (Name: student-budget-tracker)
echo.
echo --------------------------------------------------------
echo Attempting to push... (Git will prompt for your GitHub login or token)
echo --------------------------------------------------------
echo.

tools\git\cmd\git.exe push -u origin main

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================================
    echo   SUCCESS! Pushed to https://github.com/naomi-dereje/student-budget-tracker
    echo ========================================================
) else (
    echo.
    echo --------------------------------------------------------
    echo If GitHub prompted for credentials:
    echo   1. Use a GitHub Personal Access Token (PAT) as password.
    echo      Create one here: https://github.com/settings/tokens
    echo      (Select 'repo' scope).
    echo   2. Or run:
    echo      tools\git\cmd\git.exe push https://YOUR_TOKEN@github.com/naomi-dereje/student-budget-tracker.git main
    echo --------------------------------------------------------
)

echo.
pause
