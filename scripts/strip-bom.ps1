# Strip UTF-8 BOM from text files in the repository
# Usage: .\scripts\strip-bom.ps1 -Path '.' -Extensions '*.java','*.xml','*.properties'
param(
    [string]$Path = '.',
    [string[]]$Extensions = @('*.java','*.xml','*.properties','*.yml','*.yaml','*.md')
)

Write-Host "Stripping BOM from files under $Path"
$files = Get-ChildItem -Path $Path -Recurse -Include $Extensions -File -ErrorAction SilentlyContinue
foreach ($f in $files) {
    $bytes = [System.IO.File]::ReadAllBytes($f.FullName)
    if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
        Write-Host "Removing BOM from $($f.FullName)"
        $newBytes = $bytes[3..($bytes.Length - 1)]
        [System.IO.File]::WriteAllBytes($f.FullName, $newBytes)
    }
}
Write-Host "Done."