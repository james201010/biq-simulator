/**
 * 
 */
package com.appdynamics.cloud.modern;

import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import com.appdynamics.cloud.modern.analytics.BiQActionsExecutor;

/**
 * @author James Schneider
 *
 */
public class BiQSimulatorAppListener implements ApplicationConstants, ApplicationListener<ApplicationEvent> {

	private static Logger logr;
	private static BiQActionsExecutor EXECUTOR;
	
	
	/**
	 * 
	 */
	public BiQSimulatorAppListener() {
		logr = new Logger(BiQSimulatorAppListener.class.getSimpleName());
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		
		if (event instanceof AvailabilityChangeEvent) {
			
			AvailabilityChangeEvent<?> ace = (AvailabilityChangeEvent<?>)event;
			
			if (ace.getState().equals(ReadinessState.ACCEPTING_TRAFFIC)) {
		
				try {
					
					logr.printBanner(true);
					
					EXECUTOR = new BiQActionsExecutor();
					EXECUTOR.executeAction();
					
				} catch (Throwable ex) {
					ex.printStackTrace();
				}
				
				
			}
			
		}
	}
	
//	private void initializeServices() throws Throwable {
//		
//		String confPath = System.getProperty(ADBIQSIM_CONF_KEY);
//		Yaml yaml = new Yaml(new Constructor(BiQSimulatorConfig.class));
//		InputStream inputStream = StringUtils.getFileAsStream(confPath);
//		
//		SRVCS_CONF = yaml.load(inputStream);	
//						
//        EventsServiceConfig eventsSrvcConfig = SRVCS_CONF.getEventsServiceConfig();
//        		
//		
//        List<AnalyticsEventsSourceConfig> aescList = SRVCS_CONF.getAnalyticsEventsSources();
//        if (aescList != null) {
//        	
//        	ANALYTICS_DRIVER_THREADS = new ArrayList<Thread>();
//        	
//        	for (AnalyticsEventsSourceConfig aesConf : aescList) {
//        		
//        		
//        		//if (aesConf.getSchemaName().equals("loans_premod")) {
//        			
//                	AnalyticsEventsDriver driver;
//                	AnalyticsEventsSource source;
//        			
//        			Class<?> clazz = Class.forName(aesConf.getEventsSourceClass());
//        			Object object = clazz.newInstance();
//        			source = (AnalyticsEventsSource)object;
//        			
//        			source.initialize(aesConf);
//        			driver = new AnalyticsEventsDriver(eventsSrvcConfig, source);
//        			Thread driverThread = new Thread(driver);
//        			driverThread.start();
//        			ANALYTICS_DRIVER_THREADS.add(driverThread);
//        			
//        		//}
//        		
//        	}
//        }
//		
//	}

}
