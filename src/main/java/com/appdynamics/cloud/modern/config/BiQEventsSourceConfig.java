/**
 * 
 */
package com.appdynamics.cloud.modern.config;

import java.util.List;

/**
 * @author James Schneider
 *
 */
public class BiQEventsSourceConfig {

	private String schemaName;
	private Integer numberOfThreads;
	private String primaryKeySchemaFieldName;
	private String primaryKeyPrefix;
	private Long primaryKeyStartNumber; 
	private String primaryKeyPersistanceFile;
	private String criteriaSchemaFieldName;
	private Integer startDelay;
	
	List<BiQMilestoneConfig> biqMilestones;
	List<BiQSchemaFieldConfig> schemaFields;
	
	
	/**
	 * 
	 */
	public BiQEventsSourceConfig() {
		
	}
	
	public String getSchemaName() {
		return schemaName;
	}
	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}
	public Integer getNumberOfThreads() {
		return numberOfThreads;
	}
	public void setNumberOfThreads(Integer numberOfThreads) {
		this.numberOfThreads = numberOfThreads;
	}
	public String getPrimaryKeySchemaFieldName() {
		return primaryKeySchemaFieldName;
	}
	public void setPrimaryKeySchemaFieldName(String primaryKeySchemaFieldName) {
		this.primaryKeySchemaFieldName = primaryKeySchemaFieldName;
	}
	public String getPrimaryKeyPrefix() {
		return primaryKeyPrefix;
	}
	public void setPrimaryKeyPrefix(String primaryKeySchemaFieldPrefix) {
		this.primaryKeyPrefix = primaryKeySchemaFieldPrefix;
	}
	public Long getPrimaryKeyStartNumber() {
		return primaryKeyStartNumber;
	}
	public void setPrimaryKeyStartNumber(Long primaryKeySchemaFieldStartNumber) {
		this.primaryKeyStartNumber = primaryKeySchemaFieldStartNumber;
	}
	public String getPrimaryKeyPersistanceFile() {
		return primaryKeyPersistanceFile;
	}
	public void setPrimaryKeyPersistanceFile(String primaryKeyPersistanceFile) {
		this.primaryKeyPersistanceFile = primaryKeyPersistanceFile;
	}
	public String getCriteriaSchemaFieldName() {
		return criteriaSchemaFieldName;
	}
	public void setCriteriaSchemaFieldName(String criteriaSchemaFieldName) {
		this.criteriaSchemaFieldName = criteriaSchemaFieldName;
	}	
	public Integer getStartDelay() {
		return startDelay;
	}
	public void setStartDelay(Integer startDelay) {
		this.startDelay = startDelay;
	}

	public List<BiQMilestoneConfig> getBiqMilestones() {
		return biqMilestones;
	}

	public void setBiqMilestones(List<BiQMilestoneConfig> biqMilestones) {
		this.biqMilestones = biqMilestones;
	}

	public List<BiQSchemaFieldConfig> getSchemaFields() {
		return schemaFields;
	}

	public void setSchemaFields(List<BiQSchemaFieldConfig> schemaFields) {
		this.schemaFields = schemaFields;
	}
	
	public void initSchemaFields() throws Throwable {
		
		for (BiQSchemaFieldConfig fieldConf : this.getSchemaFields()) {
			fieldConf.initSampleData();
		}
	}
}
