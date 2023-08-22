package com.adobe.aem.guides.wknd.core.services.impl;


import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
// import org.apache.http.HttpEntity;
// import org.apache.http.HttpResponse;
// import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.aem.guides.wknd.core.services.CustomDispatcherFlush;
import com.adobe.aem.guides.wknd.core.services.DisparcherFlushConfiguration;
import com.adobe.aem.guides.wknd.core.services.configs.DisparcherFlushConfigImpl;


@Component(service = CustomDispatcherFlush.class,immediate=true)
//@Designate(ocd = DisparcherFlushConfigImpl.class)
@ServiceDescription("Dispatcher flush service")
public class CustomDispatcherFlushImpl implements CustomDispatcherFlush {

     /**
	 * Logger Object
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomDispatcherFlushImpl.class);

    // Config paramenters
    private String[] disparcherUrls;
    private String[] headerParams;
    private String flushPath;
   
    StringBuilder response = new StringBuilder();
    String msg = "";

    // Constants

	private static final String HTTP = "http://";
    private static final String DISPATCHER_INVALIDATE_CACHE = "/dispatcher/invalidate.cache";
    private static final String CQ_ACTION_SCOPE = "CQ-Action-Scope";
    private static final String CQ_PATH = "CQ-Path";
    private static final String CQ_HANDLE = "CQ-Handle";
    private static final String CQ_ACTION = "CQ-Action";
    private static final String DISP_ACTION_ACT = "ACTIVATE";
    
   @Reference
	private DisparcherFlushConfiguration config;
/*     @Activate
    public void activate(DisparcherFlushConfigImpl config) throws Exception{
        disparcherUrls = config.getDispatcherUrls();
        headerParams = config.getHeaderParams();
        flushPath = config.getFlushUrl();
        msg = String.format("Osgi config String for disparcherUrls = {}, headerParams = {}, flushPath = {} ",disparcherUrls , headerParams , flushPath);
        response.append(msg);
        LOGGER.info(msg);
        
    } */

    public StringBuilder handleFlushEvent(String cqPath,String excelFilePath ,String cqAction){
        disparcherUrls = config.getDispatcherUrls();
        headerParams = config.getHeaderParams();
        flushPath = config.getFlushUrl();
        msg = String.format("Osgi config String for disparcherUrls = %s, headerParams = %s, flushPath = %s ",disparcherUrls , headerParams , flushPath);
        response.append(msg);
        LOGGER.info("Osgi config String for disparcherUrls = {}, headerParams = {}, flushPath = {} ",disparcherUrls , headerParams , flushPath);
        msg = String.format("Input Param strings for cqPath =  %s, excelFilePath =  %s, cqAction =  %s ",cqPath , excelFilePath , cqAction);
        response.append(msg);
        LOGGER.info("Input Param strings for cqPath = {}, excelFilePath = {}, cqAction = {} ",cqPath , excelFilePath , cqAction);
        //invalidatePagePost(Arrays.toString(servers), Arrays.toString(paths));
         return response;

    }
    private void invalidatePageIPV4(String host, String path){
        String uri = HTTP + host + DISPATCHER_INVALIDATE_CACHE;
        
         HttpClient client = new HttpClient();
        GetMethod get = new GetMethod(uri);
        
        // get.setRequestHeader(CQ_ACTION, dispatcherAction);
        // get.setRequestHeader(CQ_HANDLE, path);
        // get.setRequestHeader(CQ_PATH, path);
        // get.setRequestHeader(CQ_ACTION_SCOPE, actionScope);
        try {
            client.executeMethod(get);
            LOGGER.info("Invalidation Result : " + get.getResponseBodyAsString());
        }
        catch (HttpException e) {
            LOGGER.error(e.toString());
        }
        catch (IOException ie) {
            LOGGER.error(ie.toString());
        }finally{
            try{
                get.releaseConnection();
            }catch(Exception e){
                LOGGER.error(e.toString());
            }
        }
    
        }
    private void invalidatePagePost(String host, String path) {
        try {
           
            //hard-coding connection properties is a bad practice, but is done here to simplify the example
             host = "localhost";
            
            HttpPost httpPost = new HttpPost("https://"+ host+DISPATCHER_INVALIDATE_CACHE);
            httpPost.setHeader(CQ_ACTION, DISP_ACTION_ACT);
            httpPost.setHeader(CQ_HANDLE,path);
           
            String requestBody = "{\"path\": path}";
            
            StringEntity requestEntity = new StringEntity(requestBody);
            httpPost.setEntity(requestEntity);

        
          /*   HttpClient httpClient = HttpClientBuilder.create().build();
            HttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity responseEntity = response.getEntity();
            //log the results
            LOGGER.info("result: {} status code {} " , responseEntity.getContent(), statusCode);
             */
        } 
        catch (Exception e){
            LOGGER.error("Flush cache servlet exception: {}" , e.getMessage());
        }
    }

    private void invalidatePage(String host, String path){
        
        //dispatcherAction = "Activate";
        /*
         * POST /dispatcher/invalidate.cache HTTP/1.1
            CQ-Action: Activate
            Content-Type: text/plain
            CQ-Handle: /content/geometrixx-outdoors/en/men.html
            Content-Length: 36
            /content/geometrixx-outdoors/en.html
            10.36.79.223:4503/bin/flushcache/html?page=/content/geometrixx-outdoors/en.html&handle=/content/geometrixx-outdoors/en/men.html
         */
        String uri = HTTP + host + DISPATCHER_INVALIDATE_CACHE;
        
       // LOGGER.info("Invalidation Page on :  host {}  for path {} with Action :{} and scope {}" , host, path ,  dispatcherAction , actionScope);
        /* try{
             HttpGet httpGet = new HttpGet(uri);
            httpGet.setHeader(CQ_ACTION, DISP_ACTION_ACT);
            httpGet.setHeader(CQ_HANDLE,path);
           
            httpGet.setHeader(CQ_PATH, path);
            httpGet.setHeader(CQ_ACTION_SCOPE, actionScope);
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpResponse response = httpClient.execute(httpGet);
           
            LOGGER.info("Invalidation Result : {}" , response.getEntity().getContent());
         
        } catch (IOException io) {
            LOGGER.error("http get   exception: {}" , io.getMessage());
        } */
    
        }


    }
