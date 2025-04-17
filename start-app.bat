@echo off
echo Starting ngrok...
C:\Windows\System32\WindowsPowerShell\v1.0\powershell.exe -ExecutionPolicy Bypass -Command "& { cd 'E:\My Ultimate Workspace\Project KHMT\HotelSystemManagement'; . '.\scripts\start-ngrok.ps1' }"
if %ERRORLEVEL% NEQ 0 (
    echo Failed to start ngrok
    exit /b %ERRORLEVEL%
)