@ECHO OFF
ECHO "Please use Java 11 or newer, this BAT doesn't check the Java version"

call "./test-setenv.bat"

if not "%POSTGRES_JDBC_URL%" == "" goto jdbcUrlOk
echo "Please set POSTGRES_JDBC_URL environment variable in test-setenv.bat"
goto end

:jdbcUrlOk

if not "%POSTGRES_JDBC_USERNAME%" == "" goto jdbcUsernameOk
echo "Please set POSTGRES_JDBC_USERNAME environment variable in test-setenv.bat"
goto end

:jdbcUsernameOk

if not "%POSTGRES_JDBC_USERNAME%" == "" goto jdbcPasswordOk
echo "Please set POSTGRES_JDBC_USERNAME environment variable in test-setenv.bat"
goto end

:jdbcPasswordOk

mvnw.cmd -f samples-test clean package ^
    -Duser.language=en ^
    -Dmidpoint.repository.jdbcUrl=%POSTGRES_JDBC_URL% ^
    -Dmidpoint.repository.jdbcUsername=%POSTGRES_JDBC_USERNAME% ^
    -Dmidpoint.repository.jdbcPassword=%POSTGRES_JDBC_PASSWORD% ^
    -Dtest.config.file=test-config-new-repo.xml

:end