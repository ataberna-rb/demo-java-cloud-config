package com.example.demo.config;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version 1.2.0
 */
public class ConfigVars {

	private static final Logger LOG = LoggerFactory.getLogger(ConfigVars.class);

	private static final ConfigVars INSTANCE = new ConfigVars();

	private Map<String, String> propertiesMap;

	private ConfigVars() {
		this.propertiesMap = new HashMap<>();
	}

	/**
	 * Devuelve el mapa con las variables cargadas al inicio del proyecto guardadas
	 * por el metodo "loadProperties" de {@link ConfigUtils}
	 * 
	 * @return mapa con las variables del proyecto
	 */
	public static Map<String, String> getPropertiesMap() {
		return INSTANCE.propertiesMap;
	}

	/**
	 * Agrega una variable al mapa de variables del proyecto, en caso de que exista
	 * reemplaza el valor anterior
	 * 
	 * @param key
	 * @param value
	 */
	public static void addProperty(String key, String value) {
		INSTANCE.propertiesMap.put(key, value);
	}

	/**
	 * Devuelve la el valor de la variable configurada al inicio del proyecto,
	 * convertida en la clase enviada por parametro. <b>IMPORTANTE</b> se utilizan
	 * el contructor que recive un String para generar la instancia de la clase
	 * 
	 * @param <T>
	 * @param key
	 * @param typeProperty
	 * @return valor de la variable del tipo requerido
	 */
	public static <T> T get(String key, Class<T> typeProperty) {

		String value = get(key);
		try {
			if (typeProperty.isEnum()) {
				return getEnum(key, typeProperty);
			}
			return typeProperty.getConstructor(String.class).newInstance(value);

		} catch (Exception e) {
			String message = "Error en carga de variable -> KEY: %s; CLASE: %s";
			LOG.error(String.format(message, key, typeProperty.getSimpleName()), e);
			return null;
		}
	}

	/**
	 * Devuelve la el valor de la variable configurada al inicio del proyecto en
	 * formato de enum
	 * 
	 * @param <T>
	 * @param <E>
	 * @param key
	 * @param enumClass
	 * @return enum correspondiente
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static <T, E extends Enum> T getEnum(String key, Class<T> enumClass) {

		String value = get(key);
		return (T) Enum.valueOf((Class<E>) enumClass, value.toUpperCase());
	}

	/**
	 * Devuelve la el valor de la variable configurada al inicio del proyecto
	 * 
	 * @param key
	 * @return valor de la variable
	 */
	public static String get(String key) {
		return INSTANCE.propertiesMap.get(key);
	}
}