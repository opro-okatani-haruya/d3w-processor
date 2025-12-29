@echo off
chcp 65001

set JAVA_OPTS=-Dfile.encoding=UTF-8
set JAR=build\d3w-processor.jar
set D3W="src\test\resources\templates\Box20251229184004.d3w"
set YAML1=src\test\resources\configs\01_TEST_YAML.yaml
set YAML2=src\test\resources\configs\02_TEST_YAML.yaml
set YAML3=src\test\resources\configs\03_TEST_YAML.yaml
set YAML4=src\test\resources\configs\04_TEST_YAML.yaml
set YAML5=src\test\resources\configs\05_TEST_YAML.yaml

java %JAVA_OPTS% -jar "%JAR%" "%D3W%" "%YAML1%" "%YAML2%" "%YAML3%" "%YAML4%" "%YAML5%"

pause
