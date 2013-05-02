package com.sanxing.sesame.jdbc.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;

public class DAOGenerator extends CodeGenerator {
	private static final Logger LOG = LoggerFactory.getLogger(DAOGenerator.class);

	public Template getTemplate() {
		try {
			Template template = Velocity.getTemplate(
					"com/sanxing/sesame.jdbc/tools/DAO.vm", "UTF-8");
			return template;
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return null;
	}
}