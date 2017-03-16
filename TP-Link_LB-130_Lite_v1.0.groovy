/*
TP-Link_LB-130 Lite Version 1.0

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
	LB-130 functions:
	a.	On/Off
	b.	Brightness,
	c.	Color Temperature (range 2500 - 9000)
	d.	Circadian Mode
	e.	Color (using color wheel).

Update History
	03/12/2017 - Created initial rendition.  Version 1.0
*/
metadata {
	definition (name: "TP-Link_LB-130_Lite", namespace: "V1.0", author: "Dave Gutheinz") {
		capability "Switch"
		capability "Switch Level"
		capability "Color Control"
		capability "Color Temperature"
		capability "refresh"
		attribute "bulbMode", "string"
		command "setModeNormal"
		command "setModeCircadian"
	}
	tiles {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#79b821",
				nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff",
				nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#79b821",
				nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff",
				nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", label: "Brightness: ${currentValue}", action:"switch level.setLevel"
			}
			tileAttribute ("device.color", key: "COLOR_CONTROL") {
        		attributeState "color", action:"setColor"
			}
		}
		controlTile("colorTempSliderControl", "device.colorTemperature", "slider", width: 4, height: 1, inactiveLabel: false,
		range:"(2500..9000)") {
        	state "colorTemperature", action:"color temperature.setColorTemperature"
		}
		valueTile("colorTemp", "device.colorTemperature", inactiveLabel: false, decoration: "flat", height: 1, width: 4) {
			state "colorTemp", label: 'ColorTemp: ${currentValue}K'
		}
		standardTile("bulbMode", "bulbMode", width: 2, height: 2) {
			state "normal", label:'Normal', action:"setModeCircadian", backgroundColor:"#ffffff", nextState: "circadian"
			state "circadian", label:'Circadian', action:"setModeNormal", backgroundColor:"#1814EA", nextState: "normal"
		}
		standardTile("refresh", "capability.refresh", width: 2, height: 2,  decoration: "flat") {
			state ("default", label:"Refresh", action:"refresh.refresh", icon:"st.secondary.refresh")
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
	sendCmdtoServer('{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"on_off": 1}}}', "hubActionResponse")
}
def off() {
	log.info "${device.name} ${device.label}: Turning OFF"
	sendCmdtoServer('{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"on_off": 0}}}', "hubActionResponse")
}
def setLevel(percentage) {
	log.info "${device.name} ${device.label}: Setting Brightness to ${percentage}%"
	complexCmd("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"brightness": ${percentage}}}}""")
}
def setColorTemperature(kelvin) {
	log.info "${device.name} ${device.label}: Setting Color Temperature to ${kelvin}K"
    complexCmd("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"color_temp": ${kelvin}}}}""")
}
def setModeNormal() {
	log.info "${device.name} ${device.label}: Changing Mode to NORMAL"
    sendCmdtoServer("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"mode": "normal"}}}""", "hubActionResponse")
}
def setModeCircadian() {
	log.info "${device.name} ${device.label}: Changing Mode to CIRCADIAN"
    sendCmdtoServer("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"mode": "circadian"}}}""", "hubActionResponse")
}
def setColor(Map color) {
	def hue = color.hue * 3.6 as int
	def saturation = color.saturation as int
	log.info "${device.name} ${device.label}: Setting bulb Hue: ${hue} and Saturation: ${saturation}"
    complexCmd("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"color_temp": 0, "hue": ${hue}, "saturation": ${saturation}}}}""")
}
def refresh(){
	log.info "Polling ${device.name} ${device.label}"
	sendCmdtoServer('{"system":{"get_sysinfo":{}}}', "hubActionResponse")
}
def complexCmd(command) {
	if(device.latestValue("switch") == "off") {
		sendCmdtoServer('{"smartlife.iot.smartbulb.lightingservice": {"transition_light_state": {"on_off": 1}}}', "nullHubAction")
		sendCmdtoServer(command, "hubActionResponse")
    } else {
		sendCmdtoServer(command, "hubActionResponse")}
}
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
def nullHubAction(response){
}
def hubActionResponse(response){
	def cmdResponse = parseJson(response.headers["cmd-response"])
	String cmdResp = cmdResponse.toString()
	if (cmdResp.substring(1,10) == "smartlife") {
	state =  cmdResponse["smartlife.iot.smartbulb.lightingservice"]["transition_light_state"]
	} else {
    	state = cmdResponse.system.get_sysinfo.light_state
	}
	def status = state.on_off
	if (status == 1) {
		status = "on"
	} else {
        status = "off"
		state = state.dft_on_state
	}
	def mode = state.mode
	def level = state.brightness
	def color_temp = state.color_temp
	def hue = state.hue //as int
	def saturation = state.saturation //as int
	log.info "${device.name} ${device.label}: Power: ${status} / Mode: ${mode} / Brightness: ${level}% / Color Temp: ${color_temp}K / Hue: ${hue} / Saturation: ${saturation}"
	sendEvent(name: "bulbMode", value: mode, isStateChange: true)
	sendEvent(name: "switch", value: status, isStateChange: true)
	sendEvent(name: "level", value: level)
	sendEvent(name: "colorTemperature", value: color_temp)
	sendEvent(name: "hue", value: hue)
	sendEvent(name: "saturation", value: saturation)
	sendEvent(name: "color", value: colorUtil.hslToHex(hue/3.6 as int, saturation as int))
}