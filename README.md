# TP-Link-Smarthings-with-Server
(new version with functions in groovy file and server used as HTTP-UDP bridge)

This version transfers all bulb functionality to the SmartThings groovy files and then uses the node server (T_-LinkSeverLite.js) as a HTTP to UDP bridge.  The objective is to have a UDP ready device handler for when and if SmartThings starts to support the TP-Link IOT UDP sockets.

Version 2.0.  Added functionality to detect a TP-Link device off-line.  Added a NodeJSBridge Device Handler and code in TP-LinkServerLite.js to allow refreshing status and restarting bridge (these will only work if the bridge and software are already working).  The NodeJSBridge Device Handler is OPTIONAL.

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
  
  d.  A NodeJSBridge device handler to allow checking status of bridge software and restarting bridge, if desired (this will not restart the bridge if the node software is not running or is locked).
     
The Installation files have been greatly reduced from the previous versions and include:

•	“cmdPrompt.bat” – a MS Windows batch file that will start a window with a command prompt.

•	“Readme.md” – these installation instructions.

•	 “TP-LinkServerLite.js” – the single node.js server script.

•	“TP-LinkServerLite.bat” – a MS Windows server start file.

•	“TP-LInk_Plug_Switch_v2.0.groovy” – SmartThings Device Handler for all TP-Link Plugs and Switches.

•	“TP-Link_LB-100_110_v2.0.grooy” – SmartThings Device Handler for the TP-Link LB-100 and LB-110 bulbs.

•	“TP-Link_LB-120_v2.0.grooy” – SmartThings Device Handler for the TP-Link L1-20 bulbs.

•	“TP-Link_LB-130_v12.0.grooy” – SmartThings Device Handler for the TP-Link LB-130 bulbs.

•	“NodeJSBridge_v2.0” – a Bridge device handler to see and refresh status of bridge.

•	“TP-Link Integration Description.pdf” – describes the operation of the installed app on a android smart phone.

These instructions are a modification of those posted by destructure00 on the SmartThings Community forum.  His application was for the TP-Link HS-100 outlets.  These instructions are based my installation and experience.
Pre-requisites: 

- 	Windows machine that stays turned on, logged in, and connected to your local network all the time. This will be your gateway PC.

- 	Static IP address for your gateway PC. Assign through your router.

- 	Static IP address for each of your TP Link smart outlets (you can find the MAC addresses for your outlets in the Device Settings page in the Kasa app if needed).

- 	The common recommendation is to disable automatic updates on the gateway PC so it does not auto-restart and kill your node.js session. Do this at your own risk.

There are notes at the end of this document that discuss options for keeping the software running after a restart of the server.
Instructions:

# Installation

These instructions assume that you have already downloaded the files from the SmartThings Community Forum thread.

1.	Install node.js. Link here: https://nodejs.org/en/download/  (note - make sure you get the msi installer package, not just the exe binary)

2.	Place the above files into a directory of your choice.  Examples in these instructions, will use: “C:\\TPLink”

3.	Open the batch file “TP-LinkServerLite.bat” for editing.  Change the first line to change the directory to the location your TPLinkBulb files were paced (i.e., “CD c:\\TPLink”).

4.	Test the server by starting the batch file “TP-LinkServerLite.bat”.  This should start the server in a separate window on your display.

5.	Log in to SmartThings IDE, then go to "My Device Handlers". Click the green button for "Create New Device Handler".

6.	Open the appropriate groovy and copy the entire.

7.	On the Create New Device Handler page, choose the "From Code" option at the top. Paste in the raw groovy file (from the clipboard).  Go to the bottom and click the “Create” button.

8.	On the next page that opens, click “Publish”, then “For Me” near the top-right of the page. 

9.	Go to My Devices in IDE, click on New Device in the top right corner (you will repeat this step for each of the outlets you have)

  o	Name - enter a name for the product *i.e., “TP-Link HS-100", "TP-Link HS-200", "TP-Link LB-120", TP-Link LB-130").

  o	Label - enter a label, this is what will show in the SmartThings app, (i.e., “Den Lamp”, "Bedroom Fan").

  o	Device Network Id - enter a unique ID (i.e., “LB100-1”, “LB120-1”, and “LB120-2”).

  o	Type - select the appropriate groovy file from the drop down list (should be near the bottom of the list).

  o	Version - Published

  o	Location and Hub - select for your setup

  o	Group - leave blank for now, you can assign to a room later through the app

  o	 Click Create

10.	Now that your device is created, specify the IP addresses for your gateway PC and each device (bulb or plug). To do this, click on "edit" next to Preferences (or find the outlet in your list of Things in the ST app, and go to settings).

11.	Enter the IP addresses for the device (plug or bulb) and gateway PC. Click Save.

12. If you are installing the bridge, enter the Bridge IP (same as gateway IP) and the Bridge Port (8082 for this application).

# Notes on keeping the application running on your PC

Several users have had problems keeping the server application running.  This is typically due to

a.	The PC restarting after updates,

b.	Power failures, and

c.	Resets of the local router.

The following is a method to maximize up-time.  However, remember that for the PC, these are NOT secure settings.  Assure that the server is in a physically secure location.

Prerequisites

a.	Windows 10 PC on WiFi and Windows 10 (either home or professional), and

b.	Router that allows you to reserve IP addresses (mine is a TP-Link Archer C-3150).

Steps

1.	You have already modified the “TPLinkIoTServerStart.bat” for your path.  Use this file below.

2.	Go to the Windows Scheduler (Control Panel -> System and Security -> Administrative Tools -> Task Scheduler.

3.	 Schedule the “TPLinkIoTServerStart.bat” to start every time you log on.

4.	Set Windows to automatically Log you in (see site https://www.cnet.com/how-to/automatically-log-in-to-your-windows-10-pc1).

5.	Go to the BIOS power setting and set PC to NEVER go into standby.

6.	In your router Reserve an IP address for your server PC and your controlled TP-Link devices.

7.	Restart your router. Wait for the reboot to complete.

8.	Go to the smart phone SmartThings App and update your TP-Link device setting to the new IP addresses (for the devices and server).

9.	Reboot your PC. It should automatically run the node app. 

10.	Test your device(s).

11.	(You can now disconnect /power off monitor, mouse, and keyboard.)
