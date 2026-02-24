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
SectionEnd
