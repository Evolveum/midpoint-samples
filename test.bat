@ECHO OFF
ECHO "Please use Java 11 or newer, this BAT doesn't check the Java version"

mvnw.cmd -f samples-test clean package
