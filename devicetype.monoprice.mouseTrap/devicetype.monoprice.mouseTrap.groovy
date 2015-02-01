metadata {
	// Automatically generated. Make future change here.
	definition (name: "Zwave Mouse Trap", namespace: "jdeltoft", author: "Justin Eltoft") {
		capability "Sensor"
		capability "Battery"

		attribute "sensor", "string"
	}

	simulator {
		status "active": "command: 3003, payload: FF"
		status "inactive": "command: 3003, payload: 00"
	}

	tiles {
		standardTile("sensor", "device.sensor", width: 2, height: 2) {
			state("dead", label:'dE4D moUZe', icon:"st.Seasonal Fall.seasonal-fall-006", backgroundColor:"#ed092b")
			state("clean", label:'7R4p cL34n', icon:"st.Food & Dining.dining4", backgroundColor:"#33ef1a")
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat") {
			state "battery", label:'${currentValue}% battery', unit:""
		}

		main "sensor"
		details(["sensor", "battery"])
	}
}

def parse(String description) {
	def result = []
	if (description.startsWith("Err")) {
	    result = createEvent(descriptionText:description, displayed:true)
	} else {
		def cmd = zwave.parse(description, [0x20: 1, 0x30: 1, 0x31: 5, 0x32: 3, 0x80: 1, 0x84: 1, 0x71: 1, 0x9C: 1])
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	return result
}

def sensorValueEvent(Short value) {
	if (value == 0) {
		createEvent([ name: "sensor", value: "clean" ])
	} else if (value == 255) {
		//createEvent([ name: "sensor", value: "dead" ])
        createEvent(name: "sensor", value: "dead", descriptionText: "$device.displayName caught a mouse!!")
	} else {
	    log.debug "Invalid Sensor Value:$value"
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{
	sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd)
{
	sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd)
{
	sensorValueEvent(cmd.sensorValue)
}

def zwaveEvent(physicalgraph.zwave.commands.alarmv1.AlarmReport cmd)
{
	sensorValueEvent(cmd.alarmLevel)
}

def zwaveEvent(physicalgraph.zwave.commands.sensoralarmv1.SensorAlarmReport cmd)
{
	sensorValueEvent(cmd.sensorState)
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} has a low battery"
	} else {
		map.value = cmd.batteryLevel
	}
	createEvent(map)
}
