package app;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class contains all the global application configuration stuff.
 * @author bmilojkovic
 *
 */
public class AppConfig {

	/**
	 * Convenience access for this servent's information
	 */
	public static ServentInfo myServentInfo;

	/**
	 * Print a message to stdout with a timestamp
	 * @param message message to print
	 */
	public static void timestampedStandardPrint(String message) {
		DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		Date now = new Date();

		System.out.println(timeFormat.format(now) + " - " + message);
	}

	/**
	 * Print a message to stderr with a timestamp
	 * @param message message to print
	 */
	public static void timestampedErrorPrint(String message) {
		DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		Date now = new Date();

		System.err.println(timeFormat.format(now) + " - " + message);
	}

	public static boolean INITIALIZED = false;
	public static String BOOTSTRAP_IP;
	public static int BOOTSTRAP_PORT;

	public static ChordState chordState;

	public static void readBootstrapConfig(String configName){
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(new File(configName)));

		} catch (IOException e) {
			timestampedErrorPrint("Couldn't open properties file. Exiting...");
			System.exit(0);
		}

		BOOTSTRAP_IP = properties.getProperty("bs.ip");

		try {
			BOOTSTRAP_PORT = Integer.parseInt(properties.getProperty("bs.port"));
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading bootstrap_port. Exiting...");
			System.exit(0);
		}

		chordState = new ChordState();

		myServentInfo = new ServentInfo(BOOTSTRAP_IP, BOOTSTRAP_PORT);
	}

	/**
	 * Reads a config file. Should be called once at start of app.
	 * The config file should be of the following format:
	 * <br/>
	 * <code><br/>
	 * servent_count=3 			- number of servents in the system <br/>
	 * chord_size=64			- maximum value for Chord keys <br/>
	 * bs.port=2000				- bootstrap server listener port <br/>
	 * servent0.port=1100 		- listener ports for each servent <br/>
	 * servent1.port=1200 <br/>
	 * servent2.port=1300 <br/>
	 *
	 * </code>
	 * <br/>
	 * So in this case, we would have three servents, listening on ports:
	 * 1100, 1200, and 1300. A bootstrap server listening on port 2000, and Chord system with
	 * max 64 keys and 64 nodes.<br/>
	 *
	 * @param configName name of configuration file
	 */
	public static void readConfig(String configName){
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(new File(configName)));

		} catch (IOException e) {
			timestampedErrorPrint("Couldn't open properties file. Exiting...");
			System.exit(0);
		}

		try {
			BOOTSTRAP_PORT = Integer.parseInt(properties.getProperty("bs.port"));
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading bootstrap_port. Exiting...");
			System.exit(0);
		}

		chordState = new ChordState();

		try {
			String ipAddress = properties.getProperty("ip");
			int listenerPort = Integer.parseInt(properties.getProperty("port"));

			myServentInfo = new ServentInfo(ipAddress, listenerPort);
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading ip_address and port. Exiting...");
			System.exit(0);
		}

		int jobCount = 0;
		try {
			jobCount = Integer.parseInt(properties.getProperty("job_count"));

			for (int i=0; i < jobCount; i++) {
				String name = properties.getProperty("job"+i+".name");
				String[] pointsCoordinates = properties.getProperty("job"+i+".points.coordinates").split(";");
				double proportion = Double.parseDouble(properties.getProperty("job"+i+".proportion"));
				int width = Integer.parseInt(properties.getProperty("job"+i+".width"));
				int height = Integer.parseInt(properties.getProperty("job"+i+".height"));

				List<Point> points = new ArrayList<>();
				for (String coordinates: pointsCoordinates) {
					String[] xy = coordinates.substring(1, coordinates.length() - 1).split(",");
					points.add(new Point(Integer.parseInt(xy[0]), Integer.parseInt(xy[1])));
				}

				Job job = new Job(name, proportion, width, height, points);
				myServentInfo.addJob(job);
			}
		} catch (NumberFormatException numberFormatException) {
			timestampedErrorPrint("Problem reading jobs. Exiting...");
			System.exit(0);
		}
	}

}
