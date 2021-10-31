/**
 * 
 */
package com.appdynamics.cloud.modern.config;

import java.util.List;

/**
 * @author James Schneider
 *
 */
public class BiQSimulatorConfig {

	private String loggingLevel = "info";
	private EventsServiceConfig eventsServiceConfig;
	private List<BiQEventsSourceConfig> biqEventsSources;
	
	
	/**
	 * 
	 */
	public BiQSimulatorConfig() {
		
	}

	public String getLoggingLevel() {
		return loggingLevel;
	}

	public void setLoggingLevel(String loggingLevel) {
		this.loggingLevel = loggingLevel;
	}

	public EventsServiceConfig getEventsServiceConfig() {
		return eventsServiceConfig;
	}

	public void setEventsServiceConfig(EventsServiceConfig eventsServiceConfig) {
		this.eventsServiceConfig = eventsServiceConfig;
	}

	public List<BiQEventsSourceConfig> getBiqEventsSources() {
		return biqEventsSources;
	}

	public void setBiqEventsSources(List<BiQEventsSourceConfig> biqEventsSources) {
		this.biqEventsSources = biqEventsSources;
	}




	
	
}
