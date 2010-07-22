set JPE_HOME=C:\home\rssh\work\TermWareEx\JPE
set TERMWARE_HOME=C:\home\rssh\work\TermWare.2
set JAVACHECKER2_HOME=C:\home\rssh\work\TermWareEx\JavaChecker2


rem set JAVA_OPTS=-Xms128m -Xmx512m

rem java -cp "%JPE_HOME%\lib\TermWare2.jar";"%JPE_HOME%\lib\TermWareJPP.jar";"%JPE_HOME%\lib\JavaChecker2.jar";"%JPE_HOME%\dist\JPE.jar"  ua.gradsoft.jpe.Main --input-dir "%JPE_HOME%\src" --output-dir "%JPE_HOME%\output\jpe-out-1" --jpehome "%JPE_HOME%" --transformation ID


java %JAVA_OPTS% -cp "%JPE_HOME%\lib\TermWare2.jar";"%JPE_HOME%\lib\TermWareJPP.jar";"%JPE_HOME%\lib\JavaChecker2.jar";"%JPE_HOME%\dist\JPE.jar"  ua.gradsoft.jpe.Main --input-dir "%TERMWARE_HOME%\jsrc-core" --output-dir "%JPE_HOME%\output\jpe-out-2" --jpehome "%JPE_HOME%" --transformation ID


set OUTPUT_DIR="%JPE_HOME%\output\jpe-out-e1"
set INPUT_DIR="%JPE_HOME%\testdata\example1"

rem java %JAVA_OPTS% -cp "%JPE_HOME%\lib\TermWare2.jar";"%JPE_HOME%\lib\TermWareJPP.jar";"%JPE_HOME%\lib\JavaChecker2.jar";"%JPE_HOME%\dist\JPE.jar"  ua.gradsoft.jpe.Main --input-dir "%INPUT_DIR%" --output-dir "%OUTPUT_DIR%" --jpehome "%JPE_HOME%" --transformation JPE

