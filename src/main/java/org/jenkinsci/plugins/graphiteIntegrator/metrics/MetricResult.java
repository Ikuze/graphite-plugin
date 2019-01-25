package org.jenkinsci.plugins.graphiteIntegrator.metrics;

import hudson.Extension;
import hudson.model.Result;
import hudson.model.Run;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.io.PrintStream;
import java.net.UnknownHostException;

import java.util.ArrayList;
import java.util.List;

@Extension
public class MetricResult extends GraphiteMetric {

    @Override
    public List<Snapshot> getSnapshots(@NonNull Run run, @NonNull String baseQueue, PrintStream logger){
        Result result = null;
        String queueName = "result";

        if (!run.getActions(jenkins.model.InterruptedBuildAction.class).isEmpty()) {
            result = Result.ABORTED;
        }
        else if(run.getResult() == null){
            result = Result.SUCCESS;        
        }
        else{
            result = run.getResult();
        }

        Snapshot snapshot = new Snapshot(baseQueue.concat(".").concat(queueName),
                                         result.toString());

        ArrayList<Snapshot> snapshots = new ArrayList<Snapshot>();
        snapshots.add(snapshot);

        this.log(logger, "Metric Result: " + snapshot.getValue());

        return snapshots;
    }

}
