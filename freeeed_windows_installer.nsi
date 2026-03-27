; FreeEed Windows Installer Script (NSIS)
; Requires NSIS (makensis) to compile

!include "MUI2.nsh"
!include "nsDialogs.nsh"

!define APPNAME "FreeEed"
!define COMPANYNAME "SHMSoft"
!define DESCRIPTION "FreeEed E-Discovery Platform"
!define EULA_TRACKING_URL "https://shmsoft.com/eula/accept"
; VERSION will be passed in from the command line (/DVERSION=...)

Name "${APPNAME} ${VERSION}"
OutFile "FreeEed-${VERSION}-Windows.exe"
InstallDir "$PROFILE\FreeEed" ; Install to user's home directory so it doesn't need admin rights

; Request non-admin privileges
RequestExecutionLevel user

; ---- EULA License Page ----
!insertmacro MUI_PAGE_LICENSE "EULA.txt"
!insertmacro MUI_PAGE_DIRECTORY

; ---- Email Input Page ----
Var UserEmail
Page custom EmailPageCreate EmailPageLeave

!insertmacro MUI_PAGE_INSTFILES

!insertmacro MUI_LANGUAGE "English"

; ---- Email Page Functions ----
Function EmailPageCreate
  nsDialogs::Create 1018
  Pop $0

  ${NSD_CreateLabel} 0 0 100% 24u "Please enter your email address to continue:"
  Pop $0

  ${NSD_CreateText} 0 30u 100% 12u ""
  Pop $1
FunctionEnd

Function EmailPageLeave
  ${NSD_GetText} $1 $UserEmail
  StrCmp $UserEmail "" 0 +2
    MessageBox MB_OK "Please enter your email address."
    Abort
FunctionEnd

Section "FreeEed Application" SecCore
  SetOutPath "$INSTDIR"
  
  ; Copy all files recursively from the current directory (where makensis runs)
  File /r "*.*"

  ; ---- Track EULA acceptance (best-effort) ----
  ; Use PowerShell to POST to the licensing server
  nsExec::ExecToLog 'powershell -Command "try { $body = @{machine_id=[System.Environment]::MachineName; email=\"$UserEmail\"; os=\"Windows\"; version=\"${VERSION}\"} | ConvertTo-Json; Invoke-RestMethod -Uri \"${EULA_TRACKING_URL}\" -Method Post -ContentType \"application/json\" -Body $body -TimeoutSec 5 } catch { }"'

  ; ---- Create %USERPROFILE%\.freeeed and default .env ----
  CreateDirectory "$PROFILE\.freeeed"
  IfFileExists "$PROFILE\.freeeed\.env" skip_env_creation
    FileOpen $0 "$PROFILE\.freeeed\.env" w
    FileWrite $0 "# AI Advisor Configuration$\r$\n"
    FileWrite $0 "OPENAI_API_KEY=$\r$\n"
    FileWrite $0 "CHROMA_PERSIST_DIR=chroma_data$\r$\n"
    FileWrite $0 "LLM_MODEL=gpt-4o-mini$\r$\n"
    FileWrite $0 "CHROMA_EMBED_MODEL=text-embedding-3-small$\r$\n"
    FileWrite $0 "TOP_K=10$\r$\n"
    FileWrite $0 "PORT=8000$\r$\n"
    FileClose $0
    MessageBox MB_OK "A default config was created at $PROFILE\.freeeed\.env$\nPlease edit it and add your OPENAI_API_KEY."
  skip_env_creation:

  ; Create shortcuts
  CreateDirectory "$SMPROGRAMS\${APPNAME}"
  CreateShortCut "$SMPROGRAMS\${APPNAME}\FreeEed Control Panel.lnk" "$INSTDIR\ControlPanel.bat" "" "$INSTDIR\freeeed.png"
  CreateShortCut "$DESKTOP\FreeEed Control Panel.lnk" "$INSTDIR\ControlPanel.bat" "" "$INSTDIR\freeeed.png"
  
  ; Uninstaller shortcut
  CreateShortCut "$SMPROGRAMS\${APPNAME}\Uninstall.lnk" "$INSTDIR\uninstall.exe"
  
  ; Create uninstaller
  WriteUninstaller "$INSTDIR\uninstall.exe"
SectionEnd

Section "Uninstall"
  ; Remove shortcuts
  Delete "$SMPROGRAMS\${APPNAME}\FreeEed Control Panel.lnk"
  Delete "$SMPROGRAMS\${APPNAME}\Uninstall.lnk"
  RMDir "$SMPROGRAMS\${APPNAME}"
  Delete "$DESKTOP\FreeEed Control Panel.lnk"

  ; Remove the installation directory
  RMDir /r "$INSTDIR"

  ; Ask user if they want to remove their config/API keys
  MessageBox MB_YESNO "Remove your config ($PROFILE\.freeeed\.env)?$\nThis will delete your API keys." IDNO skip_config_removal
    RMDir /r "$PROFILE\.freeeed"
  skip_config_removal:
SectionEnd
