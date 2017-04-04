/*
TP-Link_LB-130 Version 1.2

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
3.	This handler is for the TP-Link LB-130 bulb.  This handler support LB-130 power
	and bulb state functions.
Update History
	03/12/2017	- Created initial rendition.  Version 1.0
	03/30/2017	- Version 1.12.  Rearranged some functions.  Added color coding to
			  indicate bulb is turning on or off.  Added some notes.
	04/02/2017	- Version 1.2.  Added messaging for bridge to device TCP timeout. 
			  Added new power state indicating these two problems. Created
			  dedicated call-back for commands versus status refresh.
*/
metadata {
	definition (name: "TP-Link LB-130", namespace: "V1.2", author: "Dave Gutheinz") {
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
//-- Switch (on/off) Tile - off: white, on: blue, turning on/off: yellow, offine: red -----
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
                attributeState "offline", label:'Bulb Offline', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#bc2323",
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
//-- Mode Tile - normal: white, circadian: blue ------------------------------------------
		standardTile("bulbMode", "bulbMode", width: 2, height: 2) {
			state "normal", label:'Normal', action:"setModeCircadian", backgroundColor:"#ffffff", nextState: "circadian"
			state "circadian", label:'Circadian', action:"setModeNormal", backgroundColor:"#00a0dc", nextState: "normal"
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
//-- Set Color Temperature - If bulb is off, turn on first. -------------------------------
def setColorTemperature(kelvin) {
	log.info "${device.name} ${device.label}: Setting Color Temperature to ${kelvin}K"
	if(device.latestValue("switch") == "off") {
		sendCmdtoServer('{"smartlife.iot.smartbulb.lightingservice": {"transition_light_state": {"on_off": 1}}}', "onAction")
		sendEvent(name: "switch", value: "turningOn", isStateChange: true)
		sendCmdtoServer("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"color_temp": ${kelvin}}}}""", "commandResponse")
    } else {
		sendCmdtoServer("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"color_temp": ${kelvin},"hue":0,"saturation":0}}}""", "commandResponse")
	}
}
//-- Set Mode Normal ----------------------------------------------------------------------
def setModeNormal() {
	log.info "${device.name} ${device.label}: Changing Mode to NORMAL"
	sendCmdtoServer("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"mode":"normal"}}}""", "commandResponse")
}
//-- Set Mode Circadian -------------------------------------------------------------------
def setModeCircadian() {
	log.info "${device.name} ${device.label}: Changing Mode to CIRCADIAN"
	sendCmdtoServer("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"mode":"circadian"}}}""", "commandResponse")
}
//-- Set Bulb Color - I bulb is off, turn on first. ---------------------------------------
def setColor(Map color) {
	def hue = color.hue * 3.6 as int
	def saturation = color.saturation as int
	log.info "${device.name} ${device.label}: Setting bulb Hue: ${hue} and Saturation: ${saturation}"
	if(device.latestValue("switch") == "off") {
		sendCmdtoServer('{"smartlife.iot.smartbulb.lightingservice": {"transition_light_state": {"on_off": 1}}}', "onAction")
		sendEvent(name: "switch", value: "turningOn", isStateChange: true)
		sendCmdtoServer("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"color_temp":0,"hue":${hue},"saturation":${saturation}}}}""", "commandResponse")
    } else {
		sendCmdtoServer("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"color_temp":0,"hue":${hue},"saturation":${saturation}}}}""", "commandResponse")
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
	def mode = state.mode
	def level = state.brightness
	def color_temp = state.color_temp
	def hue = state.hue
	def saturation = state.saturation
	log.info "$device.name $device.label: Power: ${status} / Mode: ${mode} / Brightness: ${level}% / Color Temp: ${color_temp}K / Hue: ${hue} / Saturation: ${saturation}"
//-- Update the color bulb parameters. --------------------------------------------------
	sendEvent(name: "switch", value: status, isStateChange: true)
	sendEvent(name: "bulbMode", value: mode, isStateChange: true)
	sendEvent(name: "level", value: level, isStateChange: true)
	sendEvent(name: "colorTemperature", value: color_temp, isStateChange: true)
	sendEvent(name: "hue", value: hue, isStateChange: true)
	sendEvent(name: "saturation", value: saturation, isStateChange: true)
	sendEvent(name: "color", value: colorUtil.hslToHex(hue/3.6 as int, saturation as int))
}