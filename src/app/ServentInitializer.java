package app;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import servent.message.NewNodeMessage;
import servent.message.util.MessageUtil;

public class ServentInitializer implements Runnable {

	private ServentEntity[] getLastAndFirstServentEntities() {
		String bsIp = AppConfig.BOOTSTRAP_IP;
		int bsPort = AppConfig.BOOTSTRAP_PORT;

		ServentEntity[] retVal = null;

		try {
			Socket bsSocket = new Socket(bsIp, bsPort);

			PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
			bsWriter.write("Hail\n" +
					AppConfig.myServentInfo.getIpAddress() + ":" + AppConfig.myServentInfo.getListenerPort() + "\n");

			bsWriter.flush();

			Scanner bsScanner = new Scanner(bsSocket.getInputStream());
			String lastServentIpAndPort = bsScanner.nextLine();
			String firstServentIpAndPort = bsScanner.nextLine();
			bsSocket.close();


			ServentEntity[] lastAndFirstServentEntities = new ServentEntity[2];
			lastAndFirstServentEntities[0] = new ServentEntity(lastServentIpAndPort);
			lastAndFirstServentEntities[1] = new ServentEntity(firstServentIpAndPort);

			retVal = lastAndFirstServentEntities;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return retVal;
	}

	@Override
	public void run() {
		ServentEntity[] lastAndFirstServentEntities = getLastAndFirstServentEntities();

		if (lastAndFirstServentEntities == null) {
			AppConfig.timestampedErrorPrint("Error in contacting bootstrap. Exiting...");
			System.exit(0);
		}
		if (lastAndFirstServentEntities[0].getPort() == -1 && lastAndFirstServentEntities[1].getPort() == -1) { //bootstrap gave us -1 -> we are first
			AppConfig.myServentInfo.setId(0);
			AppConfig.timestampedStandardPrint("First node in Chord system.");
		} else { //bootstrap gave us something else - let that node tell our successor that we are here


			NewNodeMessage nnm = new NewNodeMessage(
					AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
					lastAndFirstServentEntities[0].getIp(), lastAndFirstServentEntities[0].getPort());
			MessageUtil.sendMessage(nnm);
		}
	}

}
