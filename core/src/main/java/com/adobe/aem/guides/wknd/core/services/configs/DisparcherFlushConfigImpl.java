package com.adobe.aem.guides.wknd.core.services.configs;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import com.adobe.aem.guides.wknd.core.services.DisparcherFlushConfiguration;
 
@Component(service = DisparcherFlushConfiguration.class, immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = DisparcherFlushConfigImpl.Config.class)
public class DisparcherFlushConfigImpl implements DisparcherFlushConfiguration{

    private String[] dispatcherUrls;
	private String[] headerParams;
    private String flushUrl;

  
@ObjectClassDefinition(name = "Custom Dispatcher flush Configuration", description = "flush Configuration")
    public @interface Config {
    
        
		@AttributeDefinition(name = "Dispatcher URL with host:port")
		String[] dispatcherUrls() default "localhost:8080";

        @AttributeDefinition(name = "Header Params")
		String[] headerParams();
        
        @AttributeDefinition(name = "Flush Url")
		String flushUrl() default "/dispatcher/invalidate.cache";
           
    }
    @Activate
	@Modified
	public void init(Config config) {
    
        this.dispatcherUrls = config.dispatcherUrls();
        this.headerParams = config.headerParams();
        this.flushUrl = config.flushUrl();
    }
    @Override
    public String[] getDispatcherUrls() {
        return this.dispatcherUrls;
    }
    @Override
    public String[] getHeaderParams() {
       return this.headerParams;
    }

    @Override
    public String getFlushUrl() {
       return this.flushUrl;
    }
   
}




