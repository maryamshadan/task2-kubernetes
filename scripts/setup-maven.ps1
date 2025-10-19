# Download and setup Apache Maven locally for the current PowerShell session
# Usage: .\scripts\setup-maven.ps1

param(
    [string]$MavenVersion = '3.9.6'
)

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$installFull = Join-Path $scriptDir '..' -ChildPath '.maven' | Resolve-Path -LiteralPath | ForEach-Object { $_.ProviderPath }

# Create directory
if (-Not (Test-Path $installFull)) {
    New-Item -ItemType Directory -Path $installFull | Out-Null
}

$zipName = "apache-maven-$MavenVersion-bin.zip"
$downloadUrl = "https://archive.apache.org/dist/maven/maven-3/$MavenVersion/binaries/$zipName"
$zipPath = Join-Path $installFull $zipName

Write-Host "Downloading Maven $MavenVersion..."
Invoke-WebRequest -Uri $downloadUrl -OutFile $zipPath -UseBasicParsing

Write-Host "Extracting to $installFull..."
Add-Type -AssemblyName System.IO.Compression.FileSystem
[System.IO.Compression.ZipFile]::ExtractToDirectory($zipPath, $installFull)

# The extracted folder
$extractedDir = Join-Path $installFull "apache-maven-$MavenVersion"
$mavenBin = Join-Path $extractedDir 'bin'

# Add to PATH for this session
$env:PATH = "$mavenBin;$env:PATH"

Write-Host "Maven installed to $extractedDir"
Write-Host "You can now run: mvn -v"

# Clean up zip
Remove-Item $zipPath -Force
