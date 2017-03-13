REM  Place this file in the TP-Link Bulb top level directory.
REM  Add path to the TP-Link Bulb directory if auto-starting.
color 3f
title TP-Link Bulb Family SmartThings Controller
prompt $_
Echo off
CLS
:startNode
date /t
time /t
node TP-LinkServerLite.js
goto startNode