package com.adobe.aem.guides.wknd.core.services;

public interface DisparcherFlushConfiguration {
    
    /**
	 * Method to get Dispatcher Urls from Configuration File.
	 *
	 * @return Dispatcher Urls 
	 */
	String[] getDispatcherUrls();

	/**
	 * Method to get Header params from Configuration File.
	 *
	 * @return Header params 
	 */
	String[] getHeaderParams();

    	/**
	 * Method to get Flush url from Configuration File.
	 *
	 * @return flush url 
	 */
	String getFlushUrl();

}
