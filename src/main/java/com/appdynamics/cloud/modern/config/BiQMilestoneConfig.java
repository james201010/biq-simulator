/**
 * 
 */
package com.appdynamics.cloud.modern.config;

/**
 * @author James Schneider
 *
 */
public class BiQMilestoneConfig {

	private String milestoneName;
	private String criteriaSchemaFieldValue;
	private Integer time2NextMilestoneLower;
	private Integer time2NextMilestoneUpper;
	private Integer numberOfThreads2Publish;
	
	/**
	 * 
	 */
	public BiQMilestoneConfig() {
		
	}

	public String getMilestoneName() {
		return milestoneName;
	}

	public void setMilestoneName(String milestoneName) {
		this.milestoneName = milestoneName;
	}

	public String getCriteriaSchemaFieldValue() {
		return criteriaSchemaFieldValue;
	}

	public void setCriteriaSchemaFieldValue(String criteriaSchemaFieldValue) {
		this.criteriaSchemaFieldValue = criteriaSchemaFieldValue;
	}

	public Integer getTime2NextMilestoneLower() {
		return time2NextMilestoneLower;
	}

	public void setTime2NextMilestoneLower(Integer time2NextMilestoneLower) {
		this.time2NextMilestoneLower = time2NextMilestoneLower;
	}

	public Integer getTime2NextMilestoneUpper() {
		return time2NextMilestoneUpper;
	}

	public void setTime2NextMilestoneUpper(Integer time2NextMilestoneUpper) {
		this.time2NextMilestoneUpper = time2NextMilestoneUpper;
	}

	public Integer getNumberOfThreads2Publish() {
		return numberOfThreads2Publish;
	}

	public void setNumberOfThreads2Publish(Integer percent2Process) {
		this.numberOfThreads2Publish = percent2Process;
	}

}
