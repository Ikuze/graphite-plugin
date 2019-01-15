package org.jenkinsci.plugins.graphiteIntegrator.metrics;

import hudson.model.Result;
import hudson.model.Run;
import hudson.model.AbstractBuild;

import java.io.IOException;
import java.io.PrintStream;
import java.net.UnknownHostException;

import org.jenkinsci.plugins.graphiteIntegrator.loggers.GraphiteLogger;
import org.jenkinsci.plugins.graphiteIntegrator.Metric;
import org.jenkinsci.plugins.graphiteIntegrator.Server;

/**
 * 
 * @author Josh Sinfield
 */
public class BuildSuccessfulMetric extends AbstractMetric {

    /**
     * 
     * @param run
     * @param logger
     * @param graphiteLogger
     */
    public BuildSuccessfulMetric(Run<?, ?> run, PrintStream logger, GraphiteLogger graphiteLogger, String baseQueueName) {
        super(run, logger, graphiteLogger, baseQueueName);
    }

    /**
     * 
     * @param server
     * @param metric
     * @throws UnknownHostException
     * @throws IOException
     */
    @Override
    public void sendMetric(Server server, Metric... metric) throws UnknownHostException, IOException {
        
        if((run instanceof AbstractBuild && run.getResult().isCompleteBuild() && run.getResult() == Result.SUCCESS) ||
           (!(run instanceof AbstractBuild) && (run.getResult() == null || 
                                                run.getResult() == Result.SUCCESS))){
            String metricToSend = String.valueOf(1);
            sendMetric(server, metric[0], metricToSend);
        }
        
        
    }

}
