/*
TP-Link_LB-100&110 Version 2.0

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
3.	This handler is for the TP-Link LB-100/110 bulbs.  This handler support LB-100 and
    LB-110 power and bulb state functions.
Update History
	03/12/2017	- Created initial rendition.  Version 1.0
	03/30/2017	- Version 1.2.  Rearranged some functions.  Added color coding to	
			      indicate bulb is turning on or off.  Added some notes.
	04/02/2017	- Version 2.0. Added messaging for bridge to device TCP timeout.  Added new 
			      power state indicating these two problems. Created dedicated
			      call-back for commands versus status refresh.
*/
metadata {
	definition (name: "TP-Link LB-100&110", namespace: "V2.0", author: "Dave Gutheinz") {
		capability "Switch"
		capability "Switch Level"
		capability "refresh"
	}
	tiles {
//-- Switch (on/off) Tile - off: white, on: blue, turning on/off: yellow, offine: red -----
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00a0dc",
				nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff",
				nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#e86d13",
				nextState:"on"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#e86d13",
				nextState:"off"
                attributeState "offline", label:'Bulb Offline', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#e86d13",
                nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", label: "Brightness: ${currentValue}", action:"switch level.setLevel"
			}
		}
		standardTile("refresh", "capability.refresh", width: 2, height: 2,  decoration: "flat") {
			state ("default", label:"Refresh", action:"refresh.refresh", icon:"st.secondary.refresh", backgroundColor:"#ffffff")
		}         
		main("switch")
		details(["switch", "refresh"])
    }
}
preferences {
	input("deviceIP", "text", title: "Device IP", required: true, displayDuringSetup: true)
	input("gatewayIP", "text", title: "Gateway IP", required: true, displayDuringSetup: true)
}
//-- ON -----------------------------------------------------------------------------------
def on() {
	log.info "${device.name} ${device.label}: Turning ON"
	sendCmdtoServer('{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"on_off":1}}}', "commandResponse")
}
//-- OFF -----------------------------------------------------------------------------------
def off() {
	log.info "${device.name} ${device.label}: Turning OFF"
	sendCmdtoServer('{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"on_off":0}}}', "commandResponse")
}
//-- Set Level (brightness) - If bulb is off, turn on first. -------------------------------
def setLevel(percentage) {
	log.info "${device.name} ${device.label}: Setting Brightness to ${percentage}%"
 	if(device.latestValue("switch") == "off") {
		sendCmdtoServer('{"smartlife.iot.smartbulb.lightingservice": {"transition_light_state": {"on_off": 1}}}', "onAction")
		sendEvent(name: "switch", value: "turningOn", isStateChange: true)
		sendCmdtoServer("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"brightness":${percentage}}}}""", "commandResponse")
    } else {
		sendCmdtoServer("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"brightness":${percentage}}}}""", "commandResponse")
	}
}
//-- Refresh ------------------------------------------------------------------------------
def refresh(){
	log.info "Polling ${device.name} ${device.label}"
	sendCmdtoServer('{"system":{"get_sysinfo":{}}}', "refreshResponse")
}
//-- Send the command to the Bridge.  Callback defined in the sendCmdtoServer command. ----
private sendCmdtoServer(command, action){
	def headers = [:] 
	headers.put("HOST", "$gatewayIP:8082")   // port 8082 must be same as value in TP-LInkServerLite.js
	headers.put("tplink-iot-ip", deviceIP)
	headers.put("command", command)
	sendHubCommand(new physicalgraph.device.HubAction([
		headers: headers],
		device.deviceNetworkId,
		[callback: action]
	))
}
//-- Callback onAction - use when turning on bulb while setting other params.  ------------
def onAction(response){
	log.info "On command response returned from bulb."
}
//-- Callback commandResponse - the command response is different than the refresh. -------
//-- Also, check for a bulb TCP time-out and set switch state to alert user. --------------
def commandResponse(response){
	def cmdResponse = parseJson(response.headers["cmd-response"])
	if (cmdResponse.error == "TCP Timeout") {
		log.error "$device.name $device.label: $cmdResponse.error"
 		sendEvent(name: "switch", value: "offline", isStateChange: true)
	} else {
		state =  cmdResponse["smartlife.iot.smartbulb.lightingservice"]["transition_light_state"]
		parseStatus(state)
	}
}
//-- Callback refreshResponse -------------------------------------------------------------
def refreshResponse(response){
	def cmdResponse = parseJson(response.headers["cmd-response"])
	if (cmdResponse.error == "TCP Timeout") {
		log.error "$device.name $device.label: $cmdResponse.error"
		sendEvent(name: "switch", value: "offline", isStateChange: true)
	} else {
		state = cmdResponse.system.get_sysinfo.light_state
		parseStatus(state)
	}
}
//-- Parse Status - Status return format is different if the bulb s on vs off. ----------
def parseStatus(state){
	def status = state.on_off
	if (status == 1) {
		status = "on"
	} else {
		status = "off"
		state = state.dft_on_state
	}
	def level = state.brightness
	log.info "$device.name $device.label: Power: ${status} / Mode: ${mode} / Brightness: ${level}% / Color Temp: ${color_temp}K / Hue: ${hue} / Saturation: ${saturation}"
//-- Update the color bulb parameters. --------------------------------------------------
	sendEvent(name: "switch", value: status, isStateChange: true)
	sendEvent(name: "level", value: level, isStateChange: true)
}