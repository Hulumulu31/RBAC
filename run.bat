@echo off
echo Compiling RBAC Console Application...
javac -d . src\main\java\*.java
if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b %errorlevel%
)

echo Compilation successful!
echo Starting RBAC Console Application...
echo.
java RBACConsoleApp

pause