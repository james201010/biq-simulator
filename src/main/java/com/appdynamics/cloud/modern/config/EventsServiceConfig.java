/**
 * 
 */
package com.appdynamics.cloud.modern.config;

/**
 * @author James Schneider
 *
 */
public class EventsServiceConfig {

    private String eventsServiceEndpoint;
    private String eventsServiceApikey;
    private String controllerGlobalAccount;
    
    
	/**
	 * 
	 */
	public EventsServiceConfig() {
		
	}


	public String getEventsServiceEndpoint() {
		return eventsServiceEndpoint;
	}


	public void setEventsServiceEndpoint(String eventsServiceEndpoint) {
		this.eventsServiceEndpoint = eventsServiceEndpoint;
	}


	public String getEventsServiceApikey() {
		return eventsServiceApikey;
	}


	public void setEventsServiceApikey(String eventsServiceApikey) {
		this.eventsServiceApikey = eventsServiceApikey;
	}


	public String getControllerGlobalAccount() {
		return controllerGlobalAccount;
	}


	public void setControllerGlobalAccount(String controllerGlobalAccount) {
		this.controllerGlobalAccount = controllerGlobalAccount;
	}

	

	
	
}
