/**
 *  Copyright 2015 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Garage Door Monitor
 *
 *  Author: Brian Mowery
 */
definition(
    name: "Garage Door Monitor",
    namespace: "bemowery",
    author: "Brian Mowery",
    description: "Monitor your garage door and close it if it is open too long",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_contact.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_contact@2x.png"
)

preferences {
	section("When the garage door is open...") {
		input "theDoor", "capability.garageDoorControl", title: "Which?"
	}
	section("For too long...") {
		input "maxOpenTime", "number", title: "Minutes?"
	}
}

def installed()
{
	subscribe(theDoor, "contact.open", doorOpenHandler)
}

def updated()
{
	unsubscribe()
	subscribe(theDoor, "contact.open", doorOpenHandler)
}

def doorOpenHandler(evt) {
	def doorState = theDoor.currentValue("door")
    def isOpen = doorState == "open"
    def isScheduled = state.status == "scheduled"
    
    if(!isScheduled){
    	scheduleCloseDoor()
    }
    else{
    	log.debug "Door is already scheduled to be closed."
    }
  
}

def scheduleCloseDoor(){
	def timeToClose = maxOpenTime * 60
    
    log.debug "Scheduling door to close in ${timeToClose} seconds"
    
	runIn(timeToClose, closeDoor)
	state.status = "scheduled"
}

def retryCloseDoor(times){
	if(times >= 0){
    	
    }
}

def closeDoor(){
	def doorState = theDoor.currentValue("door")
    def isOpen = doorState == "open"
    
    if (isOpen) {
        log.debug "closing the door"
        sendNotification("Garage door has been open too long. Attempting to close.", [method: "push"])
		theDoor.close()
        clearStatus()
    }
    else{
    	clearStatus()
    	log.debug "Nothing to do. Door is not open."
    }
}

def clearStatus() {
	state.status = null
}