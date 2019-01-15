package org.jenkinsci.plugins.graphiteIntegrator.metrics;

import hudson.model.Result;
import hudson.model.Run;

import java.io.IOException;
import java.io.PrintStream;
import java.net.UnknownHostException;

import org.jenkinsci.plugins.graphiteIntegrator.loggers.GraphiteLogger;
import org.jenkinsci.plugins.graphiteIntegrator.Metric;
import org.jenkinsci.plugins.graphiteIntegrator.Server;


public class BuildResultMetric extends AbstractMetric {
    /** 
     *
     * @param run
     * @param logger
     * @param graphiteLogger
     */
    public BuildResultMetric(Run<?, ?> run, PrintStream logger, GraphiteLogger graphiteLogger, String baseQueueName) {
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
       
        Result result = null;
        String resultQueueName = "result";

        if (!run.getActions(jenkins.model.InterruptedBuildAction.class).isEmpty()) {
            result = Result.ABORTED;
        }
        else if(run.getResult() == null){
            result = Result.SUCCESS;        
        }
        else{
            result = run .getResult();
        }
        metric[0].setName(result.toString());
        metric[0].setQueueName(metric[0].getQueueName().concat(".").concat(resultQueueName));
 
        String metricToSend = String.valueOf(1);
        sendMetric(server, metric[0], metricToSend);
    }
}
