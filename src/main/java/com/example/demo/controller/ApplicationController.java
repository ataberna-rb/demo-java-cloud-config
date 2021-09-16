package com.example.demo.controller;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.demo.config.ConfigVars;
import com.example.demo.config.ConfigReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class ApplicationController {
	
	private static final Logger LOG = LoggerFactory.getLogger(ApplicationController.class);

	@Autowired
	private Environment env;

	public final static String URL_API = "/api";

	@GetMapping("/alive")
	public ResponseEntity<Object> status() {
		return new ResponseEntity<Object>("ALIVE!!!", HttpStatus.OK);
	}

	@RequestMapping(path = "/properties", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Object> getProperties() {
		Map<String, String> properties = ConfigVars.getPropertiesMap();
		return new ResponseEntity<>(properties, HttpStatus.OK);
	}
	@RequestMapping(path = "/properties/{key}", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Object> getProperties(@PathVariable String key) {
		LOG.info("Buscando "+key);
		return new ResponseEntity<>(env.getProperty(key), HttpStatus.OK);
	}

	@RequestMapping(path = "/properties/refresh", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Object> reloadProperties() {
		ConfigReader.loadProperties();
		return new ResponseEntity<>(getProperties(), HttpStatus.OK);
	}

}
