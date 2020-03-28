package edu.carleton.comp4601.resources;

import java.io.IOException;
import java.util.logging.Level;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.carleton.comp4601.utility.SearchServiceManager;

public class CustomContextClassListener implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		ServletContextListener.super.contextDestroyed(sce);
		
		SearchServiceManager.getInstance().log(Level.INFO, "Stopping Search Service Manager");
		SearchServiceManager.getInstance().stop();
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContextListener.super.contextInitialized(sce);
		
		try {
			SearchServiceManager.getInstance().log(Level.INFO, "Starting Search Service Manager");
			SearchServiceManager.getInstance().start();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}

	
}
