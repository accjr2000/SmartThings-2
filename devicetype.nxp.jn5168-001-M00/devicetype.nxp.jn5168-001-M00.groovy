
metadata {
	// Automatically generated. Make future change here.
	definition (name: "NXP module", namespace: "jdeltoft", author: "Justin Eltoft") {
		capability "Motion Sensor"

		fingerprint profileId: "0104", deviceId:"0x0107", inClusters: "0000,0003,0406", outClusters: "0003"
	}

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
        
        standardTile("Configure", "device.configure", inactiveLabel: false, decoration: "flat") {
			state "default", action:"configure", icon:"st.secondary.refresh"
		}


		main "motion"
  		details(["motion","refresh","Configure"])
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
   log.debug "JDE: description: $description"
   return []
}

def refresh()
{
	log.debug "JDE: Refresh called"
	"st rattr 0x${device.deviceNetworkId} 1 0x406 0"
}

def configure() {

	String zigbeeId = swapEndianHex(device.hub.zigbeeId)
	log.debug "JDE: Configuring bindings."
	def configCmds = [
     	"zdo bind 0x${device.deviceNetworkId} 1 1 0x406 {${device.zigbeeId}} {}"
	]
    return configCmds + refresh() // send refresh cmds as part of config
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
