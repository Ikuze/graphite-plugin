package org.jenkinsci.plugins.graphiteIntegrator;

import java.io.IOException;
import java.net.UnknownHostException;
import com.google.common.collect.ImmutableSet;
import  org.jenkinsci.plugins.graphiteIntegrator.loggers.GraphiteLogger;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.SynchronousStepExecution;
import hudson.Extension;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Collections;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.TaskListener;

import hudson.model.Run;
import hudson.model.Action;
import jenkins.model.Jenkins;
import jenkins.model.GlobalConfiguration;

import org.jenkinsci.plugins.graphiteIntegrator.metrics.GraphiteMetric;

public class MetricReporter extends Step {

    private List<String> servers;
    List<String> metricNames;
    boolean fail;

    @DataBoundConstructor public MetricReporter(@NonNull List <String> servers,
                                                @NonNull List <String> metricNames,
                                                @NonNull boolean fail) {
        this.servers = servers;
        this.metricNames = metricNames;
        this.fail = fail;
    }

    @Override public StepExecution start(StepContext context) throws Exception {
        return new Execution(this.servers, this.metricNames, this.fail, context);
    }

    public List<String> getServers() {
        return servers;
    }

    @Extension public static final class StepDescriptorImpl extends StepDescriptor {

        @Override public String getFunctionName() {
            return "graphite";
        }

        @Override public String getDisplayName() {
            return "Report single data to graphite server";
        }

        @Override public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(Run.class, TaskListener.class);
        }

    //    @Override public String argumentsToString(Map<String, Object> namedArgs) {
    //        return null; // "true" is not a reasonable description
    //    }

    }


    public static class Execution extends SynchronousStepExecution<Void> {
        
        @SuppressFBWarnings(value="SE_TRANSIENT_FIELD_NOT_RESTORED", justification="Only used when starting.")
        private transient final List<String> serverIds;
        private transient final List<String> metricNames;
        private transient final boolean fail;

        Execution(List<String> serverIds, List<String> metricNames, boolean fail, StepContext context) {
            super(context);
            this.serverIds = serverIds;
            this.metricNames = metricNames;
            this.fail = fail;
        }

        @Override protected Void run() throws Exception {
            TaskListener listener = getContext().get(TaskListener.class);
            Run run = getContext().get(Run.class);

            String baseQueueName = this.getBaseQueueName();
            GraphiteLogger graphiteLogger = new GraphiteLogger(listener.getLogger());

            ArrayList<GraphiteMetric.Snapshot> snapshots = new ArrayList<GraphiteMetric.Snapshot>();

            for(GraphiteMetric metric : GraphitePlugin.allMetrics){
                snapshots.addAll(metric.getSnapshots(run, baseQueueName, listener.getLogger()));
            }

            for(String serverId : this.serverIds){
                listener.getLogger().println(serverId);
                Server server = this.getServerById(serverId);
                this.notify(server, graphiteLogger, run, snapshots);
            }

            return null;
        }


        public void notify(Server server, GraphiteLogger graphiteLogger,
                           Run run, List<GraphiteMetric.Snapshot> snapshots) throws InterruptedException, IOException{

            for(GraphiteMetric.Snapshot snapshot : snapshots){
                graphiteLogger.logToGraphite(server.getIp(), server.getPort(), snapshot.getQueue(),
                                             snapshot.getValue(), server.getProtocol());
            }
        } 


        @NonNull public Server getServerById(@NonNull String serverDesc) {
            DescriptorImpl globalConfig = GlobalConfiguration.all().get(DescriptorImpl.class);

            Server[] servers = globalConfig.getServers();
            for (Server server : servers) {
                if (server.getDescription().equals(serverDesc)) {
                    return server;
                }
            }
            return null;
        }

        @NonNull public String getBaseQueueName() {
            DescriptorImpl globalConfig = GlobalConfiguration.all().get(DescriptorImpl.class);
            return globalConfig.getBaseQueueName();
        }


    }

}
