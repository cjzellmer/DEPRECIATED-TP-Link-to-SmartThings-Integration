/*
TP-Link_LB-120 Version 2.3

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
3.	This handler is for the TP-Link LB-120 bulb.  This handler support LB-120 power
	and bulb state functions.
Version 2.3 - 05-05-2017
*/
metadata {
	definition (name: "TP-Link LB-120", namespace: "V2.3", author: "Dave Gutheinz") {
		capability "Switch"
		capability "Switch Level"
		capability "Color Temperature"
		capability "refresh"
		attribute "bulbMode", "string"
		command "setModeNormal"
		command "setModeCircadian"
	}
	tiles {
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
		controlTile("colorTempSliderControl", "device.colorTemperature", "slider", width: 4, height: 1, inactiveLabel: false,
		range:"(2700..6500)") {
        	state "colorTemperature", action:"color temperature.setColorTemperature"
		}
		valueTile("colorTemp", "device.colorTemperature", inactiveLabel: false, decoration: "flat", height: 1, width: 4) {
			state "colorTemp", label: 'ColorTemp: ${currentValue}K'
		}
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
def on() {
	log.info "${device.name} ${device.label}: Turning ON"
	sendCmdtoServer('{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"on_off":1}}}', "commandResponse")
}
def off() {
	log.info "${device.name} ${device.label}: Turning OFF"
	sendCmdtoServer('{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"on_off":0}}}', "commandResponse")
}
def setLevel(percentage) {
	percentage = percentage as int
	log.info "${device.name} ${device.label}: Setting Brightness to ${percentage}%"
	sendCmdtoServer("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"ignore_default":1,
        "on_off":1,"brightness":${percentage}}}}""", "commandResponse")
}
def setColorTemperature(kelvin) {
	kelvin = kelvin as int
	log.info "${device.name} ${device.label}: Setting Color Temperature to ${kelvin}K"
	sendCmdtoServer("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"ignore_default":1,
    	"on_off":1,"color_temp": ${kelvin},"hue":0,"saturation":0}}}""", "commandResponse")
}
def setModeNormal() {
	log.info "${device.name} ${device.label}: Changing Mode to NORMAL"
	sendCmdtoServer("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"mode":"normal"}}}""", "commandResponse")
}
def setModeCircadian() {
	log.info "${device.name} ${device.label}: Changing Mode to CIRCADIAN"
	sendCmdtoServer("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"mode":"circadian"}}}""", "commandResponse")
}
def refresh(){
	log.info "Polling ${device.name} ${device.label}"
	sendCmdtoServer('{"system":{"get_sysinfo":{}}}', "refreshResponse")
}
private sendCmdtoServer(command, action){
	def headers = [:] 
	headers.put("HOST", "$gatewayIP:8082")   // port 8082 must be same as value in TP-LInkServerLite.js
	headers.put("tplink-iot-ip", deviceIP)
    headers.put("tplink-command", command)
	headers.put("command", "deviceCommand")
	sendHubCommand(new physicalgraph.device.HubAction([
		headers: headers],
		device.deviceNetworkId,
		[callback: action]
	))
}
def commandResponse(response){
	if (response.headers["cmd-response"] == "TcpTimeout") {
		log.error "$device.name $device.label: TCP Timeout"
		sendEvent(name: "switch", value: "offline", isStateChange: true)
	} else if (response.headers["cmd-response"] == "invalidServerCmd") {
		log.error "$device.name $device.label: Server Received an Invalid Command"
    } else {
		def cmdResponse = parseJson(response.headers["cmd-response"])
		state =  cmdResponse["smartlife.iot.smartbulb.lightingservice"]["transition_light_state"]
		parseStatus(state)
	}
}
def refreshResponse(response){
	if (response.headers["cmd-response"] == "TcpTimeout") {
		log.error "$device.name $device.label: TCP Timeout"
		sendEvent(name: "switch", value: "offline", isStateChange: true)
	} else if (response.headers["cmd-response"] == "invalidServerCmd") {
		log.error "$device.name $device.label: Server Received an Invalid Command"
    } else {
		def cmdResponse = parseJson(response.headers["cmd-response"])
		state = cmdResponse.system.get_sysinfo.light_state
		parseStatus(state)
	}
}
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
	log.info "$device.name $device.label: Power: ${status} / Mode: ${mode} / Brightness: ${level}% / Color Temp: ${color_temp}K"
	sendEvent(name: "switch", value: status, isStateChange: true)
	sendEvent(name: "bulbMode", value: mode, isStateChange: true)
	sendEvent(name: "level", value: level, isStateChange: true)
	sendEvent(name: "colorTemperature", value: color_temp, isStateChange: true)
}