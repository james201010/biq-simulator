/**
 * 
 */
package com.appdynamics.cloud.modern.analytics.model;

import com.appdynamics.cloud.modern.config.BiQMilestoneConfig;

/**
 * @author James Schneider
 *
 */
public class BiQMilestone {

	private boolean added2Thread = false;
	private boolean shouldPublish = false;
	private BiQMilestoneConfig milestoneConfig;
	private String dataString;
	
	/**
	 * 
	 */
	public BiQMilestone(BiQMilestoneConfig milestoneConf) {
		this.milestoneConfig = milestoneConf;
	}

	public BiQMilestoneConfig getMilestoneConfig() {
		return this.milestoneConfig;
	}

	public boolean shouldPublish() {
		return shouldPublish;
	}

	public void setShouldPublish(boolean shouldPublish) {
		this.shouldPublish = shouldPublish;
	}

	public boolean isAdded2Thread() {
		return added2Thread;
	}

	public void setAdded2Thread(boolean added2Thread) {
		this.added2Thread = added2Thread;
	}

	public String getDataString() {
		return dataString;
	}

	public void setDataString(String dataString) {
		this.dataString = dataString;
	}
	
	
}
