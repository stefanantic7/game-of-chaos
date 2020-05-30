package app;

import cli.CLIParser;
import servent.SimpleServentListener;

/**
 * Describes the procedure for starting a single Servent
 *
 * @author bmilojkovic
 */
public class ServentMain {

	/**
	 * Command line arguments are:
	 * 0 - path to servent list file
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			AppConfig.timestampedErrorPrint("Please provide servent list file.");
		}

		int portNumber = -1;

		String serventConfig = args[0];

		AppConfig.readConfig(serventConfig);

		try {
			portNumber = AppConfig.myServentInfo.getListenerPort();

			if (portNumber < 1000 || portNumber > 2000) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException e) {
			AppConfig.timestampedErrorPrint("Port number should be in range 1000-2000. Exiting...");
			System.exit(0);
		}

		AppConfig.timestampedStandardPrint("Starting servent " + AppConfig.myServentInfo);

		SimpleServentListener simpleListener = new SimpleServentListener();
		Thread listenerThread = new Thread(simpleListener);
		listenerThread.start();

		CLIParser cliParser = new CLIParser(simpleListener);
		Thread cliThread = new Thread(cliParser);
		cliThread.start();

		ServentInitializer serventInitializer = new ServentInitializer();
		Thread initializerThread = new Thread(serventInitializer);
		initializerThread.start();

	}
}
