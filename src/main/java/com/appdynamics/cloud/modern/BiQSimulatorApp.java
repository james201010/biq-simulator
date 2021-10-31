package com.appdynamics.cloud.modern;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class BiQSimulatorApp {

	public static void main(String[] args) {
		
		
	    SpringApplication springApplication = new SpringApplication(BiQSimulatorApp.class);
	    springApplication.addListeners(new BiQSimulatorAppListener());
	    springApplication.run(args);
	
	}

}
