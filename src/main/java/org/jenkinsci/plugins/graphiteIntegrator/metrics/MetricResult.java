package org.jenkinsci.plugins.graphiteIntegrator.metrics;

import hudson.model.Result;
import hudson.model.Run;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.io.PrintStream;
import java.net.UnknownHostException;

public class MetricResult extends GraphiteMetric {
    public MetricResult(PrintStream logger) {
        super(logger);
    }

    @Override
    public Snapshot getSnapshot(@NonNull Run run, @NonNull String baseQueue){

        Result result = null;
        String resultQueueName = "result";

        if (!run.getActions(jenkins.model.InterruptedBuildAction.class).isEmpty()) {
            result = Result.ABORTED;
        }
        else if(run.getResult() == null){
            result = Result.SUCCESS;        
        }
        else{
            result = run.getResult();
        }

        Snapshot snapshot = new Snapshot(baseQueue.concat(".").concat(resultQueueName),
                                         result.toString());

        return snapshot;
    }

}
