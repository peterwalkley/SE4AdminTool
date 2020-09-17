@echo off
set PATH=./jre/bin
set JAVA_HOME=./jre
set CP=original-SE4AdminTool.jar;libs/*
set LOG_OPTS=-Dorg.slf4j.simpleLogger.showDateTime=true
set LOG_OPTS=%LOG_OPTS% -Dorg.slf4j.simpleLogger.dateTimeFormat="yyyy-MM-dd HH:mm:ss"
set LOG_OPTS=%LOG_OPTS% -Dorg.slf4j.simpleLogger.showThreadName=false
java -classpath %CP% %LOG_OPTS% tfa.se4.SEAdminServerConnection %1
