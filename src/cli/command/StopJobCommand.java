package cli.command;

import app.AppConfig;
import app.ServentInfo;
import cli.CLIParser;
import servent.SimpleServentListener;
import servent.message.StopJobMessage;
import servent.message.util.MessageUtil;

public class StopJobCommand implements CLICommand {

	@Override
	public String commandName() {
		return "stop";
	}

	@Override
	public void execute(String jobName) {
		if (jobName == null || jobName.equals("")) {
			AppConfig.timestampedErrorPrint("Please provide the job name");
			return;
		}
		AppConfig.timestampedStandardPrint("Stopping \"" + jobName + "\"...");

		ServentInfo firstNode = AppConfig.chordState.getAllNodeInfo().get(0);

		// TODO: skip links
		StopJobMessage message = new StopJobMessage(
				AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
				firstNode.getIpAddress(), firstNode.getListenerPort(),
				jobName);
		MessageUtil.sendMessage(message);
	}

}
