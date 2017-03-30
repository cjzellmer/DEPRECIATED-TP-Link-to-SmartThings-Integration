/*
TP-Link Plugs and Switches Lite Version 1.0

Copyright 2017 Dave Gutheinz

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this 
file except in compliance with the License. You may obtain a copy of the License at:

		http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under 
the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF 
ANY KIND, either express or implied. See the License for the specific language governing 
permissions and limitations under the License.

Notes: 
1.	This Device Handler requires an operating Windows 10 PC (server) that interfaces 
	to the TP-Link Bulbs.
2.	This DH will work only with the TP-LinkServerLite.js server node.
3.	This handler is for the TP-Link Plugs and Switches bulbs.  This handler support the 
	following Plug and Switch functions:
	a.	On/Off

Update History
	03/12/2017 - Created initial rendition.  Version 1.0
	03/30/2017 - Version 1.1.  Rearranged some functions.  Added color coding to
			     indicate switch/plug is turning on or off.  Added some notes.	

*/
metadata {
	definition (name: "TP-Link_Plug_Switch_Lite", namespace: "V1.1", author: "Dave Gutheinz") {
		capability "Switch"
		capability "refresh"
	}
	tiles {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00a0dc",
				nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff",
				nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#efd90f",
				nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#efd90f",
				nextState:"turningOn"
			}
		}
		standardTile("refresh", "capability.refresh", width: 2, height: 2,  decoration: "flat") {
			state ("default", label:"Refresh", action:"refresh.refresh", icon:"st.secondary.refresh")
		}         
		main("switch")
		details(["switch", "refresh"])
    }
}
preferences {
	input("deviceIP", "text", title: "Device IP", required: true, displayDuringSetup: true)
	input("gatewayIP", "text", title: "Gateway IP", required: true, displayDuringSetup: true)
}
def on() {
	log.info "${device.name} ${device.label}: Turning ON"
	sendCmdtoServer('{"system":{"set_relay_state":{"state": 1}}}', "nullHubAction")
}
def off() {
	log.info "${device.name} ${device.label}: Turning OFF"
	sendCmdtoServer('{"system":{"set_relay_state":{"state": 0}}}', "nullHubAction")
}
def refresh(){
	log.info "Polling ${device.name} ${device.label}"
	sendCmdtoServer('{"system":{"get_sysinfo":{}}}', "hubActionResponse")
}
//	Send the command and switch/plug IP to the server.
private sendCmdtoServer(command, action){
	def headers = [:]
	headers.put("HOST", "$gatewayIP:8082")   // port 8082 must be same as value in TP-LInkServerLite.js
	headers.put("tplink-iot-ip", deviceIP)
	headers.put("command", command)
	sendHubCommand(new physicalgraph.device.HubAction(
		[headers: headers],
 		device.deviceNetworkId,
 		[callback: action]
	))
}
//	Once command is received, request status via refresh.
def nullHubAction(response){
	log.info "On/Off command response received from server!"
	refresh()
}
def hubActionResponse(response){
	def cmdResponse = parseJson(response.headers["cmd-response"])
	def status = cmdResponse.system.get_sysinfo.relay_state
	if (status == 1) {
		status = "on"
	} else {
        status = "off"
	}
	log.info "${device.name} ${device.label}: Power: ${status}"
	sendEvent(name: "switch", value: status, isStateChange: true)
}