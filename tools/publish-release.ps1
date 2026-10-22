param (
    [string]$Tag = "v1.0.0",
    [string]$Repo = "Atom0131/Bori-dex",
    [string]$ApkPath = ""
)

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Bori-dex Direct Release Uploader" -ForegroundColor Cyan
Write-Host " Target Tag : $Tag" -ForegroundColor Cyan
Write-Host " Target Repo: $Repo" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 1. Determine APK path
if (-not $ApkPath) {
    $candidates = @(
        "$PSScriptRoot\..\app\build\outputs\apk\release\app-release.apk",
        "$PSScriptRoot\..\app\build\outputs\apk\debug\app-debug.apk",
        "$PSScriptRoot\..\..\Bori-dex-$Tag.apk",
        "$PSScriptRoot\..\Bori-dex-$Tag.apk"
    )
    foreach ($cand in $candidates) {
        if (Test-Path $cand) {
            $ApkPath = (Resolve-Path $cand).Path
            break
        }
    }
}

if (-not $ApkPath -or -not (Test-Path $ApkPath)) {
    Write-Error "Could not find APK file to upload. Checked release and debug output paths."
    exit 1
}

$apkItem = Get-Item $ApkPath
$assetName = "Bori-dex-$Tag.apk"
$apkSizeMb = [math]::Round($apkItem.Length / 1MB, 2)
Write-Host ("[+] Selected APK: " + $apkItem.FullName + " (" + $apkSizeMb + " MB)") -ForegroundColor Green

# 2. Retrieve token from Git Credential Manager
Write-Host "[*] Fetching GitHub credentials via Git Credential Manager..." -ForegroundColor Yellow
$credOutput = "protocol=https`nhost=github.com`n`n" | git credential fill | Out-String
$tokenLine = ($credOutput -split "`r?`n") | Where-Object { $_ -like "password=*" }
if (-not $tokenLine) {
    Write-Error "Failed to retrieve GitHub token from git credential manager. Please ensure git is logged into GitHub."
    exit 1
}
$token = $tokenLine.TrimStart("password=").Trim()

$headers = @{
    "Authorization" = "Bearer $token"
    "User-Agent"    = "BoriDex-Uploader"
    "Accept"        = "application/vnd.github+json"
}

# 3. Fetch or Create Release
Write-Host "[*] Querying GitHub release for tag '$Tag'..." -ForegroundColor Yellow
$releaseUrl = "https://api.github.com/repos/$Repo/releases/tags/$Tag"
$release = $null
try {
    $release = Invoke-RestMethod -Uri $releaseUrl -Headers $headers -Method Get
} catch {
    Write-Host "[*] Release '$Tag' not found. Creating release '$Tag' on GitHub..." -ForegroundColor Yellow
    
    $releaseNotesPath = "$PSScriptRoot\..\RELEASE_NOTES.md"
    $bodyText = "Bori-dex $Tag release."
    if (Test-Path $releaseNotesPath) {
        $content = Get-Content $releaseNotesPath -Raw
        $bodyText = $content.Substring(0, [math]::Min(3500, $content.Length))
    }
    
    $createBody = @{
        tag_name = $Tag
        name = "Bori-dex $Tag"
        body = $bodyText
        draft = $false
        prerelease = $false
    } | ConvertTo-Json -Depth 5

    $createUrl = "https://api.github.com/repos/$Repo/releases"
    $bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($createBody)
    $release = Invoke-RestMethod -Uri $createUrl -Headers $headers -Method Post -Body $bodyBytes -ContentType "application/json; charset=utf-8"
    Write-Host ("[+] Created new Release: '" + $release.name + "' (ID: " + $release.id + ")") -ForegroundColor Green
}

Write-Host ("[+] Found Release: '" + $release.name + "' (ID: " + $release.id + ")") -ForegroundColor Green

# 4. Check if asset already exists and delete it if so
$existingAsset = $release.assets | Where-Object { $_.name -eq $assetName }
if ($existingAsset) {
    Write-Host ("[*] Existing asset '$assetName' found (Asset ID: " + $existingAsset.id + "). Replacing...") -ForegroundColor Yellow
    $deleteUrl = "https://api.github.com/repos/$Repo/releases/assets/$($existingAsset.id)"
    Invoke-RestMethod -Uri $deleteUrl -Headers $headers -Method Delete | Out-Null
    Write-Host "[+] Old asset removed successfully." -ForegroundColor Green
}

# 5. Upload new APK
$uploadUrl = "https://uploads.github.com/repos/$Repo/releases/$($release.id)/assets?name=$assetName"
Write-Host ("[*] Uploading " + $assetName + " (" + $apkSizeMb + " MB)... Please wait...") -ForegroundColor Yellow

$uploadHeaders = @{
    "Authorization" = "Bearer $token"
    "User-Agent"    = "BoriDex-Uploader"
    "Content-Type"  = "application/vnd.android.package-archive"
}

try {
    $uploadResult = Invoke-RestMethod -Uri $uploadUrl -Headers $uploadHeaders -Method Post -InFile $apkItem.FullName
    Write-Host "========================================" -ForegroundColor Green
    Write-Host " [+] SUCCESS! Release APK Updated!" -ForegroundColor Green
    Write-Host " Asset Name : $($uploadResult.name)" -ForegroundColor Green
    Write-Host " Size       : $([math]::Round($uploadResult.size / 1MB, 2)) MB" -ForegroundColor Green
    Write-Host " State      : $($uploadResult.state)" -ForegroundColor Green
    Write-Host " Download   : $($uploadResult.browser_download_url)" -ForegroundColor Cyan
    Write-Host " Release    : $($release.html_url)" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Green
} catch {
    Write-Error "Failed to upload APK asset: $_"
    exit 1
}
