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

@Extension public final class GlobalConfig extends GlobalConfiguration  {
    protected static final Logger LOGGER = Logger.getLogger(GlobalConfig.class.getName());

    private final CopyOnWriteList<Server> servers = new CopyOnWriteList<Server>();
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

    public Server[] getServers() {
        Iterator<Server> it = servers.iterator();
        int size = 0;
        while (it.hasNext()) {
            it.next();
            size++;
        }
        return servers.toArray(new Server[size]);
    }

    @Override
    public String getDisplayName() {
        LOGGER.log(Level.INFO, "GlobalConfig Showing display name");
        return "Publish metrics to Graphite Server";
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) {
        servers.replaceBy(req.bindParametersToList(Server.class, "serverBinding."));
        baseQueueName = formData.optString("baseQueueName", "");
        save();
        return true;
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


    public void setBaseQueueName(String baseQueueName) {
        this.baseQueueName = baseQueueName;
    }


    public FormValidation doTestConnection(@QueryParameter("serverBinding.ip") final String ip,
        @QueryParameter("serverBinding.port") final String port,
        @QueryParameter("serverBinding.protocol") final String protocol) {
        if(protocol.equals("UDP")) {
            return FormValidation.ok("UDP is configured");
        }
        else if(protocol.equals("TCP")) {
            if (!validator.isIpPresent(ip) || !validator.isPortPresent(port)
                    || !validator.isListening(ip, Integer.parseInt(port))) {
                return FormValidation.error("Server is not listening... Or ip:port are not correctly filled");
            }

            return FormValidation.ok("Server is listening");
        } else {
            return FormValidation.ok("Unknown protocol");
        }
    }

    public FormValidation doCheckIp(@QueryParameter final String value) {
        if (!validator.isIpPresent(value)) {
            return FormValidation.error("Please set a ip");
        }
        if (!validator.validateIpFormat(value)) {
            return FormValidation.error("Please check the IP format");
        }

        return FormValidation.ok("IP is correctly configured");
    }

    public FormValidation doCheckID(@QueryParameter final String value) {
        if (!validator.isIDPresent(value)) {
            return FormValidation.error("Please set an ID");
        }
        int length = 50;
        if (validator.isIDTooLong(value, length)) {
            return FormValidation.error(String.format("ID is limited to %d characters", length));
        }

        return FormValidation.ok("ID is correctly configured");
    }

    public FormValidation doCheckPort(@QueryParameter final String value) {
        if (!validator.isPortPresent(value)) {
            return FormValidation.error("Please set a port");
        }

        if (!validator.validatePortFormat(value)) {
            return FormValidation.error("Please check the port format");
        }

        return FormValidation.ok("Port is correctly configured");
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
