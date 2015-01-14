metadata {
	// Automatically generated. Make future change here.
	definition (name: "NXP module", namespace: "jdeltoft", author: "Justin Eltoft") {
		capability "Motion Sensor"
       	capability "Refresh"
		capability "Configuration"

		fingerprint inClusters: "0000,0003,0406", outClusters: "0003,0004,0005", manufacturer: "NXP", model: "ZHA-OccupancySensor"
		fingerprint inClusters: "0000,0003,0406", outClusters: "0003,0004,0005", manufacturer: "NXP"
	}

//fingerprint profileId: "0104", deviceId:"0x0107", inClusters: "0000,0003,0406", outClusters: "0003,0004,0005", manufacturer: "NXP"

	// simulator metadata
	simulator {
	}

	// UI tile definitions
	tiles {
        standardTile("motion", "device.motion", width: 2, height: 2) {
			state("active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#53a7c0")
			state("inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff")
		}
        
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        
        standardTile("configure", "device.motion", inactiveLabel: false, decoration: "flat") {
			state "configure", action:"configuration.configure", icon:"st.secondary.configure"
		}


		main "motion"
  		details(["motion","refresh","configure"])
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
    log.debug "JDE: description: $description"
    
   	Map map = [:]
	if (description?.startsWith('read attr -')) {
		map = parseReportAttributeMessage(description)
	}
 
	log.debug "Parse returned $map"
    
	def result = map ? createEvent(map) : null
    
    return result

}

//
//JDE: description: read attr - raw: 74510104060800001800, dni: 7451, endpoint: 01, cluster: 0406, size: 08, attrId: 0000, encoding: 18, value: 00
//
private Map parseReportAttributeMessage(String description) {
	Map descMap = (description - "read attr - ").split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
	log.debug "Desc Map: $descMap"
 
	Map resultMap = [:]
    if (descMap.cluster == "0406" && descMap.attrId == "0000") {
    	def value = descMap.value.endsWith("01") ? "active" : "inactive"
    	resultMap = getMotionResult(value)
    } 
 
	return resultMap
}

private Map getMotionResult(value) {
	log.debug 'motion'
	String linkText = getLinkText(device)
	String descriptionText = value == 'active' ? "${linkText} detected motion" : "${linkText} motion has stopped"
	return [
		name: 'motion',
		value: value,
		descriptionText: descriptionText
	]
}

def updated() {
    response(configure())
}

def refresh()
{
	log.debug "JDE: Refresh called y"
	//"st rattr 0x${device.deviceNetworkId} 1 0x406 0"
}

def configure() {
	log.debug "JDE: Configuring bindings."
	String zigbeeId = swapEndianHex(device.hub.zigbeeId)
	def configCmds = [
     	"zdo bind 0x${device.deviceNetworkId} 1 1 0x406 {${device.zigbeeId}} {}"
	]
    return configCmds + refresh()  // send refresh cmds as part of config
}

private hex(value) {
	new BigInteger(Math.round(value).toString()).toString(16)
}

private String swapEndianHex(String hex) {
    reverseArray(hex.decodeHex()).encodeHex()
}

private byte[] reverseArray(byte[] array) {
    int i = 0;
    int j = array.length - 1;
    byte tmp;
    while (j > i) {
        tmp = array[j];
        array[j] = array[i];
        array[i] = tmp;
        j--;
        i++;
    }
    return array
}

