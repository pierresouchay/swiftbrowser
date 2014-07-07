#!/bin/bash
echo 'tell application "System Preferences"
	activate
	set the current pane to pane id "com.apple.preference.security"
	
end tell' | osascript
