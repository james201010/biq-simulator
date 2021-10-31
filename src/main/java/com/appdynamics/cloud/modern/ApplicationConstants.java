/**
 * 
 */
package com.appdynamics.cloud.modern;

/**
 * @author James Schneider
 *
 */
public interface ApplicationConstants {

	
	// The absolute path of the setup.yaml file 
	public static final String ADBIQSIM_CONF_KEY = "appdBiqSimConf";
	
	// The action to execute (publish or delete)
	public static final String ADBIQSIM_ACTION_KEY = "appdBiqSimAction";
	
	// The name of the schema to delete if the delete action is used
	public static final String ADBIQSIM_SCHEMA_KEY = "appdBiqSimSchemaName";
	
	// The init action will only publish 1 business journey for each defined 'biqEventsSource'
	public static final String ADBIQSIM_ACTION_INIT = "init";	
	
	// The publish action will publish event continuously
	public static final String ADBIQSIM_ACTION_PUBLISH = "publish";
	
	// The delete action will check to see if the schema passed in the 'appdBiqSimSchemaName' parameter exists and if so, will delete it
	public static final String ADBIQSIM_ACTION_DELETE = "delete";
	
}
