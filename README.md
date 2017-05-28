# TP-Link-Smarthings-with-Server
Version 2.3 with some user status functions.

This version will be replaced on June 1 or 2 to support HS110 energy monitor functions and other improvements.

This version transfers all bulb functionality to the SmartThings groovy files and then uses the node server (T_-LinkSever.js) as a HTTP to UDP bridge.  The objective is to have a UDP ready device handler for when and if SmartThings starts to support the TP-Link IOT UDP sockets.

Version 2.0.  Added functionality to detect a TP-Link device off-line plus in interface to the TP-LinkBridge device handler.

Version 2.1.  Added bridge device interface, interfacing to the separate bridge.  Allows restart of the PC bridge from ST.

Version 2.2.  Integrate command parameter "ignore_default" into command stream.

Version 2.3.  Fixed problem with Amazon Echo - ST - TP-Link Bulbs integration (due to updates in Echo-ST stream).

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
   
  c.  Visual indication of a TP-Link device off-line.
  
  d.  Interfaces to a separate TP-LinkBridge device handler.  This DH allows checking the operational status of the TP-LinkServer.js applet and also allows a reboot of the PC from SmartThings.  The TP-LinkBridge is NOT required.
     
These instructions are a modification of those posted by destructure00 on the SmartThings Community forum.  His application was for the TP-Link HS-100 outlets.  These instructions are based my installation and experience.
Pre-requisites: 

- 	Windows machine that stays turned on, logged in, and connected to your local network all the time. This will be your gateway PC.

- 	Static IP address for your gateway PC. Assign through your router.

- 	Static IP address for each of your TP Link smart outlets (you can find the MAC addresses for your outlets in the Device Settings page in the Kasa app if needed).

- 	The common recommendation is to disable automatic updates on the gateway PC so it does not auto-restart and kill your node.js session. Do this at your own risk.

There are notes at the end of this document that discuss options for keeping the software running after a restart of the server.
Instructions:

# Installation

See the file TP-Link Server Install.txt
