package org.jenkinsci.plugins.graphiteIntegrator.metrics;

import hudson.model.Result;
import hudson.model.Run;
import edu.umd.cs.findbugs.annotations.NonNull;


public class MetricResult extends GraphiteMetric {

    @Override
    public String getQueueName(){
        return "result";
    }

    @Override
    public String getValue(@NonNull Run run){
        Result result = null;

        if (!run.getActions(jenkins.model.InterruptedBuildAction.class).isEmpty()) {
            result = Result.ABORTED;
        }
        else if(run.getResult() == null){
            result = Result.SUCCESS;        
        }
        else{
            result = run.getResult();
        }

        return result.toString();
    }
}
