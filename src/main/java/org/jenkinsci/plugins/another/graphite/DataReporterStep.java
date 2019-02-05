package org.jenkinsci.plugins.another.graphite;

import com.google.common.collect.ImmutableSet;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.SynchronousStepExecution;
import hudson.Extension;
import java.util.List;
import java.util.Set;
import org.kohsuke.stapler.DataBoundConstructor;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.TaskListener;

import hudson.model.Run;
import jenkins.model.GlobalConfiguration;

import org.jenkinsci.plugins.another.graphite.metrics.GraphiteMetric;

public class DataReporterStep extends Step {

    private List<String> servers;
    String dataQueue;
    String data;

    @DataBoundConstructor public DataReporterStep(@NonNull List <String> servers,
                                                  @NonNull String dataQueue,
                                                  @NonNull String data) {
        this.servers = servers;
        this.dataQueue = dataQueue;
        this.data = data;
    }

    @Override public StepExecution start(StepContext context) throws Exception {
        return new Execution(this.servers, this.dataQueue, this.data, context);
    }

    public List<String> getServers() {
        return servers;
    }

    @Extension public static final class StepDescriptorImpl extends StepDescriptor {

        @Override public String getFunctionName() {
            return "graphiteData";
        }

        @Override public String getDisplayName() {
            return "Report single data to graphite server";
        }

        @Override public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(Run.class, TaskListener.class);
        }

    }


    public static class Execution extends SynchronousStepExecution<Void> {
        
        @SuppressFBWarnings(value="SE_TRANSIENT_FIELD_NOT_RESTORED", justification="Only used when starting.")
        private transient final List<String> serverIds;
        private transient final String dataQueue;
        private transient final String data;

        Execution(List<String> serverIds, String dataQueue, String data, StepContext context) {
            super(context);
            this.serverIds = serverIds;
            this.dataQueue = dataQueue;
            this.data = data;
        }

        @Override protected Void run() throws Exception {
            TaskListener listener = getContext().get(TaskListener.class);
            Run run = getContext().get(Run.class);

            String baseQueueName = this.getBaseQueueName();

            String queueName = baseQueueName.concat(".").concat(this.dataQueue);
            GraphiteMetric.Snapshot snapshot = new GraphiteMetric.Snapshot(queueName,
                                                                           this.data);

            for(String serverId : this.serverIds){
                listener.getLogger().println(serverId);
                Server server = this.getServerById(serverId);
                server.send(snapshot, listener.getLogger());
            }

            return null;
        }


        @NonNull public Server getServerById(@NonNull String serverId) {
            GlobalConfig globalConfig = GlobalConfiguration.all().get(GlobalConfig.class);

            List<Server> servers = globalConfig.getServers();
            for (Server server : servers) {
                if (server.getId().equals(serverId)) {
                    return server;
                }
            }
            return null;
        }

        @NonNull public String getBaseQueueName() {
            GlobalConfig globalConfig = GlobalConfiguration.all().get(GlobalConfig.class);
            return globalConfig.getBaseQueueName();
        }


    }

}
