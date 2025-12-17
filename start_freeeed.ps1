$ErrorActionPreference = "Stop"

Write-Host "******************** Starting FreeEed services"

# Always run from this script's directory
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ScriptDir

# Ensure logs directory exists
$logDir = Join-Path $ScriptDir "logs"
if (-not (Test-Path $logDir)) {
    New-Item -ItemType Directory -Path $logDir | Out-Null
}

# ---------------- Tomcat ----------------
Write-Host "Starting Tomcat..."
$tomcatStartup = Join-Path $ScriptDir "freeeed-tomcat\bin\startup.bat"
if (Test-Path $tomcatStartup) {
    Start-Process -FilePath $tomcatStartup -WorkingDirectory (Split-Path $tomcatStartup -Parent)
} else {
    Write-Warning "Tomcat startup script not found at $tomcatStartup"
}

# ---------------- Solr ----------------
Write-Host "Starting Solr..."
$solr = Start-Process `
    -FilePath "java" `
    -ArgumentList "-Xmx1024M -jar start.jar" `
    -WorkingDirectory (Join-Path $ScriptDir "freeeed-solr\example") `
    -RedirectStandardOutput (Join-Path $logDir "solr.log") `
    -RedirectStandardError  (Join-Path $logDir "solr.err") `
    -PassThru
$solr.Id | Out-File (Join-Path $logDir "solr.pid") -Encoding ascii

# ---------------- Tika ----------------
Write-Host "Starting Tika..."
$tika = Start-Process `
    -FilePath "java" `
    -ArgumentList "-Xmx1024M -jar tika-server.jar" `
    -WorkingDirectory (Join-Path $ScriptDir "freeeed-tika") `
    -RedirectStandardOutput (Join-Path $logDir "tika.log") `
    -RedirectStandardError  (Join-Path $logDir "tika.err") `
    -PassThru
$tika.Id | Out-File (Join-Path $logDir "tika.pid") -Encoding ascii

# ---------------- Python (Uvicorn) via how_to_run.ps1 ----------------
# Prefer using how_to_run.ps1 with ExecutionPolicy Bypass if available
$pythonDir = $null
$pythonDirCandidates = @(
    (Join-Path $ScriptDir "python"),
    (Join-Path $ScriptDir "FreeEed\python")
)

foreach ($cand in $pythonDirCandidates) {
    if (Test-Path $cand) {
        $pythonDir = $cand
        break
    }
}

if ($null -eq $pythonDir) {
    Write-Warning "Could not find Python service directory (tried .\python and .\FreeEed\python). Python services will NOT start."
} else {
    $howToRun = Join-Path $pythonDir "how_to_run.ps1"
    if (Test-Path $howToRun) {
        Write-Host "Starting Python (Uvicorn) via how_to_run.ps1 in $pythonDir..."
        $python = Start-Process `
            -FilePath "powershell.exe" `
            -ArgumentList "-ExecutionPolicy Bypass -File `"$howToRun`"" `
            -WorkingDirectory $pythonDir `
            -RedirectStandardOutput (Join-Path $logDir "python.log") `
            -RedirectStandardError  (Join-Path $logDir "python.err") `
            -PassThru
        $python.Id | Out-File (Join-Path $logDir "python.pid") -Encoding ascii
    } else {
        # Fallback to direct venv + uvicorn if how_to_run.ps1 is missing
        $pythonExe = Join-Path $pythonDir "myenv\Scripts\python.exe"
        if (-not (Test-Path $pythonExe)) {
            Write-Warning "Python venv not found at $pythonExe; Python services will NOT start."
        } else {
            Write-Host "Starting Python (Uvicorn) directly from $pythonExe..."
            $python = Start-Process `
                -FilePath $pythonExe `
                -ArgumentList "-m uvicorn main:app --host 0.0.0.0 --port 8000 --reload" `
                -WorkingDirectory $pythonDir `
                -RedirectStandardOutput (Join-Path $logDir "python.log") `
                -RedirectStandardError  (Join-Path $logDir "python.err") `
                -PassThru
            $python.Id | Out-File (Join-Path $logDir "python.pid") -Encoding ascii
        }
    }
}

# ---------------- FreeEed Player (Swing) ----------------
Write-Host "Starting FreeEed Player (Swing)..."
$playerBat = Join-Path $ScriptDir "FreeEed\freeeed_player.bat"   # adjust if different

if (-not (Test-Path $playerBat)) {
    Write-Warning "FreeEed player launcher not found at $playerBat"
} else {
    $player = Start-Process `
        -FilePath $playerBat `
        -WorkingDirectory (Split-Path $playerBat -Parent) `
        -PassThru
    $player.Id | Out-File (Join-Path $logDir "player.pid") -Encoding ascii
}

Write-Host "✅ All FreeEed services started"

