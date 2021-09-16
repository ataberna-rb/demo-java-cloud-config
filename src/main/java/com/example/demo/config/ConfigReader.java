package com.example.demo.config;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.io.FileNotFoundException;
import java.lang.IllegalStateException;
import java.lang.Throwable;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.demo.DemoApplication;

/**
 * @version 1.2.1
 */
public class ConfigReader {

	private static final Logger LOG = LoggerFactory.getLogger(ConfigReader.class);

	private static final String PROPERTIES_FILE_EXTENSION = "yml";
	private static final String PROPERTIES_FILE_NAME = "application."+PROPERTIES_FILE_EXTENSION;
	// private static final String PROPERTIES_FILE_NAME = DemoApplication.PROJECT_NAME + ".yml";
	private static final String REGEX_ENV_VAR = "\\$\\{([^\\}]+)+(:[^\\}]+)*\\}";

	private static final ConfigReader INSTANCE = new ConfigReader();

	private enum TypeProperty {
		FILE, SERVER, ENVIROMENT
	}

	/**
	 * Carga las propiedades del proyecto manejando la siguiente jerarquia:
	 * <code> archivo < server < ambiente </code>
	 */
	public static void loadProperties() {

		Properties systemProps = INSTANCE.getSystemProperties();
		Properties fileProps = INSTANCE.getFileProperties(PROPERTIES_FILE_NAME);

		Map<String, TypeProperty> keysAndTypes = new HashMap<>();

		for (String key : getConfigurationKeys(Vars.class)) {

			String value = fileProps.getProperty(key);
			keysAndTypes.put(key, TypeProperty.FILE);

			if (value == null) {
				String message = String.format("No se encuentra la variable \"%s\" en el archivo \"%s\"", key,
						PROPERTIES_FILE_NAME);
				LOG.info(message);
				value="";
				keysAndTypes.put(key, TypeProperty.SERVER);
			}

			if (systemProps.containsKey(key)) {
				value = systemProps.getProperty(key);
				keysAndTypes.put(key, TypeProperty.SERVER);
			}

			if (haveEnviromentFormat(value)) {
				value = getEnviromentProperty(value);
				keysAndTypes.put(key, TypeProperty.ENVIROMENT);
			}

			ConfigVars.addProperty(key, value);
		}

		LOG.info("ENVIROMENT properties -> " + filterKeys(TypeProperty.ENVIROMENT, keysAndTypes));
		LOG.info("SYSTEM properties -> " + filterKeys(TypeProperty.SERVER, keysAndTypes));
		LOG.info("FILE properties -> " + filterKeys(TypeProperty.FILE, keysAndTypes));
	}

	/**
	 * Devuelve verdadero si la variable incluye algun formato de variable de
	 * ambiente, que es: <code> ${VARIABLE:-default} </code>
	 * 
	 * @param key
	 * @return verdadero o falso
	 */
	public static Boolean haveEnviromentFormat(String value) {

		Pattern pattern = Pattern.compile(REGEX_ENV_VAR);
		return pattern.matcher(value).matches();
	}

	/**
	 * Devuelve el valor de la variable reemplazando la variables de ambiente por su
	 * respectivo valor, en caso de que tenga un default se utiliza este, en caso de
	 * que no lo tenga y la variable de ambiente no se encuentre, se guarda ""
	 * 
	 * @param value
	 * @return el valor de la variable de ambiente
	 */
	public static String getEnviromentProperty(String value) {

		Pattern pattern = Pattern.compile(REGEX_ENV_VAR);

		Matcher matcher = pattern.matcher(value);
		while (matcher.find()) {

			String keyToReplace = matcher.group(0);
			String[] keyAndDefault = keyToReplace.replaceAll("\\$\\{", "").replaceAll("}", "").split(":");

			String envValue = System.getenv(keyAndDefault[0]);
			String envDefault = keyAndDefault.length == 2 ? keyAndDefault[1] : "";

			String finalValue = envValue != null ? envValue : envDefault;
			value = value.replace(keyToReplace, finalValue);
		}

		return value;
	}

	/**
	 * Filtra las claves del mapa por el filtro enviado por parametro
	 * 
	 * @param typeProperty
	 * @param keysAndTypes
	 * @return lista de claves
	 */
	private static List<String> filterKeys(TypeProperty typeProperty, Map<String, TypeProperty> keysAndTypes) {

		return keysAndTypes.keySet().stream().filter(k -> keysAndTypes.get(k).equals(typeProperty))
				.collect(Collectors.toList());
	}

	/**
	 * Devuelve las propiedades cargadas en el sistema, generalmente son las de
	 * Jboss
	 * 
	 * @return objeto con las propiedades
	 */
	private Properties getSystemProperties() {

		return System.getProperties();
	}

	/**
	 * Devuelve las propiedades cargadas del alchivo con nombre enviado por
	 * parametro dentro de la carpeta resources
	 * 
	 * @param fileName
	 * @return objeto con las propiedades
	 */
	private Properties getFileProperties(String fileName) {
		if(PROPERTIES_FILE_EXTENSION=="yml"){
			return getFilePropertiesFromYaml(fileName);
		}
		return getFilePropertiesFromProps(fileName);
	}
	private Properties getFilePropertiesFromProps(String fileName) {

		Properties fileProperties = new Properties();
		try {
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
			fileProperties.load(inputStream);

		} catch (IOException e) {
			LOG.error("Read property error -> ", e);
		}

		return fileProperties;
	}

	private Properties getFilePropertiesFromYaml(String fileName) {
        try {
			Resource samplesResource = new ClassPathResource(fileName);

            YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
            factory.setResources(samplesResource);
            factory.afterPropertiesSet();

            return factory.getObject();
        } catch (RuntimeException ex) {
            LOG.error("",ex);
        }
		return null;
    }

	/**
	 * Devuelve los atriutos estaticos de una clase
	 * 
	 * @param <T>
	 * @param clazz
	 * @return
	 */
	public static <T> List<String> getConfigurationKeys(Class<T> clazz) {

		List<String> keys = new ArrayList<>();

		Field[] fields = clazz.getFields();
		for (Field field : fields) {
			try {
				keys.add(field.get(String.class).toString());

			} catch (IllegalArgumentException | IllegalAccessException e) {
				LOG.error(e.getLocalizedMessage(), e);
			}
		}

		return keys;
	}
}
