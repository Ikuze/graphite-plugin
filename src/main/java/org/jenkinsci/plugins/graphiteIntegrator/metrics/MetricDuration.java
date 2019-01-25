package org.jenkinsci.plugins.graphiteIntegrator.metrics;

import hudson.model.Run;
import edu.umd.cs.findbugs.annotations.NonNull;


public class MetricDuration extends GraphiteMetric {

    @Override
    public String getQueueName(){
        return "duration";
    }

    @Override
    public String getValue(@NonNull Run run){
        String duration = null;

        // Depending on the jenkins version build duration will have a value or not
        //  if the build has not finished. If there is no value, we calculate it.
        if(run.getDuration() != 0){
            duration = String.valueOf((new Long(run.getDuration()).intValue() / 1000));
        }
        else{
            duration = String.valueOf((new Long((System.currentTimeMillis() - run.getStartTimeInMillis())/1000)));
            this.log("Calculated duration: " + duration + " seconds.");
        }

        return duration;
    }

}
