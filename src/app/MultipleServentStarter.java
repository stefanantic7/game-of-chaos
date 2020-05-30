package app;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * This class implements the logic for starting multiple servent instances.
 *
 * To use it, invoke startServentTest with a directory name as parameter.
 * This directory should include:
 * <ul>
 * <li>A <code>servent_list.properties</code> file (explained in {@link AppConfig} class</li>
 * <li>A directory called <code>output</code> </li>
 * <li>A directory called <code>error</code> </li>
 * <li>A directory called <code>input</code> with text files called
 * <code> servent0_in.txt </code>, <code>servent1_in.txt</code>, ... and so on for each servent.
 * These files should contain the commands for each servent, as they would be entered in console.</li>
 * </ul>
 *
 * @author bmilojkovic
 */
public class MultipleServentStarter {

	/**
	 * We will wait for user stop in a separate thread.
	 * The main thread is waiting for processes to end naturally.
	 */
	private static class ServentCLI implements Runnable {

		private Process bsProcess;

		public ServentCLI(Process bsProcess) {
			this.bsProcess = bsProcess;
		}

		@Override
		public void run() {
			Scanner sc = new Scanner(System.in);

			while(true) {
				String line = sc.nextLine();

				if (line.equals("stop")) {
					bsProcess.destroy();
					break;
				}
			}

			sc.close();
		}
	}

	/**
	 * The parameter for this function should be the name of a directory that
	 * contains a servent_list.properties file which will describe our distributed system.
	 */
	private static void startServentTest(String testName) {
		AppConfig.readBootstrapConfig(testName+"/servent_list.properties");

		AppConfig.timestampedStandardPrint("Starting multiple servent runner. "
				+ "If servents do not finish on their own, type \"stop\" to finish them");

		Process bsProcess = null;
		ProcessBuilder bsBuilder = new ProcessBuilder("java", "-cp", "bin/", "app.BootstrapServer", String.valueOf(AppConfig.BOOTSTRAP_PORT));

		bsBuilder.redirectOutput(new File(testName+"/output/bootstrap_out.txt"));
		bsBuilder.redirectError(new File(testName+"/error/bootstrap_err.txt"));
		bsBuilder.redirectInput(new File(testName+"/input/bootstrap_in.txt"));

		try {
			bsProcess = bsBuilder.start();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		//wait for bootstrap to start
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		Thread t = new Thread(new ServentCLI(bsProcess));

		t.start(); //CLI thread waiting for user to type "stop".

		AppConfig.timestampedStandardPrint("All servent processes finished. Type \"stop\" to halt bootstrap.");
		try {
			bsProcess.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		startServentTest("chord");
	}

}
