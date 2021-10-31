/**
 * 
 */
package com.appdynamics.cloud.modern.analytics;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.appdynamics.cloud.modern.ApplicationConstants;
import com.appdynamics.cloud.modern.Logger;
import com.appdynamics.cloud.modern.analytics.model.BiQBizJourney;
import com.appdynamics.cloud.modern.analytics.model.BiQMilestone;
import com.appdynamics.cloud.modern.config.BiQEventsSourceConfig;
import com.appdynamics.cloud.modern.config.BiQSchemaFieldConfig;
import com.appdynamics.cloud.modern.config.EventsServiceConfig;

/**
 * @author James Schneider
 *
 */
public class BiQEventsDriver implements ApplicationConstants, Runnable {

	private Logger logr;
	private BiQActionsExecutor biqExecutor;
	private EventsServiceConfig eventsSrvcConfig;
	private BiQEventsSourceConfig biqesConf;
	private BiQBizJourney bizJ;
	private String[] schemaFieldsArray = null;
	private String publishAction;
	
	/**
	 * 
	 */
	public BiQEventsDriver(BiQActionsExecutor biqExecutor, EventsServiceConfig eventsSrvcConfig, BiQEventsSourceConfig biqesConf, BiQBizJourney bizJourney, String publishAction, String logLevel) {
		logr = new Logger(BiQEventsDriver.class.getSimpleName(), logLevel);
		this.biqExecutor = biqExecutor;
		this.eventsSrvcConfig = eventsSrvcConfig;
		this.biqesConf = biqesConf;
		this.bizJ = bizJourney;
		this.publishAction = publishAction;
	}	
	
	@Override
	public void run() {

		try {			
			// this should only happen once
			this.initSchemaFields();
		} catch (Throwable ex) {
			ex.printStackTrace();
		}

		if (this.publishAction.equals(ADBIQSIM_ACTION_PUBLISH))	{
			
			try {
				// this should happen once in the very start and then only after the last milestone is processed
				this.initBizJourney();
			} catch (Throwable ex) {
				ex.printStackTrace();
			}

			try {
				while (true) {

					for (int i = 0; i < this.bizJ.getMilestones().size(); i++) {
						
						BiQMilestone ms = this.bizJ.getMilestones().get(i);
						
						if (ms.shouldPublish()) {
							
							this.publishEvent(ms);
							
							Random r = new Random();
							int low = ms.getMilestoneConfig().getTime2NextMilestoneLower();
							int high = ms.getMilestoneConfig().getTime2NextMilestoneUpper() + 1;
							int result = r.nextInt(high-low) + low;
							
							logr.carriageReturnInfo();
							logr.info("!!!!!!!!!!!!!!!!  TIME 2 SLEEP = " + result);
							logr.carriageReturnInfo();
							
							
							Thread.currentThread().sleep(result * 1000);
							
						}
					}
					
					this.initBizJourney();
					
				}
				
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
			
			
		} else {
			
		}
		
		
		
		
		
		
	}
	
	private void initBizJourney() throws Throwable {
		
		StringBuffer buff = null;
		String primaryKey = this.biqExecutor.getNextPrimaryKey(this.biqesConf) + "";
		Map<String, String> sampleData = new HashMap<String, String>();
		
		for (BiQSchemaFieldConfig fld : this.biqesConf.getSchemaFields()) {
			
			if ( (!fld.getFieldName().equals(this.biqesConf.getPrimaryKeySchemaFieldName())) && 
					(!fld.getFieldName().equals(this.biqesConf.getCriteriaSchemaFieldName())) ) {
				
				String[] sampArray = fld.getSampleData();
				
				if (sampArray != null && sampArray.length > 0) {
					
					if (sampArray.length > 1) {
						Random rand = new Random();
						int randNum = rand.nextInt(sampArray.length);
						sampleData.put(fld.getFieldName(), sampArray[randNum]);
					} else {
						sampleData.put(fld.getFieldName(), sampArray[0]);
					}
				}

			}
		}
		
		// TODO change to trace
		logr.carriageReturnTrace();
		
		for (BiQMilestone ms : this.bizJ.getMilestones()) {
			
			buff = new StringBuffer();
			BiQSchemaFieldConfig field;
			
			for (int i = 0; i < this.biqesConf.getSchemaFields().size(); i++) {
				
				field = this.biqesConf.getSchemaFields().get(i);
				
				if (field.getFieldName().equals(this.biqesConf.getPrimaryKeySchemaFieldName())) {
					
					buff.append(this.biqesConf.getPrimaryKeyPrefix() + primaryKey);
					
				} else if (field.getFieldName().equals(this.biqesConf.getCriteriaSchemaFieldName())) {
					
					buff.append(ms.getMilestoneConfig().getCriteriaSchemaFieldValue());
					
				} else {
					
					buff.append(sampleData.get(field.getFieldName()));
				}
				
				if ( i < (this.biqesConf.getSchemaFields().size() - 1) ) {
					buff.append(",");
				}
				
			}
			
			ms.setDataString(buff.toString());
			
			// TODO change to trace
			logr.trace(ms.getDataString());
			logr.trace(this.getEventPayload(ms));
			
		}
		
	}
	
	private String getEventPayload(BiQMilestone ms) throws Throwable {
		
		StringBuffer buff = null;
		long currTime = Calendar.getInstance().getTimeInMillis();
		
		buff = new StringBuffer();
		buff.append("[{");
		// -------------------------------------------------------
		
		String[] dataFields = ms.getDataString().split(",");
		
		for (int slCntr = 0; slCntr < this.schemaFieldsArray.length; slCntr++) {
			
			String[] schemaFields = this.schemaFieldsArray[slCntr].split(",");
			
			if (schemaFields[1].toLowerCase().equals("string") || schemaFields[1].toLowerCase().equals("date")) {
				
				buff.append("\""+ schemaFields[0] + "\": \"" + dataFields[slCntr] + "\"");
				
			} else if (schemaFields[1].toLowerCase().equals("boolean")) {
				// NO quotes around the true or false values
				if (dataFields[slCntr] != null && dataFields[slCntr].equals("true")) {
					buff.append("\""+ schemaFields[0] + "\": " + dataFields[slCntr]);	
				} else {
					buff.append("\""+ schemaFields[0] + "\": " + "false");
				}
				
				
			} else {
				
				buff.append("\""+ schemaFields[0] + "\": " + dataFields[slCntr]);
			}
			
			
			if (slCntr < this.schemaFieldsArray.length) {
				buff.append(",");
			}

			
		} 
		
		currTime = currTime - (3 * 6000);
		this.buildSingleMetricAttribute(buff, "eventTimestamp", "" + currTime, "float");
				
		buff.append("}]");
		
		return buff.toString();
		
	}
	
	private void buildSingleMetricAttribute(StringBuffer buff, String attrName, String attrVal, String dataType) throws Throwable {
		
		if (dataType.toLowerCase().equals("string") || dataType.toLowerCase().equals("date")) {
			buff.append("\""+ attrName + "\": \"" + attrVal + "\"");
		} else {
			buff.append("\""+ attrName + "\": " + attrVal);
		}
		
	}
	
	private void initSchemaFields() throws Throwable {
		this.schemaFieldsArray = new String[this.biqesConf.getSchemaFields().size()];
		
		for (int i = 0; i < schemaFieldsArray.length; i++) {
			
			schemaFieldsArray[i] = this.biqesConf.getSchemaFields().get(i).getFieldName() + "," + this.biqesConf.getSchemaFields().get(i).getDataType();
		}
	}
	
	
	private void publishEvent(BiQMilestone ms) throws Throwable {

		String accountName = eventsSrvcConfig.getControllerGlobalAccount();
		String apiKey = eventsSrvcConfig.getEventsServiceApikey();
		String restEndpoint = eventsSrvcConfig.getEventsServiceEndpoint() + "/events/publish/" + biqesConf.getSchemaName();
		String payload = this.getEventPayload(ms);
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		
		StringEntity json;
		HttpPost request;		
		
		request = new HttpPost(restEndpoint);
		request.addHeader("X-Events-API-AccountName", accountName);
		request.addHeader("X-Events-API-Key", apiKey);

		request.addHeader("Content-Type", "application/vnd.appd.events+json;v=2");
		request.addHeader("Accept", "application/vnd.appd.events+json;v=2");
		
		logr.trace("publishEvent: JSON Payload = " + payload);
		json = new StringEntity(payload);
		request.setEntity(json);
		HttpResponse response = null;
		
		response = httpClient.execute(request);
		
		logr.trace("publishEvent: HTTP Status Line = " + response.getStatusLine());
		//logInfo("publishEvent: HTTP Entity = " + this.getResponseContent(response.getEntity().getContent(), ContentType.getOrDefault(response.getEntity()).getCharset()  ) );

		HttpClientUtils.closeQuietly(response);
		HttpClientUtils.closeQuietly(httpClient);
		httpClient = null;
		
		logr.info(payload);
		
	}
	
}
