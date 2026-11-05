@echo off
cd /d "%~dp0\.."
py -c "import cv2" 2>nul || py -m pip install opencv-python
if errorlevel 1 (
  pause
  exit /b 1
)
py tools\webcam_bridge.py
pause
