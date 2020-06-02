package cli.command;

import app.AppConfig;
import app.ServentInfo;
import servent.message.AskForStatusMessage;
import servent.message.StopJobMessage;
import servent.message.util.MessageUtil;

import java.util.HashMap;

public class StatusCommand implements CLICommand {
    @Override
    public String commandName() {
        return "status";
    }

    @Override
    public void execute(String jobName) {
        if (jobName == null || jobName.equals("")) {
            AppConfig.timestampedErrorPrint("Please provide the job name");
            return;
        }
        AppConfig.timestampedStandardPrint("Getting status of \"" +jobName + "\"" );

        ServentInfo firstNode = AppConfig.chordState.getAllNodeInfo().get(0);

        // TODO: skip links
        AskForStatusMessage message = new AskForStatusMessage(
                AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                firstNode.getIpAddress(), firstNode.getListenerPort(),
                jobName, new HashMap<>());
        MessageUtil.sendMessage(message);
    }
}
