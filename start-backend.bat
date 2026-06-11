@echo off
:: Start Online Exam System Backend
:: Auto-restarts on crash

title Online Exam System - Backend

:START
echo [%date% %time%] Starting Online Exam System...
java -Xmx512m -Dserver.port=8080 -jar target\online-exam-system-1.0.0.jar
echo [%date% %time%] Process exited. Restarting in 3 seconds...
timeout /t 3 /nobreak > nul
goto START
