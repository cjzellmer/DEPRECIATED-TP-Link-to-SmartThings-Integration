/**
Node.js Bridge / Server Device Handler

Copyright 2017 Dave Gutheinz

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this 
file except in compliance with the License. You may obtain a copy of the License at:
		http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software distributed under 
the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF 
ANY KIND, either express or implied. See the License for the specific language governing 
permissions and limitations under the License.

Notes: 
1.	This Device Handler requires an operating Windows 10 PC (server) running the Node.js
	server designed to control TP-Link devices.
2.	This DH will work only with the TP-LinkServerLite.js server node, version 1.3 and later.
3.	This handler provides a refresh status and the ability to restart the server.

Update History
	04-07-2017	- Initial release.
    04-08-2017	- Changed to V2.0 for commonality across line.  Standardized colors to
    			  SmartThing standards.
*/
metadata {
	definition (name: "NodeJSBridge", namespace: "V2.0", author: "David Gutheinz") {
		capability "Bridge"
		capability "Refresh"
		capability "Switch"
        attribute "Reset", "string"
		command "resetBridge"
	}
	tiles {
//-- Switch is on or offline. Pressing this button does nothing.  -----
		multiAttributeTile(name:"switch", type: "lighting", width: 2, height: 2, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', icon:"st.switches.switch.on", backgroundColor:"#00a0dc"
                attributeState "polling", label:'Polling Bridge', icon:"st.switches.switch.off", backgroundColor:"#e86d13"
                attributeState "offline", label:'Bridge Offline', icon:"st.switches.switch.off", backgroundColor:"#e86d13"
			}
		}
		standardTile("refresh", "capability.refresh", width: 2, height: 2,  decoration: "flat") {
			state ("default", label:"Refresh", action:"refresh.refresh", icon:"st.secondary.refresh")
		}
		standardTile("Reset", "Reset", width: 2, height: 2, decoration: "flat") {
			state ("Reset", label:'RESET', action:"resetBridge", backgroundColor:"#ffffff")
		}

		main("switch")
		details(["switch", "refresh", "Reset"])
    }
}
preferences {
	input("bridgeIP", "text", title: "Device IP", required: true, displayDuringSetup: true)
}

def refresh() {
	log.info "Polling ${device.name} ${device.label}"
 	sendEvent(name: "switch", value: "polling", isStateChange: true)
	sendCmdtoServer('pollBridge')
}
def resetBridge() {
	log.info "Resetting ${device.name} ${device.label}"
	sendCmdtoServer('resetBridge')
}
private sendCmdtoServer(command){
	def headers = [:]
	headers.put("HOST", "$bridgeIP:8082")   // port 8082 must be same as value in TP-LInkServerLite.js
	headers.put("command", command)
	sendHubCommand(new physicalgraph.device.HubAction(
		[headers: headers],
 		device.deviceNetworkId,
 		[callback: bridgeResponse]
	))
}
def bridgeResponse(response){
	def cmdResponse = response.headers["cmd-response"]
	if (cmdResponse == 'on') {
		log.info "NodeJS Bridge is operational"
		sendEvent(name: "switch", value: "on", isStateChange: true)
	}else if (cmdResponse == 'restart') {
		log.info "NodeJS Bridge is restarting"
		sendEvent(name: "switch", value: "offline", isStateChange: true)
	} else {
		refresh()
	}
}