package cli.command;

import app.AppConfig;
import app.ServentInfo;
import servent.message.AskForResultMessage;
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
    public void execute(String args) {
        if (args == null || args.equals("")) {
            AppConfig.timestampedErrorPrint("Please provide the job name");
            return;
        }

        String[] argsArray = args.split(" ");
        String jobName = argsArray[0];

        String fractalId = null;
        if (argsArray.length > 1) {
            fractalId = argsArray[1];
        }

        AppConfig.timestampedStandardPrint("Getting status of \"" +jobName + "\"" );

        if (fractalId == null) {
            ServentInfo firstNode = AppConfig.chordState.getAllNodeInfo().get(0);

            AskForStatusMessage message = new AskForStatusMessage(
                    AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                    firstNode.getIpAddress(), firstNode.getListenerPort(),
                    jobName, new HashMap<>());
            MessageUtil.sendMessage(message);
        }
        else {
            Integer serventId = AppConfig.chordState.getIdForFractalId(fractalId);
            if (serventId == null) {
                AppConfig.timestampedErrorPrint("Fractal id " + fractalId + " does not exists");
                return;
            }
            ServentInfo concreteServent = AppConfig.chordState.getAllNodeInfo().get(serventId);

            AskForStatusMessage message = new AskForStatusMessage(
                    AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                    concreteServent.getIpAddress(), concreteServent.getListenerPort(),
                    jobName, new HashMap<>(), fractalId);
            MessageUtil.sendMessage(message);
        }
    }
}
