/* package com.adobe.aem.guides.wknd.core.mcp;

 
import org.osgi.service.component.annotations.Component;
import com.adobe.acs.commons.mcp.ProcessDefinitionFactory;
 
@Component(service = ProcessDefinitionFactory.class, immediate = true)
public class PropertyUpdateFactory extends ProcessDefinitionFactory<PropertyUpdater> {
    @Override
    public String getName() {
        return "Property Updator";
    }
    @Override
    protected PropertyUpdater createProcessDefinitionInstance() {
        return new PropertyUpdater();
    }
}
 */