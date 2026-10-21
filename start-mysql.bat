@echo off
title Student Budget Tracker - MySQL Production Mode
echo ===================================================
echo   Starting Student Budget Tracker (MySQL Mode)
echo ===================================================
echo.
echo Make sure MySQL server is running and database is created.
echo (See database\schema.sql and database\seed_data.sql)
echo.
echo Port:     8080
echo URL:      http://localhost:8080
echo ===================================================
echo.

java -Dspring.profiles.active=mysql -jar target\student-budget-tracker-1.0.0.jar

pause
