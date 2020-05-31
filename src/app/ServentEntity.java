package app;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServentEntity that = (ServentEntity) o;
        return port == that.port &&
                Objects.equals(ip, that.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port);
    }

    @Override
    public String toString() {
        return "ServentEntity{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }
}
