package app;

final public class ServentEntity {
    private final String ip;
    private final int port;

    public ServentEntity(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public ServentEntity(String ipAndPort) {
        String[] ipAndPortArray = ipAndPort.split(":");
        this.ip = ipAndPortArray[0];
        this.port = Integer.parseInt(ipAndPortArray[1]);
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }
}
