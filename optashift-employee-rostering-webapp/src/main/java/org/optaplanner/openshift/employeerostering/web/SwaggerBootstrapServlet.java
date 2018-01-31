package org.optaplanner.openshift.employeerostering.web;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import io.swagger.jaxrs.config.BeanConfig;

public class SwaggerBootstrapServlet extends HttpServlet {

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		BeanConfig beanConfig = new BeanConfig();
		//beanConfig.setVersion("1.0.2");
		//beanConfig.setSchemes(new String[]{"http"});
		//beanConfig.setHost("localhost:8080");
		beanConfig.setBasePath("/rest");
		beanConfig.setResourcePackage("org.optaplanner.openshift.employeerostering");
		beanConfig.setScan(true);
	}

}
