/**
 * 
 */
package com.appdynamics.cloud.modern.analytics.model;

import java.util.List;

/**
 * @author James Schneider
 *
 */
public class BiQBizJourney {

	private Long primaryKey;
	private List<BiQMilestone> milestones;
	
	/**
	 * 
	 */
	public BiQBizJourney(List<BiQMilestone> milestoneList) {
		this.milestones = milestoneList;
	}

	
	public Long getPrimaryKey() {
		return primaryKey;
	}


	public void setPrimaryKey(Long primaryKey) {
		this.primaryKey = primaryKey;
	}


	public List<BiQMilestone> getMilestones() {
		return milestones;
	}

	
	
}
