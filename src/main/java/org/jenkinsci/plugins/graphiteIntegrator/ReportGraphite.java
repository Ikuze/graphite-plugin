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

import utils.MetricsEnum;
import org.jenkinsci.plugins.graphiteIntegrator.metrics.AbstractMetric;
import org.jenkinsci.plugins.graphiteIntegrator.metrics.BuildDurationMetric;
import org.jenkinsci.plugins.graphiteIntegrator.metrics.BuildFailedMetric;
import org.jenkinsci.plugins.graphiteIntegrator.metrics.BuildSuccessfulMetric;
import org.jenkinsci.plugins.graphiteIntegrator.metrics.BuildResultMetric;
import org.jenkinsci.plugins.graphiteIntegrator.metrics.CoberturaCodeCoverageMetric;
import org.jenkinsci.plugins.graphiteIntegrator.metrics.FailTestsMetric;
import org.jenkinsci.plugins.graphiteIntegrator.metrics.SkipTestsMetric;
import org.jenkinsci.plugins.graphiteIntegrator.metrics.TotalTestsMetric;


public class ReportGraphite extends Step {

    private List<String> servers;

    @DataBoundConstructor public ReportGraphite(List<String> servers) {
        this.servers = servers;
    }

    @Override public StepExecution start(StepContext context) throws Exception {
        return new Execution(this.servers, context);
    }

    //@DataBoundSetter public void setServer(String server) {
    //    this.server = server;
    //}

    public List<String> getServers() {
        return servers;
    }

    @Extension public static final class StepDescriptorImpl extends StepDescriptor {

        @Override public String getFunctionName() {
            return "graphite";
        }

        @Override public String getDisplayName() {
            return "Report metrics to graphite server";
        }

        @Override public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(Run.class, TaskListener.class);
            //return Collections.singleton(TaskListener.class);
        }

    //    @Override public String argumentsToString(Map<String, Object> namedArgs) {
    //        return null; // "true" is not a reasonable description
    //    }

    }


    public static class Execution extends SynchronousStepExecution<Void> {
        
        @SuppressFBWarnings(value="SE_TRANSIENT_FIELD_NOT_RESTORED", justification="Only used when starting.")
        private transient final List<String> serverDescs;

        Execution(List<String> serverDescs, StepContext context) {
            super(context);
            this.serverDescs = serverDescs;
        }

        @Override protected Void run() throws Exception {
            TaskListener listener = getContext().get(TaskListener.class);
            Run run = getContext().get(Run.class);
            String baseQueueName = this.getBaseQueueName();
            GraphiteLogger graphiteLogger = new GraphiteLogger(listener.getLogger());

            for(String serverDesc : this.serverDescs){
                listener.getLogger().println(serverDesc);
                Server server = this.getServerByDesc(serverDesc);
                this.notify(server, run, graphiteLogger,  baseQueueName);
            }

            return null;
        }

        public Metric getMetric(String name, String queueName){
            Metric metric =  new Metric();
            metric.setName(name); 
            metric.setQueueName(queueName); 

            return metric;
        }

        public Void notify(Server server, Run run, GraphiteLogger graphiteLogger, String baseQueueName) throws InterruptedException, IOException{
            AbstractMetric metricSender = null;

            List<Metric> coberturaMetrics = null;
            Metric metric = null;

            metric = this.getMetric(MetricsEnum.BUILD_DURATION.name() ,"");
            metricSender = new BuildDurationMetric(run, graphiteLogger.getLogger(), graphiteLogger, baseQueueName);
            metricSender.sendMetric(server, metric);

            metric = this.getMetric("", run.getParent().getFullName());
            metricSender = new BuildResultMetric(run, graphiteLogger.getLogger(), graphiteLogger, baseQueueName);
            metricSender.sendMetric(server, metric);

/*
                    metric = this.getMetric(MetricsEnum.BUILD_FAILED.name() ,"");
                    metricSender = new BuildFailedMetric(run, graphiteLogger.getLogger(), graphiteLogger, baseQueueName);
                    metricSender.sendMetric(server, metric);

                    metric = this.getMetric(MetricsEnum.BUILD_SUCCESSFUL.name() ,"");
                    metricSender = new BuildSuccessfulMetric(run, graphiteLogger.getLogger(), graphiteLogger, baseQueueName);
                    metricSender.sendMetric(server, metric);
*/
                //if (isCoberturaMetric(metric)) {
                //    if (!isCoberturaListInitialized(coberturaMetrics)) {
                //        coberturaMetrics = new ArrayList<Metric>();
                //    }
                //    coberturaMetrics.add(metric);
                //}
                // If a Freestyle Build has been configured (without publishing
                // JUnit XML Results) these will fail.
                // Added simple null check in for now to be safe.
                if (this.getTestResultAction(run) != null) {
                        metric = this.getMetric(MetricsEnum.FAIL_TESTS.name() ,"");
                        metricSender = new FailTestsMetric(run, graphiteLogger.getLogger(), graphiteLogger, baseQueueName);
                        metricSender.sendMetric(server, metric);

                        metric = this.getMetric(MetricsEnum.SKIPED_TESTS.name() ,"");
                        metricSender = new SkipTestsMetric(run, graphiteLogger.getLogger(), graphiteLogger, baseQueueName);
                        metricSender.sendMetric(server, metric);

                        metric = this.getMetric(MetricsEnum.TOTAL_TESTS.name() ,"");
                        metricSender = new TotalTestsMetric(run, graphiteLogger.getLogger(), graphiteLogger, baseQueueName);
                        metricSender.sendMetric(server, metric);
                }

            //if (isCoberturaListInitialized(coberturaMetrics)) {
            //    metricSender = new CoberturaCodeCoverageMetric(run, graphiteLogger.getLogger(), graphiteLogger, DESCRIPTOR.getBaseQueueName());
            //    metricSender.sendMetric(getServer(), coberturaMetrics.toArray(new Metric[coberturaMetrics.size()]));
            //}
            return null;
        } 

        public Action getTestResultAction(Run run) {
            try {
                return run.getAction(Jenkins.getInstance().getPluginManager().uberClassLoader.loadClass("hudson.tasks.test.AbstractTestResultAction").asSubclass(Action.class));
            } catch (ClassNotFoundException x) {
                return null;
            }
        }


        @NonNull public Server getServerByDesc(@NonNull String serverDesc) {
            DescriptorImpl graphiteDescriptor = new DescriptorImpl();
            Server[] servers = graphiteDescriptor.getServers();
            for (Server server : servers) {
                if (server.getDescription().equals(serverDesc)) {
                    return server;
                }
            }
            return null;
        }

        @NonNull public String getBaseQueueName() {
            DescriptorImpl graphiteDescriptor = new DescriptorImpl();
            return graphiteDescriptor.getBaseQueueName();
        }

        private static final long serialVersionUID = 1L;

    }

}
