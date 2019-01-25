package org.jenkinsci.plugins.graphiteIntegrator.metrics;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionPoint;
import hudson.model.Run;
import java.io.PrintStream;


public abstract class GraphiteMetric implements ExtensionPoint {

        protected PrintStream logger;

        public GraphiteMetric(){
            this.logger = null;
        }

        public GraphiteMetric(PrintStream logger){
            this.logger = logger;
        }

        public void log(String message){
            if(this.logger != null){
		this.logger.println(message);
            }
        }

        public void setLogger(PrintStream logger){
            this.logger = logger;
        }

        @NonNull abstract public String getQueueName();
        @NonNull abstract public String getValue(Run run);

        @NonNull final public Snapshot getSnapshot(Run run, @NonNull String baseQueue){
            Snapshot snapshot = new Snapshot(baseQueue.concat(".").concat(this.getQueueName()),
                                             this.getValue(run));

            return snapshot;
        }

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
