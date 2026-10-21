@echo off
title Student Budget Tracker - Dev Mode
echo ===================================================
echo   Starting Student Budget Tracker (Dev Mode)
echo ===================================================
echo.
echo Database: In-Memory H2 (No MySQL setup needed)
echo Port:     8080
echo URL:      http://localhost:8080
echo.
echo Demo Account:
echo   Email:    alex.student@campus.edu
echo   Password: Student123!
echo.
echo Press Ctrl+C at any time to stop the application.
echo ===================================================
echo.

java -Dspring.profiles.active=dev -jar target\student-budget-tracker-1.0.0.jar

pause
