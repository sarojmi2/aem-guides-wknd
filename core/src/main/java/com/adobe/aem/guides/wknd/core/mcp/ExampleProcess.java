/* package com.adobe.aem.guides.wknd.core.mcp;
 
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import javax.jcr.RepositoryException;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.ResourceResolver;
import com.adobe.acs.commons.fam.ActionManager;
import com.adobe.acs.commons.mcp.ProcessDefinition;
import com.adobe.acs.commons.mcp.ProcessInstance;
import com.adobe.acs.commons.mcp.form.FormField;
import com.adobe.acs.commons.mcp.model.GenericReport;
 
public class ExampleProcess extends ProcessDefinition {
     
    private static final String REPORT_SAVE_PATH = "/jcr:content/report";
    private final List<EnumMap<ReportColumns, Object>> reportRows = new ArrayList<>();
    GenericReport genericReport = new GenericReport();
     
    @FormField(name = "Enter Text",
            description = "Example text to be reported",
            required = false,
            options = {"default=enter test text"})
    private String emapleText;
     
    @Override
    public void buildProcess(ProcessInstance instance, ResourceResolver rr) throws RepositoryException, LoginException {        
        genericReport.setName("exampleReport");
        instance.defineCriticalAction("Running Example", rr, this::reportExampleAction);
        instance.getInfo().setDescription("Executing Example Process");
    }
     
    protected void reportExampleAction(ActionManager manager) {
        record(emapleText);
    }
     
    private void record(String emapleText) {
         final EnumMap<ReportColumns, Object> row = new EnumMap<>(ReportColumns.class);
         row.put(ReportColumns.SERIAL, 1);
            row.put(ReportColumns.TEXT, emapleText);
            reportRows.add(row);
    }
 
    @Override
    public void storeReport(ProcessInstance instance, ResourceResolver rr) throws RepositoryException, PersistenceException {
        genericReport.setRows(reportRows, ReportColumns.class);
        genericReport.persist(rr, instance.getPath() + REPORT_SAVE_PATH);
    }
 
    @Override
    public void init() throws RepositoryException {
    }
     
    public enum ReportColumns {
        SERIAL, TEXT
    }
} */