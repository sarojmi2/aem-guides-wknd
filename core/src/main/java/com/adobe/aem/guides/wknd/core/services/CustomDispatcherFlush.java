package com.adobe.aem.guides.wknd.core.services;
 import org.apache.sling.api.resource.ResourceResolver;

public interface CustomDispatcherFlush {
    public StringBuilder handleFlushEvent(String cqPath, String excelFilePath ,String cqAction , ResourceResolver rr) ;
}

