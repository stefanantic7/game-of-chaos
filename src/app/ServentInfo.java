package app;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This is an immutable class that holds all the information for a servent.
 *
 * @author bmilojkovic
 */
public class ServentInfo implements Serializable {

	private static final long serialVersionUID = 5304170042791281555L;
	private int id;
	private final String ipAddress;
	private final int listenerPort;

	private final List<Job> jobs;

	public ServentInfo(String ipAddress, int listenerPort) {
		this.ipAddress = ipAddress;
		this.listenerPort = listenerPort;

		this.jobs = new ArrayList<>();
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public int getListenerPort() {
		return listenerPort;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void addJob(Job job) {
		this.jobs.add(job);
	}

	public Job getFirstJob() {
		if (this.jobs.size() == 0) {
			return null;
		}
		return this.jobs.get(0);
	}

	@Override
	public String toString() {
		return "[" + id + "|" + ipAddress + "|" + listenerPort + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ServentInfo that = (ServentInfo) o;
		return listenerPort == that.listenerPort &&
				Objects.equals(ipAddress, that.ipAddress);
	}

	@Override
	public int hashCode() {
		return Objects.hash(ipAddress, listenerPort);
	}
}
