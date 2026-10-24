param([ValidateSet('run', 'test')][string]$Task = 'run')
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
$previousMavenOpts = $env:MAVEN_OPTS
try {
    # Use the existing Windows trust store; TLS certificate validation stays enabled.
    $env:MAVEN_OPTS = "$previousMavenOpts -Djavax.net.ssl.trustStoreType=Windows-ROOT -Djavax.net.ssl.trustStore=NUL"
    # Windows domain sockets cannot use the long temporary path of some host environments.
    $socketOption = "-Djdk.net.unixdomain.tmpdir=$PSScriptRoot"
    if ($Task -eq 'test') {
        & .\mvnw.cmd "-DargLine=$socketOption" test
    } else {
        & .\mvnw.cmd "-Dspring-boot.run.jvmArguments=$socketOption" spring-boot:run
    }
    $result = $LASTEXITCODE
} finally {
    $env:MAVEN_OPTS = $previousMavenOpts
    Pop-Location
}
exit $result
