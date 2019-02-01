package org.jenkinsci.plugins.graphiteIntegrator;

import org.jenkinsci.plugins.graphiteIntegrator.metrics.GraphiteMetric.Snapshot;
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

public class Server {

    String ip;

    String port;

    String id;

    String protocol;


    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(@NonNull String protocol) {
        this.protocol = protocol;
    }

    public String getIp() {
        return ip;
    }

    public String getPort() {
        return port;
    }

    public String getId() {
        return id;
    }

    public void setIp(@NonNull String ip) {
        this.ip = ip;
    }

    public void setPort(@NonNull String port) {
        this.port = port;
    }

    public void setId(@NonNull String id) {
        this.id = id;
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

}
