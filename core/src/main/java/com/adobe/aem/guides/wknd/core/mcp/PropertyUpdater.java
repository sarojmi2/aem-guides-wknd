/* package com.adobe.aem.guides.wknd.core.mcp;

 
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.jcr.RepositoryException;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.resource.filter.ResourceFilterStream;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adobe.acs.commons.data.Spreadsheet;
import com.adobe.acs.commons.fam.ActionManager;
import com.adobe.acs.commons.mcp.ProcessDefinition;
import com.adobe.acs.commons.mcp.ProcessInstance;
import com.adobe.acs.commons.mcp.form.FileUploadComponent;
import com.adobe.acs.commons.mcp.form.FormField;
import com.adobe.acs.commons.mcp.form.SelectComponent;
import com.adobe.acs.commons.mcp.form.TextfieldComponent;
import com.adobe.acs.commons.mcp.model.GenericReport;
import com.day.cq.commons.jcr.JcrConstants;
 
public class PropertyUpdater extends ProcessDefinition {
 
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyUpdater.class);
    private final GenericReport report = new GenericReport();
    private static final String REPORT_NAME = "Property-update-report";
    private static final String RUNNING = "Running ";
    private static final String EXECUTING_KEYWORD = " Property Updation";
 
    private static final String PROPERTY = "property";
    private static final String PROPERTY_TYPE = "type";
    private static final String PROPERTY_VALUE = "value";
 
    protected enum UpdateAction {
        ADD, UPDATE, DELETE
    }
 
    @FormField(name = "Property Update Excel", component = FileUploadComponent.class)
    private RequestParameter sourceFile;
 
    @FormField(name = "Path",
            component = TextfieldComponent.class,
            hint = "(Provide the Relative path to start search)",
            description = "A query will be executed starting this path")
    private String path;
 
    @FormField(name = "Action",
            component = SelectComponent.EnumerationSelector.class,
            description = "Add, Update or Delete?",
            options = "default=Add")
    UpdateAction reAction = UpdateAction.ADD;
 
    @FormField(name = "ResourceType",
            component = TextfieldComponent.class,
            hint = "(Provide the resourcetype to be searched for)",
            description = "A query will be executed based on resourcetype")
    private String pageResourceType;
 
    private Map<String, Object> propertyMap = new HashMap<>();
 
    @Override
    public void init() throws RepositoryException {
        validateInputs();
    }
 
    @Override
    public void buildProcess(ProcessInstance instance, ResourceResolver rr) throws LoginException, RepositoryException {
        report.setName(REPORT_NAME);
        instance.getInfo().setDescription(RUNNING + reAction + EXECUTING_KEYWORD);
        instance.defineCriticalAction("Adding the Properties", rr, this::updateProperties);
    }
 
    private void updateProperties(ActionManager manager) {
        manager.deferredWithResolver(this::addProperties);
    }
 
    private void addProperties(ResourceResolver resourceResolver) {
        @NotNull Resource resource = resourceResolver.resolve(path);
        if(!ResourceUtil.isNonExistingResource(resource)) {
            ResourceFilterStream rfs = resource.adaptTo(ResourceFilterStream.class);
            rfs.setBranchSelector("[jcr:primaryType] == 'cq:Page'")
                    .setChildSelector("[jcr:content/sling:resourceType] == $type")
                    .addParam("type", pageResourceType)
                    .stream()
                    .map(r -> r.getChild(JcrConstants.JCR_CONTENT))
                    .forEach(this::updateProperty);
        }
    }
 
    private void updateProperty(Resource resultResource) {
        if (reAction == UpdateAction.ADD || reAction == UpdateAction.UPDATE) {
            addOrUpdateProp(resultResource);
        } else if (reAction == UpdateAction.DELETE) {
            removeProp(resultResource);
        }
    }
 
    private void removeProp(Resource resultResource) {
        try {
            ModifiableValueMap map = resultResource.adaptTo(ModifiableValueMap.class);
            propertyMap.entrySet().stream().forEach(r -> map.remove(r.getKey()));
            resultResource.getResourceResolver().commit();
            recordAction(resultResource.getPath(), reAction.name(), StringUtils.join(propertyMap));
        } catch (PersistenceException e) {
            LOGGER.error("Error occurred while persisting the property {}", e.getMessage());
        }
    }
 
    private void addOrUpdateProp(Resource resultResource) {
        try {
            ModifiableValueMap map = resultResource.adaptTo(ModifiableValueMap.class);
            propertyMap.entrySet().stream().forEach(r -> map.put(r.getKey(), r.getValue()));
            resultResource.getResourceResolver().commit();
            recordAction(resultResource.getPath(), reAction.name(), StringUtils.join(propertyMap));
        } catch (PersistenceException e) {
            LOGGER.error("Error occurred while persisting the property {}", e.getMessage());
        }
    }
 
    public enum ReportColumns {
        PATH, ACTION, DESCRIPTION
    }
 
    private void validateInputs() throws RepositoryException {
        if (sourceFile != null && sourceFile.getSize() > 0) {
            Spreadsheet sheet;
            try {
                sheet = new Spreadsheet(sourceFile, PROPERTY, PROPERTY_TYPE, PROPERTY_VALUE).buildSpreadsheet();
            } catch (IOException ex) {
                throw new RepositoryException("Unable to parse spreadsheet", ex);
            }
 
            if (!sheet.getHeaderRow().contains(PROPERTY) || !sheet.getHeaderRow().contains(PROPERTY_TYPE) || !sheet.getHeaderRow().contains(PROPERTY_VALUE)) {
                throw new RepositoryException(MessageFormat.format("Spreadsheet should have two columns, respectively named {0}, {1} and {2}", PROPERTY, PROPERTY_TYPE, PROPERTY_VALUE));
            }
 
            sheet.getDataRowsAsCompositeVariants().forEach(row -> {
                String propertyType = row.get(PROPERTY_TYPE).toString();
                if(StringUtils.equalsAnyIgnoreCase("String", propertyType)) {
                    propertyMap.put(row.get(PROPERTY).toString(), row.get(PROPERTY_VALUE).toString());
                } else if(StringUtils.equalsAnyIgnoreCase("Date", propertyType)) {
                    SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                    propertyMap.put(row.get(PROPERTY).toString(), dt.format(row.get(PROPERTY_VALUE).toString()));
                } else if(StringUtils.equalsAnyIgnoreCase("Array", propertyType)) {
                    propertyMap.put(row.get(PROPERTY).toString(), row.get(PROPERTY_VALUE).toString().split(","));
                } else if(StringUtils.equalsAnyIgnoreCase("Long", propertyType)) {
                    Integer result = Optional.ofNullable(row.get(PROPERTY_VALUE).toString())
                            .filter(Objects::nonNull)
                            .map(Integer::parseInt)
                            .orElse(0);
                    propertyMap.put(row.get(PROPERTY).toString(), result);
                } else if(StringUtils.equalsAnyIgnoreCase("Binary", propertyType)) {
                    propertyMap.put(row.get(PROPERTY).toString(), Boolean.valueOf(row.get(PROPERTY_VALUE).toString()));
                }
            });
        }
    }
 
    List<EnumMap<ReportColumns, String>> reportData = Collections.synchronizedList(new ArrayList<>());
 
    private void recordAction(String path, String action, String description) {
        EnumMap<ReportColumns, String> row = new EnumMap<>(ReportColumns.class);
        row.put(ReportColumns.PATH, path);
        row.put(ReportColumns.ACTION, action);
        row.put(ReportColumns.DESCRIPTION, description);
        reportData.add(row);
    }
 
    @Override
    public void storeReport(ProcessInstance instance, ResourceResolver rr) throws RepositoryException, PersistenceException {
        report.setRows(reportData, ReportColumns.class);
        report.persist(rr, instance.getPath() + "/jcr:content/report");
    }
}
 */