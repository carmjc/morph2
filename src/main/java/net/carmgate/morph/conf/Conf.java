package net.carmgate.morph.conf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Conf {

	public static enum ConfItem {
		MORPH_SIMPLEPROPULSOR_MAXFORCE("morph.simplePropulsor.maxForce"),
		MORPH_SIMPLEPROPULSOR_MAXFORCE_FACTORPERLEVEL("morph.simplePropulsor.maxForce.factorPerLevel"),
		MORPH_SIMPLEPROPULSOR_MAXSPEED("morph.simplePropulsor.maxSpeed"),
		MORPH_SIMPLEPROPULSOR_MAXSPEED_FACTORPERLEVEL("morph.simplePropulsor.maxSpeed.factorPerLevel"),
		MORPH_SIMPLEPROPULSOR_MAXANGLESPEEDPERMASSUNIT("morph.simplePropulsor.maxAngleSpeedPerMassUnit");

		private final String key;

		ConfItem(String key) {
			this.key = key;
		}

		public String getKey() {
			return key;
		}
	}

	private static final Conf _instance = new Conf();
	private static final Logger LOGGER = LoggerFactory.getLogger(Conf.class);

	public static float getFloatProperty(ConfItem confItem) {
		return Float.parseFloat(_instance.prop.getProperty(confItem.getKey()));
	}

	public static float getFloatProperty(String key) {
		return Float.parseFloat(_instance.prop.getProperty(key));
	}

	/** Singleton getter. */
	public static Conf getInstance() {
		return _instance;
	}

	public static int getIntProperty(ConfItem confItem) {
		return Integer.parseInt(_instance.prop.getProperty(confItem.getKey()));
	}

	public static int getIntProperty(String key) {
		return Integer.parseInt(_instance.prop.getProperty(key));
	}

	public static String getProperty(ConfItem confItem) {
		return _instance.prop.getProperty(confItem.getKey());
	}

	public static String getProperty(String key) {
		return _instance.prop.getProperty(key);
	}

	private final Properties prop;

	private Conf() {
		prop = new Properties();
		try (InputStream in = getClass().getResourceAsStream("/config.properties")) {
			prop.load(in);
		} catch (IOException e) {
			LOGGER.error("Exception raised loading properties", e);
		}
	}
}
