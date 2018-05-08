package io.csra.wily.components.listener;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * This servlet context listener handles forcibly destroying the JDBC driver, which can cause memory leaks and
 * unintended behavior in the application.
 *
 * See: http://stackoverflow.com/questions/3320400/to-prevent-a-memory-leak-the-jdbc-driver-has-been-forcibly-unregistered/23912257#23912257
 *
 * @author ndimola
 *
 */
@Component
public class CustomServletContextListener implements ServletContextListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomServletContextListener.class);

	/**
	 * See the following article for an explanation on why this needs to be here:
	 * http://stackoverflow.com/questions/3320400/to
	 * -prevent-a-memory-leak-the-jdbc-driver-has-been-forcibly-unregistered/23912257#23912257
	 * 
	 */
	public final void contextDestroyed(ServletContextEvent sce) {
		// Deregister JDBC drivers in this context's ClassLoader:
		// Get the webapp's ClassLoader
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		// Loop through all drivers
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements()) {
			Driver driver = drivers.nextElement();
			if (driver.getClass().getClassLoader() == cl) {
				// This driver was registered by the webapp's ClassLoader, so deregister it:
				try {
					LOGGER.info("Deregistering JDBC driver {}", driver);
					DriverManager.deregisterDriver(driver);
				} catch (SQLException ex) {
					LOGGER.error("Error deregistering JDBC driver {}", driver, ex);
				}
			} else {
				// driver was not registered by the webapp's ClassLoader and may be in use elsewhere
				LOGGER.trace("Not deregistering JDBC driver {} as it does not belong to this webapp's ClassLoader", driver);
			}
		}
	}

	public void contextInitialized(ServletContextEvent sce) {
		// Do nothing
	}

}
