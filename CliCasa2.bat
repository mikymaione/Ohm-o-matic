@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-11.0.1
set PATH=C:\Program Files\Java\jdk-11.0.1\bin\;%PATH%
java -version
java -cp target\Ohm-o-matic-jar-with-dependencies.jar OhmOMatic.Cli.CliCasa -r http://localhost:8080/OOM/OOM -i Sasuke -k 127.0.0.1 -q 9002 -j 127.0.0.1 -p 9001