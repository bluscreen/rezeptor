package de.dhbw.rezeptor.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Laden der Systemproperties aus config.properties File!
 * @author dhammacher
 *
 */

public class SystemProperties {
	static Properties rezeptor = new Properties();

	public SystemProperties() {
	}

	public static void buildProperties() {
		InputStream input = null;

		try {
			input = SystemProperties.class
					.getResourceAsStream("/config.properties");
			rezeptor.load(input);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static Properties getProperties() {
		buildProperties();
		return rezeptor;
	}
}
