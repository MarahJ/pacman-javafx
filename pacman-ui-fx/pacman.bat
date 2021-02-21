REM Usage: pacman.bat or pacman.bat -mspacman
 
set "JAVA_HOME=C:\Program Files\Java\jdk-15.0.2"
set "JFX_HOME=C:\Program Files\Java\javafx-sdk-15.0.1"
set "JFX_RT=C:\Users\armin\.m2\repository"

set "JFX_LIBS="%JFX_HOME%\lib\javafx.base.jar";"%JFX_HOME%\lib\javafx.controls.jar";"%JFX_HOME%\lib\javafx.graphics.jar";"%JFX_HOME%\lib\javafx.media.jar";"%JFX_RT%\org\openjfx\javafx-controls\15\javafx-controls-15.jar";"%JFX_RT%\org\openjfx\javafx-controls\15\javafx-controls-15-win.jar";"%JFX_RT%\org\openjfx\javafx-graphics\15\javafx-graphics-15.jar";"%JFX_RT%\org\openjfx\javafx-graphics\15\javafx-graphics-15-win.jar";"%JFX_RT%\org\openjfx\javafx-base\15\javafx-base-15.jar";"%JFX_RT%\org\openjfx\javafx-base\15\javafx-base-15-win.jar"

"%JAVA_HOME%\bin\javaw.exe" --module-path "%JFX_HOME%\lib" --add-modules javafx.controls -classpath "C:\Users\armin\git\pacman-basic\pacman\target\pacman-basic-1.0.jar";"C:\Users\armin\git\pacman-javafx\pacman-ui-fx\target\pacman-ui-fx-1.0.jar";%JFX_LIBS%" de.amr.games.pacman.ui.fx.app.PacManGameAppFX %*