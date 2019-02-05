package org.jenkinsci.plugins.another.graphite;

import hudson.model.AbstractProject;
import hudson.model.ModelObject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.util.CopyOnWriteList;
import hudson.util.CopyOnWriteMap;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import utils.GraphiteValidator;

import java.util.Iterator;

import jenkins.model.GlobalConfiguration;

import hudson.Extension;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

import java.util.List;
import java.util.ArrayList;

@Extension public final class GlobalConfig extends GlobalConfiguration  {
    protected static final Logger LOGGER = Logger.getLogger(GlobalConfig.class.getName());

    private List<Server> servers = new ArrayList<Server>();
    private GraphiteValidator validator = new GraphiteValidator();
    private String baseQueueName;


    public static @Nonnull GlobalConfig get() {
        GlobalConfig instance = GlobalConfiguration.all().get(GlobalConfig.class);
        if (instance == null) { // TODO would be useful to have an ExtensionList.getOrFail
            throw new IllegalStateException();
        }
        return instance;
    }

    public GlobalConfig() {
        LOGGER.log(Level.INFO, "DescriptorImmp constructed");
        load();
    }

    public GlobalConfig(List<Server> servers, String baseQueueName){
        this.servers = servers;
        this.baseQueueName = baseQueueName;
    }

    public void setServers(List<Server> servers){
        LOGGER.log(Level.INFO, "Set servers called");
        this.servers = servers;
    }

    public List<Server> getServers(){
        return this.servers;
    }

    //public Server[] getServers() {
    //    Iterator<Server> it = servers.iterator();
    //    int size = 0;
    //    while (it.hasNext()) {
    //        it.next();
    //        size++;
    //    }
    //    return servers.toArray(new Server[size]);
    //}

    @Override
    public String getDisplayName() {
        LOGGER.log(Level.INFO, "GlobalConfig Showing display name");
        return "Publish metrics to Graphite Server";
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {

        LOGGER.log(Level.INFO, String.format("Graphite Server form data: %s", formData.toString()));
        try {
            req.bindJSON(this, formData);
        } catch (Exception e) {
            LOGGER.log(Level.INFO, String.format("Problem while submitting form for Graphite Plugin (%s). (%s)", e.getMessage(), e));
            LOGGER.log(Level.INFO, String.format("Graphite Server form data: %s", formData.toString()));
            throw new FormException(
                    String.format("Malformed Graphite Server Plugin configuration (%s)", e.getMessage()), e, "graphite-global-configuration");
        }
        save();
        LOGGER.log(Level.INFO, String.format("Servers: %s", this.servers));
        return true;

        //servers.replaceBy(req.bindParametersToList(Server.class, "serverBinding."));
        //baseQueueName = formData.optString("baseQueueName", "");
        //save();
        //return true;
    }


    public void setBaseQueueName(String baseQueueName){
        this.baseQueueName = baseQueueName;
    }


    public String getBaseQueueName(){
        return baseQueueName;
    }


    public GraphiteValidator getValidator() {
        return validator;
    }

   @Override
    public String getGlobalConfigPage() {
        LOGGER.log(Level.INFO, "returned config page {0}", getConfigPage());
        return getConfigPage();
    }

    public void setValidator(GraphiteValidator validator) {
        this.validator = validator;
    }

    public FormValidation doCheckBaseQueueName(@QueryParameter final String value) {
        if(!validator.isBaseQueueNamePresent(value)){
            return FormValidation.ok();
        }
        
        if(!validator.validateBaseQueueName(value)){
            return FormValidation.error("Please ");
        }
        
        return FormValidation.ok("Base queue name is correctly Configured");
    }

}
