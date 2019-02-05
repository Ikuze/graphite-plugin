package org.jenkinsci.plugins.another.graphite;

import org.jenkinsci.plugins.another.graphite.metrics.GraphiteMetric.Snapshot;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.PrintStream;
import java.util.List;
import java.io.IOException;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.io.DataOutputStream;
import hudson.model.AbstractDescribableImpl;

import utils.GraphiteValidator;
import hudson.util.FormValidation;
import hudson.model.Descriptor;
import hudson.Extension;
import org.kohsuke.stapler.QueryParameter;

import java.util.List;
import hudson.util.ListBoxModel;
import org.kohsuke.stapler.DataBoundConstructor;

public class Server extends AbstractDescribableImpl<Server> {

    String ip;

    String port;

    String id;

    String protocol;

    @DataBoundConstructor
    public Server(@NonNull String ip, @NonNull String port,
                  @NonNull String id, @NonNull String protocol){
        this.ip = ip;
        this.id = id;
        this.port = port;
        this.protocol = protocol;
    }

    public void setProtocol(@NonNull String protocol) {
        this.protocol = protocol;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setIp(@NonNull String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    public void setPort(@NonNull String port) {
        this.port = port;
    }

    public String getPort() {
        return port;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void send(@NonNull List<Snapshot> snapshots, PrintStream logger) throws UnknownHostException, IOException {
        for(Snapshot snapshot: snapshots){
            this.send(snapshot.getQueue(), snapshot.getValue(), logger);
        }
    }

    public void send(@NonNull Snapshot snapshot, PrintStream logger) throws UnknownHostException, IOException {
        this.send(snapshot.getQueue(), snapshot.getValue(), logger);
    }

    public void send(@NonNull String queue, @NonNull String value, PrintStream logger) throws UnknownHostException, IOException {
        
        if (this.getProtocol().equals("TCP")) {
            sendTCP(queue, value, logger);
        }
        
        if (this.getProtocol().equals("UDP")) {
            sendUDP(queue, value, logger);
        }
    }
    
    private void sendUDP(@NonNull String queue, @NonNull String value, PrintStream logger) throws UnknownHostException, IOException {
        long timestamp = System.currentTimeMillis()/1000;
        String data = queue + " " + value + " " + timestamp + "\n";
        int intPort = Integer.parseInt(this.getPort());
        byte[] buffer = data.getBytes();
        InetAddress IPAddress = InetAddress.getByName(this.getIp());
        
        try {
            DatagramSocket sock = new DatagramSocket(intPort);
            DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, IPAddress, intPort);
            sock.send(sendPacket);
            sock.close();
        } catch(IOException e) {
            if(logger != null){
                logger.println("there was an exception: " + e.toString());
            }
            e.printStackTrace();
        }
    }
    
    private void sendTCP(@NonNull String queue, @NonNull String value, PrintStream logger) throws UnknownHostException, IOException  {
        Socket conn = new Socket(this.getIp(), Integer.parseInt(this.getPort()));
        
        DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
        String data = queue + " " + value + " " + (System.currentTimeMillis()/1000) + "\n";

        if(logger != null){
            logger.println("SENT DATA: " + data);
        }

        dos.writeBytes(data);
        conn.close();
    }


    @Extension
    public static class DescriptorImpl extends Descriptor<Server> {
        private GraphiteValidator validator = new GraphiteValidator();

        @Override
        public String getDisplayName() {
            return "Graphite Server";
        }

        public FormValidation doTestConnection(@QueryParameter("ip") final String ip,
            @QueryParameter("port") final String port,
            @QueryParameter("protocol") final String protocol) {
            if(protocol.equals("UDP")) {
                return FormValidation.ok("UDP is configured");
            }
            else if(protocol.equals("TCP")) {
                if (!validator.isIpPresent(ip) || !validator.isPortPresent(port)
                        || !validator.isListening(ip, Integer.parseInt(port))) {
                    return FormValidation.error("Server is not listening... Or ip:port are not correctly filled");
                }

                return FormValidation.ok("Server is listening");
            } else {
                return FormValidation.ok("Unknown protocol");
            }
        }

        public FormValidation doCheckIp(@QueryParameter final String value) {
            if (!validator.isIpPresent(value)) {
                return FormValidation.error("Please set an IP");
            }
            if (!validator.validateIpFormat(value)) {
                return FormValidation.error("Cannot reach this IP/Host.");
            }

            return FormValidation.ok("IP/Host is correctly configured");
        }

        public FormValidation doCheckId(@QueryParameter final String value) {
            if (!validator.isIDPresent(value)) {
                return FormValidation.error("Please set an ID");
            }
            int length = 50;
            if (validator.isIDTooLong(value, length)) {
                return FormValidation.error(String.format("ID is limited to %d characters", length));
            }

            return FormValidation.ok("ID is correctly configured");
        }

        public FormValidation doCheckPort(@QueryParameter final String value) {
            if (!validator.isPortPresent(value)) {
                return FormValidation.error("Please set a port");
            }

            if (!validator.validatePortFormat(value)) {
                return FormValidation.error("Please check the port format");
            }

            return FormValidation.ok("Port is correctly configured");
        }
        
        public ListBoxModel doFillProtocolItems(){
            ListBoxModel protocols = new ListBoxModel();
            protocols.add("UDP", "UDP");
            protocols.add("TCP", "TCP");

            return protocols;
        }

    }
}
