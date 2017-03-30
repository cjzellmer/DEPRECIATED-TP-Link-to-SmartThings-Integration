/*
TP-Link_LB-100_110 Lite Version 1.0

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
3.	This handler is for the TP-Link LB-130 bulb.  This handler support the following 
	LB-100 and LB-110 functions:
	a.	On/Off
	b.	Brightness
4.	Known issues:
	a.	Occasionally, the brightness command gets out-of-sync
    	when being sent with the on command.
Update History
	03/12/2017 - Created initial rendition.  Version 1.0
    03/30/2017 - Version 1.1.  Rearranged some functions.  Added color coding to
                 indicate bulb is turning on or off.  Added some notes.
*/
metadata {
	definition (name: "TP-Link_LB-100_110_Lite", namespace: "V1.1", author: "Dave Gutheinz") {
		capability "Switch"
		capability "Switch Level"
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
				nextState:"on"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#efd90f",
				nextState:"off"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", label: "Brightness: ${currentValue}", action:"switch level.setLevel"
			}
		}
		standardTile("refresh", "capability.refresh", width: 2, height: 2,  decoration: "flat") {
			state ("default", label:"Refresh", action:"refresh.refresh", icon:"st.secondary.refresh", backgroundColor:"#ffffff")
		}         
		main("switch")
		details(["switch", "colorTempSliderControl", "bulbMode", "colorTemp", "refresh"])
    }
}
preferences {
	input("deviceIP", "text", title: "Device IP", required: true, displayDuringSetup: true)
	input("gatewayIP", "text", title: "Gateway IP", required: true, displayDuringSetup: true)
}
def on() {
	log.info "${device.name} ${device.label}: Turning ON"
	sendCmdtoServer('{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"on_off":1}}}', "hubActionResponse")
}
def off() {
	log.info "${device.name} ${device.label}: Turning OFF"
	sendCmdtoServer('{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"on_off":0}}}', "hubActionResponse")
}
//	If bulb is off, turn on prior to setting brightness (level) or color_temp.
//  When originating from here, the ON command will not be parsed by the handler.
def setLevel(percentage) {
	log.info "${device.name} ${device.label}: Setting Brightness to ${percentage}%"
 	if(device.latestValue("switch") == "off") {
		sendCmdtoServer('{"smartlife.iot.smartbulb.lightingservice": {"transition_light_state": {"on_off": 1}}}', "onAction")
		sendEvent(name: "switch", value: "turningOn", isStateChange: true)
		sendCmdtoServer("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"brightness":${percentage}}}}""", "hubActionResponse")
    } else {
		sendCmdtoServer("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"brightness":${percentage}}}}""", "hubActionResponse")}
}
def refresh(){
	log.info "Polling ${device.name} ${device.label}"
	sendCmdtoServer('{"system":{"get_sysinfo":{}}}', "hubActionResponse")
}
//	Send the command and bulb IP to the server.  Callback is in hubActionResponse.
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
def onAction(response){
	log.info "On command response returned from bulb."
}
def hubActionResponse(response){
//	hubActionResponse must handle two different return message, each
//	message in two different formats (based on power state).
	def cmdResponse = parseJson(response.headers["cmd-response"])
	String cmdResp = cmdResponse.toString()
	if (cmdResp.substring(1,10) == "smartlife") {	//	Automatic message from light state commands.
	state =  cmdResponse["smartlife.iot.smartbulb.lightingservice"]["transition_light_state"]
	} else {		// Automatic message from refresh commands.
    	state = cmdResponse.system.get_sysinfo.light_state
	}
	def status = state.on_off
	if (status == 1) {		// Addresses format when bulb is ON.
		status = "on"
	} else {		// Addresses format when bulb is OFF.
        status = "off"
		state = state.dft_on_state
	}
	def mode = state.mode
	def level = state.brightness
	def color_temp = state.color_temp
	def hue = state.hue //as int
	def saturation = state.saturation //as int
	log.info "${device.name} ${device.label}: Power: ${status} / Brightness: ${level}%"
	sendEvent(name: "switch", value: status, isStateChange: true)
	sendEvent(name: "level", value: level, isStateChange: true)
}
