@echo off
setlocal
set GRADLE_VERSION=8.7
set BASE=%USERPROFILE%\.gradle\chk-wrapper\gradle-%GRADLE_VERSION%
if not exist "%BASE%\bin\gradle.bat" (
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$u='https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip'; $z='$env:TEMP\gradle-%GRADLE_VERSION%-bin.zip'; Invoke-WebRequest -UseBasicParsing $u -OutFile $z; New-Item -ItemType Directory -Force -Path '%USERPROFILE%\.gradle\chk-wrapper' | Out-Null; Expand-Archive -Force $z '%USERPROFILE%\.gradle\chk-wrapper'"
)
call "%BASE%\bin\gradle.bat" %*
endlocal
