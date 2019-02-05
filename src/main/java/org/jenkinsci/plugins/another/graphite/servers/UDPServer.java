package org.jenkinsci.plugins.another.graphite.servers;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.PrintStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import hudson.util.FormValidation;
import hudson.Extension;
import org.kohsuke.stapler.QueryParameter;

import org.kohsuke.stapler.DataBoundConstructor;

public class UDPServer extends Server {

    @DataBoundConstructor
    public UDPServer(@NonNull String ip, @NonNull String port,
                     @NonNull String id){
        this.ip = ip;
        this.id = id;
        this.port = port;
    }

    @Override
    public void send(@NonNull String queue, @NonNull String value, PrintStream logger) throws UnknownHostException, IOException  {
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
                logger.println("There was an exception sending graphite UDP: " + e.toString());
            }
            e.printStackTrace();
        }
    }

    @Extension
    public static class UDPDescriptorImpl extends DescriptorImpl {
        @Override
        public String getDisplayName() {
            return "UDP Graphite Server";
        }

        @Override
        public FormValidation doTestConnection(@QueryParameter("ip") final String ip,
                                               @QueryParameter("port") final String port) {
            if (!validator.isIpPresent(ip) || !validator.isPortPresent(port) || 
                !validator.validateIpFormat(ip) || !validator.validatePortFormat(port)) {
                return FormValidation.error("We can't reach the server... Or ip:port are not correctly filled");
            }

            return FormValidation.ok("Server is UDP...");
        }
    }
}
