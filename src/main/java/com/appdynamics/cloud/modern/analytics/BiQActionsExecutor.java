/**
 * 
 */
package com.appdynamics.cloud.modern.analytics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import com.appdynamics.cloud.modern.ApplicationConstants;
import com.appdynamics.cloud.modern.Logger;
import com.appdynamics.cloud.modern.analytics.model.BiQBizJourney;
import com.appdynamics.cloud.modern.analytics.model.BiQMilestone;
import com.appdynamics.cloud.modern.config.BiQEventsSourceConfig;
import com.appdynamics.cloud.modern.config.BiQMilestoneConfig;
import com.appdynamics.cloud.modern.config.BiQSchemaFieldConfig;
import com.appdynamics.cloud.modern.config.BiQSimulatorConfig;
import com.appdynamics.cloud.modern.config.EventsServiceConfig;
import com.appdynamics.cloud.modern.utils.FileUtils;
import com.appdynamics.cloud.modern.utils.StringUtils;

/**
 * @author James Schneider
 *
 */
public class BiQActionsExecutor implements ApplicationConstants {

	private static Logger logr;
	
	private static BiQSimulatorConfig BIZ_SIM_CONF;
	private static List<Thread> BIQ_DRIVER_THREADS;
	
	/**
	 * 
	 */
	public BiQActionsExecutor() {
		//logr = new Logger(BiQActionsExecutor.class.getSimpleName());
	}	
	
	
	public void executeAction() throws Throwable {
		
		String confPath = System.getProperty(ADBIQSIM_CONF_KEY);
		
		Logger l = new Logger(BiQActionsExecutor.class.getSimpleName(), "info");
		
		l.log("############################################################################################    STARTING APPDYNAMICS BIQ SIMULATOR    ###################################################################################");
		l.carriageReturn();
		
		if (confPath == null || confPath.equals("")) {
			l.error("Missing JVM startup property -D" + ADBIQSIM_CONF_KEY);
			l.error("Please set this property -D" + ADBIQSIM_CONF_KEY + " with the full path to the configuration Yaml file like this | -D" + ADBIQSIM_CONF_KEY + "=/opt/appdynamics/biq-simulator/conf/biq-sim-config.yaml");
			System.exit(1);
		}
		
		
		Yaml yaml = new Yaml(new Constructor(BiQSimulatorConfig.class));
		InputStream inputStream = StringUtils.getFileAsStream(confPath);
		BIZ_SIM_CONF = yaml.load(inputStream);
		
		logr = new Logger(BiQActionsExecutor.class.getSimpleName(), BIZ_SIM_CONF.getLoggingLevel());
		
		String action = System.getProperty(ADBIQSIM_ACTION_KEY);
		
		switch (action) {

		case ADBIQSIM_ACTION_INIT:
			
			this.publish(ADBIQSIM_ACTION_INIT);
			break;

		case ADBIQSIM_ACTION_PUBLISH:
			
			this.publish(ADBIQSIM_ACTION_PUBLISH);
			break;

		case ADBIQSIM_ACTION_DELETE:
			
			break;

		default:
			break;
		}
		
	}
	
	// we want multiple drivers for each biq events source since we want to create a driver for each primary key
	// we will control primary key creation and persistence here in the class with a synchronized method 
	
	// Say we have 10 threads configured for p2p so we start 10 drivers for it, each having a unique primary key
	// and when one driver is done with all the milestones in the biz journey, the driver calls back here to get 
	// the next primary key and start another biz journey run
	
	public void publish(String publishAction) throws Throwable {
		
		EventsServiceConfig eventsSrvcConfig = BIZ_SIM_CONF.getEventsServiceConfig();
		
		List<BiQEventsSourceConfig> biqesConfList = BIZ_SIM_CONF.getBiqEventsSources();
		
		if (biqesConfList != null) {
			
			BIQ_DRIVER_THREADS = new ArrayList<Thread>();
			
			for (BiQEventsSourceConfig biqesConf : biqesConfList) {
				
				this.createSchemaIfRequired(eventsSrvcConfig, biqesConf);

				if (publishAction.equals(ADBIQSIM_ACTION_INIT)) {
					
					biqesConf.setNumberOfThreads(1);
					
					for (BiQMilestoneConfig msConf : biqesConf.getBiqMilestones()) {
						msConf.setTime2NextMilestoneLower(1);
						msConf.setTime2NextMilestoneUpper(2);
						msConf.setNumberOfThreads2Publish(1);
					}
				}
				
				
				List<BiQBizJourney> bizJourneys = this.initBiQEventsSource(eventsSrvcConfig, biqesConf);
				
				
				for (BiQBizJourney bizJ : bizJourneys) {
					
					BiQEventsDriver driver = new BiQEventsDriver(this, BIZ_SIM_CONF.getEventsServiceConfig(), biqesConf, bizJ, publishAction, BIZ_SIM_CONF.getLoggingLevel());
					Thread driverThread = new Thread(driver);
					BIQ_DRIVER_THREADS.add(driverThread);
					driverThread.start();
										
					Thread.currentThread().sleep(biqesConf.getStartDelay() * 1000);
				}
				
				
			}
			
		}
		
		
	}
	
	
	protected synchronized Long getNextPrimaryKey(BiQEventsSourceConfig biqesConf) throws Throwable {
		
		Long pk = null;
		
		String pkStr = null;
		if (FileUtils.fileExists(biqesConf.getPrimaryKeyPersistanceFile())) {
			pkStr = StringUtils.getFileAsString(biqesConf.getPrimaryKeyPersistanceFile());
			pkStr.trim();
			pk = Long.parseLong(pkStr);
			pk = pk + 1;
			
		} else {
			pk = biqesConf.getPrimaryKeyStartNumber();
		}
		
		StringUtils.saveStringAsFile(biqesConf.getPrimaryKeyPersistanceFile(), "" + pk);
		
		return pk;
		
	}
	
	private List<BiQBizJourney> initBiQEventsSource(EventsServiceConfig eventsSrvcConfig, BiQEventsSourceConfig biqesConf) throws Throwable {
		
		List<List<BiQMilestone>> milestoneSegments = this.initMilestoneSegments(eventsSrvcConfig, biqesConf);

		List<BiQBizJourney> bizJourneys = this.initBiQBizJourneys(biqesConf, milestoneSegments);
				
		biqesConf.initSchemaFields();
		
		
		// for debug logging only
		// TODO change info logging to trace
		
//		logr.info("--------------------------------------------------------------------  Schema Field Info ---------------------------------------------------------------------");
//		for (BiQSchemaFieldConfig fieldConf : biqesConf.getSchemaFields()) {
//			String[] sampleData = fieldConf.getSampleData();
//			logr.carriageReturnInfo();
//			if (sampleData != null && sampleData.length > 0) {
//				logr.info("Schema Field : " + fieldConf.getFieldName() + " Sample data is NOT NULL");
//				for (int i = 0; i < sampleData.length; i++) {
//					logr.info(sampleData[i]);
//				}
//				
//			} else {
//				logr.info("Schema Field : " + fieldConf.getFieldName() + " Sample data is NULL");
//			}
//		}
//		
//		int cntr = 1;
//		logr.carriageReturnInfo();
//		logr.info("---------------------------------------------------------------  Business Journey Debug Info ---------------------------------------------------------------");
//		for (BiQBizJourney bizJ : bizJourneys) {
//			
//			StringBuffer buff = new StringBuffer();
//			buff.append("Biz Journey " + cntr + " : PK = " + bizJ.getPrimaryKey() + " ");
//			
//			cntr++;
//
//			for (BiQMilestone ms : bizJ.getMilestones()) {
//				buff.append(": " + ms.getMilestoneConfig().getMilestoneName() + " " + ms.shouldPublish());
//			}
//			
//			
//			logr.info(buff.toString());
//		}
		
		
		
		return bizJourneys;
	}
	
	private List<BiQBizJourney> initBiQBizJourneys(BiQEventsSourceConfig biqesConf, List<List<BiQMilestone>> milestoneSegments) throws Throwable {
		
		
		// a milestone can't be set to publish if the prior milestone was set to not publish
		
		List<BiQBizJourney> bizJourneys = new ArrayList<BiQBizJourney>(biqesConf.getNumberOfThreads());
		
		for (int i = 0; i < biqesConf.getNumberOfThreads(); i++) {
			
			List<BiQMilestone> bizjMstones = new ArrayList<BiQMilestone>(biqesConf.getBiqMilestones().size());
			BiQBizJourney bizJ = new BiQBizJourney(bizjMstones);
			BiQMilestone previousMilestone = null;
			
			for (List<BiQMilestone> msListX : milestoneSegments) {
				
				
				for (BiQMilestone msX : msListX) {
					
					if (!msX.isAdded2Thread()) {

						if (previousMilestone == null) {
							previousMilestone = msX;
							msX.setAdded2Thread(true);
							bizjMstones.add(msX);
							break;
							
						} else {
							
							if (!(msX.shouldPublish() && !previousMilestone.shouldPublish())) {
								previousMilestone = msX;
								msX.setAdded2Thread(true);
								bizjMstones.add(msX);
								break;
							}
						
						}
					}
					
				}
				
				
				
			}
			
			// TODO remove this once we coded doing this in the driver
			//bizJ.setPrimaryKey(this.getNextPrimaryKey(biqesConf));
			
			
			bizJourneys.add(bizJ);
		}
		
		return bizJourneys;
	}
	
	
	private List<List<BiQMilestone>> initMilestoneSegments(EventsServiceConfig eventsSrvcConfig, BiQEventsSourceConfig biqesConf) throws Throwable {
		List<List<BiQMilestone>> milestoneSegments = null;
		
		if (biqesConf.getBiqMilestones() != null && biqesConf.getBiqMilestones().size() > 1) {
			
			milestoneSegments = new ArrayList<List<BiQMilestone>>(biqesConf.getBiqMilestones().size());
			
			for (BiQMilestoneConfig msConf : biqesConf.getBiqMilestones()) {
			
				// here we are creating a list with each entry containing a list of a unique milestone X based on the number of threads
				List<BiQMilestone> milestoneXYZList = new ArrayList<BiQMilestone>(biqesConf.getNumberOfThreads());
				
				for (int i = 0; i < biqesConf.getNumberOfThreads(); i++) {
					
					BiQMilestone ms = new BiQMilestone(msConf);
					milestoneXYZList.add(ms);
					
				}
				
				// here were are setting the number of instances for a specific milestone to publish
				for (int i = 0; i < msConf.getNumberOfThreads2Publish(); i++) {
					
					milestoneXYZList.get(i).setShouldPublish(true);
					
				}
				
				milestoneSegments.add(milestoneXYZList);
			}
			
		}	
		
		return milestoneSegments;
	}
	
	
	private String generateCreateSchemaPayload(BiQEventsSourceConfig biqesConf) throws Throwable {
		
		StringBuffer buff = new StringBuffer();
		
		List<BiQSchemaFieldConfig> scConfList = biqesConf.getSchemaFields();

		String[] lines = new String[scConfList.size()];
		
		for (int i = 0; i < lines.length; i++) {
			lines[i] = scConfList.get(i).getFieldName() + "," + scConfList.get(i).getDataType();
		}
		
		buff.append("{");
		buff.append("\"schema\" : { ");

		for (int cntr = 0; cntr < lines.length; cntr++) {
			
			String[] attrs = lines[cntr].split(",");
			buff.append("\"" + attrs[0] + "\": \"" + attrs[1] + "\"");
			if (cntr < lines.length-1) {
				buff.append(", ");
			}
		}
			
		buff.append(" }");
		buff.append(" }");
		
		return buff.toString();
	}
	
	
	private void createSchema(EventsServiceConfig eventsSrvcConfig, BiQEventsSourceConfig biqesConf) throws Throwable {
		
		String accountName = eventsSrvcConfig.getControllerGlobalAccount();
		String apiKey = eventsSrvcConfig.getEventsServiceApikey();
		String restEndpoint = eventsSrvcConfig.getEventsServiceEndpoint() + "/events/schema/" + biqesConf.getSchemaName();
		String payload = this.generateCreateSchemaPayload(biqesConf);
		
		CloseableHttpClient client = HttpClients.createDefault();
		
		HttpPost request = new HttpPost(restEndpoint);
		request.addHeader("X-Events-API-AccountName", accountName);
		request.addHeader("X-Events-API-Key", apiKey);

		request.addHeader("Content-Type", "application/vnd.appd.events+json;v=2");
		request.addHeader("Accept", "application/vnd.appd.events+json;v=2");

	    StringEntity entity = new StringEntity(payload);
	    request.setEntity(entity);
	    
	    CloseableHttpResponse response = client.execute(request);
		
	    logr.trace(" - Creating schema");
	    logr.trace(" - Schema: " + biqesConf.getSchemaName() + " : HTTP Status: " + response.getStatusLine().getStatusCode());

	    String resp = this.getResponseContent(response.getEntity().getContent(), ContentType.getOrDefault(response.getEntity()).getCharset());
	    
		logr.trace("Create Schema Response");
		logr.trace(resp);		

		HttpClientUtils.closeQuietly(response);
		HttpClientUtils.closeQuietly(client);
		
		
	}
	
	private boolean schemaExists(EventsServiceConfig eventsSrvcConfig, String schemaName) throws Throwable {
		
		String accountName = eventsSrvcConfig.getControllerGlobalAccount();
		String apiKey = eventsSrvcConfig.getEventsServiceApikey();
		String restEndpoint = eventsSrvcConfig.getEventsServiceEndpoint() + "/events/schema/" + schemaName;
		
		CloseableHttpClient client = HttpClients.createDefault();
		
		HttpGet request = new HttpGet(restEndpoint);
		request.addHeader("X-Events-API-AccountName", accountName);
		request.addHeader("X-Events-API-Key", apiKey);

		request.addHeader("Content-Type", "application/vnd.appd.events+json;v=2");
		request.addHeader("Accept", "application/vnd.appd.events+json;v=2");

	    CloseableHttpResponse response = client.execute(request);
		
	    int statusCode = response.getStatusLine().getStatusCode();
	    
	    logr.trace(" - Checking for existing schema");
	    logr.trace(" - Schema: " + schemaName + " : HTTP Status: " + response.getStatusLine().getStatusCode());
	    
	    boolean exists = false;
	    
	    switch (statusCode) {

	    case 200:
	    	exists = true;
			break;

	    case 404:
	    	exists = false;
			break;
			
		default:
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(client);			
			throw new Exception("Unable to check if schema exists | schema name = " + schemaName + " | HTTP status = " + statusCode);
		}
		
	    
		HttpClientUtils.closeQuietly(response);
		HttpClientUtils.closeQuietly(client);
		
		return exists;
	}
	
	private void createSchemaIfRequired(EventsServiceConfig eventsSrvcConfig, BiQEventsSourceConfig biqesConf) throws Throwable {
		if (!this.schemaExists(eventsSrvcConfig, biqesConf.getSchemaName())) {
			this.createSchema(eventsSrvcConfig, biqesConf);
		}
	}
	
	private void deleteSchema(EventsServiceConfig eventsSrvcConfig, String schemaName) throws Throwable {

		String accountName = eventsSrvcConfig.getControllerGlobalAccount();
		String apiKey = eventsSrvcConfig.getEventsServiceApikey();
		String restEndpoint = eventsSrvcConfig.getEventsServiceEndpoint() + "/events/schema/" + schemaName;
		
		CloseableHttpClient client = HttpClients.createDefault();
		HttpDelete request;		
		
		request = new HttpDelete(restEndpoint);
		request.addHeader("X-Events-API-AccountName", accountName);
		request.addHeader("X-Events-API-Key", apiKey);
		
		HttpResponse response = null;
		
		response = client.execute(request);
		
		logr.trace("deleteSchema: HTTP Status Line = " + response.getStatusLine());
		logr.trace("deleteSchema: HTTP Entity = " + this.getResponseContent(response.getEntity().getContent(), ContentType.getOrDefault(response.getEntity()).getCharset()));

		HttpClientUtils.closeQuietly(response);
		HttpClientUtils.closeQuietly(client);
		
	}
	
	private String getResponseContent(InputStream inputStream, Charset charset) throws IOException {
		 
		StringBuilder stringBuilder = new StringBuilder();
		String line = null;
		
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, charset))) {	
			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line);
			}
		}
	 
		return stringBuilder.toString();
	}	
	
}
