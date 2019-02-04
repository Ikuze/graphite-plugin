package org.jenkinsci.plugins.another.graphite.metrics;

import hudson.Extension;
import hudson.model.Run;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.io.PrintStream;

import hudson.tasks.test.AbstractTestResultAction;

@Extension
public class MetricTests extends GraphiteMetric {

    @Override
    public String getName(){
        return "tests";
    }


    @Override
    public List<Snapshot> getSnapshots(@NonNull Run run, @NonNull String baseQueue, PrintStream logger){
        String queueName = this.getName();
        String queue = baseQueue.concat(".").concat(queueName);

        AbstractTestResultAction action = run.getAction(AbstractTestResultAction.class);

        ArrayList<Snapshot> snapshots = new ArrayList<Snapshot>();
        if(action != null){
            Snapshot snapshot = new Snapshot(queue.concat(".").concat("skipped"),
                                             Integer.toString(action.getSkipCount()));
            snapshots.add(snapshot);

            snapshot = new Snapshot(queue.concat(".").concat("failed"),
                                    Integer.toString(action.getFailCount()));
            snapshots.add(snapshot);

            snapshot = new Snapshot(queue.concat(".").concat("total"),
                                    Integer.toString(action.getTotalCount()));
            snapshots.add(snapshot);
        }
        else{
            this.log(logger, "No test found! Nothing to report.");
        }

        return snapshots;
    }
}
