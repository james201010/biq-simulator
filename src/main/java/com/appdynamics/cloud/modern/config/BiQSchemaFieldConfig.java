/**
 * 
 */
package com.appdynamics.cloud.modern.config;

import com.appdynamics.cloud.modern.utils.FileUtils;
import com.appdynamics.cloud.modern.utils.StringUtils;

/**
 * @author James Schneider
 *
 */
public class BiQSchemaFieldConfig {
	
	private String[] sampleData = null;
	private String fieldName;
	private String dataType;
	private String sampleDataFile;
	
	/**
	 * 
	 */
	public BiQSchemaFieldConfig() {
		
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getSampleDataFile() {
		return sampleDataFile;
	}

	public void setSampleDataFile(String sampleDataFile) {
		this.sampleDataFile = sampleDataFile;
	}

	public String[] getSampleData() {
		return sampleData;
	}

	public void initSampleData() throws Throwable {
		String sampleFile = this.getSampleDataFile();
		if (sampleFile != null && !sampleFile.equals("")) {
			if (FileUtils.fileExists(sampleFile)) {
				this.sampleData = StringUtils.getFileAsArrayOfLines(sampleFile);
			}
		}
	}
	
}
