@ECHO OFF

@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT" setlocal

if "%OS%" == "Windows_NT" (
  set "DIRNAME=%~dp0%"
) else (
  set DIRNAME=.\
)

set DEF_PATHS=gui-defaults.properties


set CP=%DIRNAME%/lib/*;%DIRNAME%/config/*


ARGS=-Dlog4j.configurationFile=%DIRNNAME%\config\log4j2-gui.xml
ARGS=%ARGS% -Dbqt.gui.default.paths=%DIRNNAME%\%DEF_PATHS%


java -cp %CP% %ARGS% org.jboss.bqt.gui.GUIClient