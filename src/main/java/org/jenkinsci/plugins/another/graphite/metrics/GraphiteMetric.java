package org.jenkinsci.plugins.another.graphite.metrics;


import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionPoint;
import hudson.model.Run;
import java.io.PrintStream;

import java.util.List;


public abstract class GraphiteMetric implements ExtensionPoint {


        public void log(PrintStream logger, String message){
            if(logger != null){
		logger.println(message);
            }
        }

        @NonNull abstract public List<Snapshot> getSnapshots(@NonNull Run run, @NonNull String baseQueue, PrintStream logger);
        @NonNull abstract public String getName();

        static public class Snapshot{
            private String queue;
            private String value;

            public Snapshot(@NonNull String queue, @NonNull String value){
                this.queue = queue;
                this.value = value;
            }

            public String getValue(){
                return this.value;
            }

            public String getQueue(){
                return this.queue;
            }

            public void setValue(@NonNull String value){
                this.value = value;
            }

            public void setQueue(@NonNull String queue){
                this.queue = queue;
            }

        }
}
