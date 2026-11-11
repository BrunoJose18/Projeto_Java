@echo off
chcp 65001 >nul
cd /d "%~dp0"
if not exist out mkdir out
javac -encoding UTF-8 --release 17 -cp "lib/*" -d out src/*.java
if errorlevel 1 (
  echo Falha ao compilar. Confira se um JDK 17 ou superior esta instalado.
  pause
  exit /b 1
)
java -cp "out;lib/*" Sistema
pause
