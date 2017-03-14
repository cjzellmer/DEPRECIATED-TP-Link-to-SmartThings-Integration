/*
This is a lite version of a node.js server supporting TP-Link Devices.  This node server will:
a.  receive raw TP-Link device commands from SmartThings.
b.  encrypt and then send the command to the TP-Link device.
c.  return the response raw data to the SmartThings from the TP-Link Device.
History:
03-13-2017 - Initial release with name TP-LinkServerLite.js
*/
var http = require('http')
var net = require('net')
var server = http.createServer(onRequest)
server.listen(8082) // port must be same as in "sendCmdtoServer" of groovy files.
console.log("TP-Link Server - Lite Edition")
function onRequest(request, response){
	var msg = ''
	var deviceIP = request.headers["tplink-iot-ip"]
	var command = request.headers["command"]
	console.log(" ")
	var date = new Date()
	console.log(date)
	console.log("Sending to IP address: " + deviceIP + " Command: " + command)
	sendCmd(command)
	function sendCmd(command) {
		return new Promise((resolve, reject) => {
		var socket = send(command)
		socket.on('data', (data) => {
		data = decrypt(data.slice(4)).toString('ascii')
		console.log("Command Response sent to SmartThings!")
		response.setHeader("cmd-response", data)
		response.end(msg)
		socket.end()
		if (!data.err_code || data.err_code === 0) {
			resolve(data)
			} else {
			let errMsg = data
			console.log('TP-Link Device TCP error %j' + data)
			reject(new Error(errMsg))
			}
		resolve(data)
		}).on('timeout', () => {
			socket.end()
			let errMsg = 'TP-Link Device TCP timeout'
			console.error(errMsg)
			reject(new Error(errMsg))
		}).on('error', (err) => {
			console.error('TP-Link Device TCP error')
			console.trace(err)
			socket.end()
			reject(err)
			})
		})
		return
	}
	function send(payload) {
		var socket = net.connect(9999, deviceIP)
		socket.setKeepAlive(false)
		socket.setTimeout(0)
   	 	socket.on('connect', () => {
  			socket.write(encrypt(payload))
   	 	})
		socket.on('timeout', () => {
			socket.end()
		})
		socket.on('end', () => {
			socket.end()
		})
		return socket
	}
	function encrypt(input) {
		var buf = Buffer.alloc(input.length)
		var key = 0xAB
		for (var i = 0; i < input.length; i++) {
			buf[i] = input.charCodeAt(i) ^ key
			key = buf[i]
		}
		var bufLength = Buffer.alloc(4)
		bufLength.writeUInt32BE(input.length, 0)
		return Buffer.concat([bufLength, buf], input.length + 4)
	}
	function decrypt(input, firstKey) {
		var buf = Buffer.from(input)
		var key = 0x2B
		var nextKey
		for (var i = 0; i < buf.length; i++) {
			nextKey = buf[i]
			buf[i] = buf[i] ^ key
			key = nextKey
		}
		return buf
	}
}
