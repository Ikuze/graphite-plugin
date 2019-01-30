package org.jenkinsci.plugins.graphiteIntegrator;


public class Server {

    String ip;

    String port;

    String id;

    String protocol;

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
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

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setId(String id) {
        this.id = id;
    }

}
