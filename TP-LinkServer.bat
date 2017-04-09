REM  Place this file in the TP-Link Bulb top level directory.
REM  UnREM the below an change the direcory to the TP-Link directory if auto-starting.
REM  cd c:\1-TP Link\
color 3f
title TP-Link_Server
prompt $_
Echo off
CLS
:startNode
date /t
time /t
node TP-LinkServer.js
goto startNode