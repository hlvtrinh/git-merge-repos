@echo on

set BASEDIR=%~dp0
mvn --file "%BASEDIR%/pom.xml" --quiet -X clean compile exec:java -Dexec.args="%*"
