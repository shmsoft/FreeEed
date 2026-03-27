; FreeEed Windows Installer Script (NSIS)
; Requires NSIS (makensis) to compile

!define APPNAME "FreeEed"
!define COMPANYNAME "SHMSoft"
!define DESCRIPTION "FreeEed E-Discovery Platform"
; VERSION will be passed in from the command line (/DVERSION=...)

Name "${APPNAME} ${VERSION}"
OutFile "FreeEed-${VERSION}-Windows.exe"
InstallDir "$PROFILE\FreeEed" ; Install to user's home directory so it doesn't need admin rights

; Request non-admin privileges
RequestExecutionLevel user

Page directory
Page instfiles

Section "FreeEed Application" SecCore
  SetOutPath "$INSTDIR"
  
  ; Copy all files recursively from the current directory (where makensis runs)
  File /r "*.*"

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
