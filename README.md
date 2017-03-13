# TP-Link-Smarthings-with-Server

This version transfers all bulb functionality to the SmartThings groovy files and then uses the node server (T_-LinkSeverLite.js) as a HTTP to UDP bridge.

These instructions are designed for an initial installation of the TP-Link Internet of Things (IOT) devices into the SmartThings environment.  

Features:

  a.  Single server for both bulbs and plugs.
  
  b.  No separate API files/structure.
  
  c.  Controls following TP-Link bulbs/functions:
  
      1)  HS-100 Plug and HS-200 switch, ON/OFF (tested)
      
      2)  HS-105 and HS-110 Plugs, ON/OFF (NOT tested)
      
      3)  LB-100 and LB-110 bulbs, ON/OFF and Brightness (tested on LB-120)
      
      4.  LB-120 bulb, ON/OFF, Brightness, Color Temperature, and Circadian mode (tested)
      
      5.  LB-130 bulb, LB-120 functions plus Color using color wheel (tested)
      
The Installation files have been greatly reduced from the previous versions and include:

•	“cmdPrompt.bat” – a MS Windows batch file that will start a window with a command prompt.

•	“Readme.md” – these installation instructions.

•	 “TP-LinkServerLite.js” – the single node.js server script.

•	“TP-LinkServerLite.bat” – a MS Windows server start file.

•	“TP-LInkPlug_SwitchLIte_v1.0.groovy” – SmartThings Device Handler for all TP-Link Plugs and Switches.

•	“TP-Link_LB-100_110_Lite_v1.0.grooy” – SmartThings Device Handler for the TP-Link LB-100 and LB-110 bulbs.
