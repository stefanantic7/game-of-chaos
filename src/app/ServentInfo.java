package app;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

	@Override
	public String toString() {
		return "[" + id + "|" + ipAddress + "|" + listenerPort + "]";
	}

}
