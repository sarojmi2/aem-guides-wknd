package com.adobe.aem.guides.wknd.core.services.impl;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import com.adobe.aem.guides.wknd.core.services.CustomDispatcherFlush;
import com.adobe.aem.guides.wknd.core.services.DisparcherFlushConfiguration;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.AssetManager;
import com.day.cq.dam.api.Rendition;


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

   @Reference
    private ResourceResolverFactory resourceResolverFactory;

/*     @Activate
    public void activate(DisparcherFlushConfigImpl config) throws Exception{
        disparcherUrls = config.getDispatcherUrls();
        headerParams = config.getHeaderParams();
        flushPath = config.getFlushUrl();
        msg = String.format("Osgi config String for disparcherUrls = {}, headerParams = {}, flushPath = {} ",disparcherUrls , headerParams , flushPath);
        response.append(msg);
        LOGGER.info(msg);
        
    } */

    public StringBuilder handleFlushEvent(String cqPath,String excelFilePath ,String cqAction, ResourceResolver rr){
        disparcherUrls = config.getDispatcherUrls();
        headerParams = config.getHeaderParams();
        flushPath = config.getFlushUrl();
        List<String> urlList = new ArrayList<>();
        msg = String.format("Osgi config String for disparcherUrls = %s, headerParams = %s, flushPath = %s ",disparcherUrls , headerParams , flushPath);
        response.append(msg).append("\n");
        LOGGER.info("Osgi config String for disparcherUrls = {}, headerParams = {}, flushPath = {} ",disparcherUrls , headerParams , flushPath);
        msg = String.format("Input Param strings for cqPath =  %s, excelFilePath =  %s, cqAction =  %s ",cqPath , excelFilePath , cqAction);
        response.append(msg) .append("\n");
        LOGGER.info("Input Param strings for cqPath = {}, excelFilePath = {}, cqAction = {} ",cqPath , excelFilePath , cqAction);

       try {
        urlList = getExcelUrls(excelFilePath, rr);
        } catch (IOException e) {
            response.append("Error occur while reading the excel from dam ").append("\n");
        }
        //List<String> stringList = new ArrayList<>();
        msg = String.format("Url list =  %s, ", urlList);
         response.append(msg) .append("\n");
        // For each Dispather url 
        for(int i=0 ;i<disparcherUrls.length;++i ){
            invalidatePagePost(disparcherUrls[i], urlList, cqPath , cqAction );
        }
        

         return response;

     }
    private List<String> getExcelUrls(String excelFilePath, ResourceResolver rr) throws IOException {
        List<String> urlList = new ArrayList<>(); 

         msg = String.format("getting urls to flush from path =  %s, ", excelFilePath);
         response.append(msg) .append("\n");
        /* Resource original = Objects.requireNonNull( Objects.requireNonNull(rr.getResource(excelFilePath)).adaptTo(Asset.class)) .getOriginal();
        InputStream inputStream = Objects.requireNonNull(original.getChild(JcrConstants.JCR_CONTENT))
                .adaptTo(InputStream.class); */
        //AssetManager assetManager = rr.adaptTo(AssetManager.class);
         Resource res = rr.getResource(excelFilePath);
        Asset asset = res.adaptTo(Asset.class);
        Rendition rendition = asset.getOriginal();
       // InputStream inputStream = rendition.adaptTo(InputStream.class);

        BufferedReader br = new BufferedReader(new InputStreamReader(rendition.getStream()));
        String url;
        while (( url = br.readLine()) != null) {
            urlList.add(url);
        }
       /*  urlList = new ArrayList<>();        
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        XSSFSheet sheet = workbook.getSheetAt(0);
        Iterator < Row > rowIterator = sheet.iterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Iterator < Cell > cellIterator = row.cellIterator();
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                urlList.add(cell.getStringCellValue());
            }
        } 
        urlList.add("/content/en/us/abc.html"); 
        urlList.add("/content/en/us/abcd.html");*/
        return urlList;
    }
     
            
             
    private void invalidatePagePost(String host, List<String> urlList, String path, String action) {
        
         msg = String.format("Dispatcher post call to  =  %s, ", host);
         response.append(msg) .append("\n");
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost("https://"+ host+DISPATCHER_INVALIDATE_CACHE);
            httpPost.setHeader(CQ_ACTION, DISP_ACTION_ACT);
            httpPost.setHeader(CQ_HANDLE,path);

            httpPost.setEntity(new StringEntity(urlList.get(0)));

            CloseableHttpResponse httpResponse = httpclient.execute(httpPost);

            String result = IOUtils.toString(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8);
            msg = String.format("Dispatcher post response  %s, ", result);

             response.append(msg) .append("\n");
        } catch (IOException e) {
            msg = String.format("Dispatcher post response  %s, ", e.getMessage());
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
