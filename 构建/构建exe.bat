@echo off
cd /d "%~sdp0"
echo [1/6] Build JAR...
cd /d "%~sdp0..\javasrc"
if exist "G:\apache-maven-3.9.9\bin\mvn.cmd" (
  call "G:\apache-maven-3.9.9\bin\mvn.cmd" clean package -DskipTests
) else (
  mvn clean package -DskipTests
)
if %errorlevel% neq 0 (echo BUILD FAIL & pause & exit /b)

echo [2/6] Copy JAR...
copy /Y "%~sdp0..\javasrc\target\eras-framework-1.0.0.jar" "%~sdp0eras-framework-1.0.0.jar" >nul
if not exist "%~sdp0..\dist\exe\Eras_APP\app" mkdir "%~sdp0..\dist\exe\Eras_APP\app"
copy /Y "%~sdp0..\javasrc\target\eras-framework-1.0.0.jar" "%~sdp0..\dist\exe\Eras_APP\app\" >nul

echo [3/6] Build JavaFX runtime...
set "JLINK_MP=G:\jdk21\jmods;%~sdp0javafx-lib\javafx-base-21-win.jar;%~sdp0javafx-lib\javafx-graphics-21-win.jar;%~sdp0javafx-lib\javafx-controls-21-win.jar;%~sdp0javafx-lib\javafx-fxml-21-win.jar;%~sdp0javafx-lib\javafx-swing-21-win.jar"
if exist "%~sdp0..\dist\exe\Eras_APP\custom-runtime" rmdir /s /q "%~sdp0..\dist\exe\Eras_APP\custom-runtime"
G:\jdk21\bin\jlink --module-path "%JLINK_MP%" --add-modules java.base,java.desktop,java.logging,java.xml,javafx.controls,javafx.fxml,javafx.swing --output "%~sdp0..\dist\exe\Eras_APP\custom-runtime" --no-header-files --no-man-pages
if %errorlevel% neq 0 (echo JLINK FAIL & pause & exit /b)

echo [4/6] Build EXE...
if exist "%~sdp0..\dist\exe\output" rmdir /s /q "%~sdp0..\dist\exe\output"
G:\jdk21\bin\jpackage --input "%~sdp0..\dist\exe\Eras_APP\app" --main-jar eras-framework-1.0.0.jar --main-class com.eras.ErasApp --name "Eras" --type app-image --runtime-image "%~sdp0..\dist\exe\Eras_APP\custom-runtime" --dest "%~sdp0..\dist\exe\output" --java-options "-Deras.basepath=$APPDIR\.." --java-options "-Djavax.net.ssl.trustStoreType=jks" --java-options "-Djavax.net.ssl.trustStorePassword=changeit" --java-options "-Djdk.tls.client.protocols=TLSv1.2"
if %errorlevel% neq 0 (echo JPACKAGE FAIL & pause & exit /b)

echo [5/6] Replace old EXE...
if exist "%~sdp0..\dist\exe\Eras_APP" rmdir /s /q "%~sdp0..\dist\exe\Eras_APP"
move "%~sdp0..\dist\exe\output\Eras" "%~sdp0..\dist\exe\Eras_APP"

echo [6/6] Create default config files...
if not exist "%~sdp0..\dist\exe\Eras_APP\options" mkdir "%~sdp0..\dist\exe\Eras_APP\options"
if not exist "%~sdp0..\dist\exe\Eras_APP\memory" mkdir "%~sdp0..\dist\exe\Eras_APP\memory"
if not exist "%~sdp0..\dist\exe\Eras_APP\picture\screen" mkdir "%~sdp0..\dist\exe\Eras_APP\picture\screen"
if not exist "%~sdp0..\dist\exe\Eras_APP\picture\character" mkdir "%~sdp0..\dist\exe\Eras_APP\picture\character"
if not exist "%~sdp0..\dist\exe\Eras_APP\prompt" mkdir "%~sdp0..\dist\exe\Eras_APP\prompt"

if not exist "%~sdp0..\dist\exe\Eras_APP\options\api.json" (
  copy nul "%~sdp0..\dist\exe\Eras_APP\options\api.json" >nul
  echo {}>> "%~sdp0..\dist\exe\Eras_APP\options\api.json"
)
if not exist "%~sdp0..\dist\exe\Eras_APP\options\name.json" (
  echo {"ai_name":"Good Helper","user_name":"You"} > "%~sdp0..\dist\exe\Eras_APP\options\name.json"
)
if not exist "%~sdp0..\dist\exe\Eras_APP\options\setting.json" (
  echo {"character":"","character_type":"auto","ai_text_color":"#FFFFFF","user_text_color":"#FFFFFF"} > "%~sdp0..\dist\exe\Eras_APP\options\setting.json"
)
if not exist "%~sdp0..\dist\exe\Eras_APP\memory\corememory.json" (
  echo {"ai_identity":"You are a helpful AI assistant","user_info":"User info here"} > "%~sdp0..\dist\exe\Eras_APP\memory\corememory.json"
)
if not exist "%~sdp0..\dist\exe\Eras_APP\memory\memory.json" (
  echo [] > "%~sdp0..\dist\exe\Eras_APP\memory\memory.json"
)

if exist "%~sdp0..\dist\exe\Eras_APP\Eras.exe" (
  echo Done. EXE: %~sdp0..\dist\exe\Eras_APP\Eras.exe
) else (
  echo EXE not found - check error above
)
pause
