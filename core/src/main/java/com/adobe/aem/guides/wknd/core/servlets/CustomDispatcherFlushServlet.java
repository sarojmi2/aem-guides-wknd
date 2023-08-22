package com.adobe.aem.guides.wknd.core.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.aem.guides.wknd.core.services.CustomDispatcherFlush;


    @Component(service = Servlet.class, property = {
		Constants.SERVICE_DESCRIPTION + "=POC Dispatcher flush Servlet .",
		"sling.servlet.methods=" + HttpConstants.METHOD_GET, "sling.servlet.paths=" + "/bin/wknd/dispflush",
		"sling.servlet.extensions=" + "json" }, immediate = true)
    
        
public class CustomDispatcherFlushServlet extends SlingSafeMethodsServlet {

    /**
	 * Logger Object
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomDispatcherFlushServlet.class);

     @Reference
     CustomDispatcherFlush customDispatcherFlush;

        @Override   
       protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException
      {
            LOGGER.debug("Custom Dispatcher servlet --1-- do get called.");
            // Get Request 1. flush path 2. refetch urls in Excel path 3.action
            String cqPath = request.getParameter("path");
            String excelFilePath = request.getParameter("excelPath");
            String cqAction = request.getParameter("cqAction");
            StringBuilder res =  flushDispatcher(cqPath, excelFilePath , cqAction);
            PrintWriter out = response.getWriter();
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            out.print(res);
            out.flush();
            
      }
        public StringBuilder flushDispatcher(String cqPath,String excelFilePath ,String cqAction)
        {
             LOGGER.debug("Flush Dispatcher method --2-- called.");
            StringBuilder path = customDispatcherFlush.handleFlushEvent(cqPath, excelFilePath , cqAction);
            String msg = "Custom Dispatcher config path is {}.";
            LOGGER.debug( msg, path);
            return path;
        }
}

