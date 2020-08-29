@echo off
set LOG_OPTS=-Dorg.slf4j.simpleLogger.showDateTime=true
set LOG_OPTS=%LOG_OPTS% -Dorg.slf4j.simpleLogger.dateTimeFormat="yyyy-MM-dd HH:mm:ss"
set LOG_OPTS=%LOG_OPTS% -Dorg.slf4j.simpleLogger.showThreadName=false
java %LOG_OPTS% -jar target\SE4AdminTool.jar %1
