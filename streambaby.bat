@echo off
set LAUNCHDIR="%CD%"
pushd "%~dp0\native"
java -Djava.net.preferIPv4Stack=true -Xmx256m -Xmx256m -jar "%~dp0/jbin/streambaby.jar" %1 %2 %3 %4 %5 %6 %7 %8 
echo Exited.
pause
popd
