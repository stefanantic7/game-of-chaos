package cli.command;

import app.AppConfig;
import app.ServentInfo;
import servent.message.AskForResultMessage;
import servent.message.util.MessageUtil;
//import servent.message.AskJobFractalIDResultMessage;
//import servent.message.AskJobResultMessage;

import java.util.Collections;

public class ResultCommand implements CLICommand {

    @Override
    public String commandName() {
        return "result";
    }

    @Override
    public void execute(String args) {
        if (args == null || args.equals("")) {
            AppConfig.timestampedErrorPrint("Job name as argument is required");
            return;
        }

        String[] argsArray = args.split(" ");

        String jobName = argsArray[0];

        String fractalId = null;
        if (argsArray.length > 1) {
            fractalId = argsArray[1];
        }

        if (fractalId == null) {
            AppConfig.timestampedStandardPrint("Collecting computed points for job \"" + jobName + "\"...");

            ServentInfo firstServent = AppConfig.chordState.getAllNodeInfo().get(0);

            AskForResultMessage askForResultMessage = new AskForResultMessage(
                    AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                    firstServent.getIpAddress(), firstServent.getListenerPort(),
                    jobName);
            MessageUtil.sendMessage(askForResultMessage);
        }
        else {
            Integer runnerId = AppConfig.chordState.getIdForFractalId(fractalId);
            if (runnerId == null) {
                AppConfig.timestampedErrorPrint("Fractal id " + fractalId + " does not exists");
                return;
            }
            ServentInfo serventRunner = AppConfig.chordState.getAllNodeInfo().get(runnerId);

            AskForResultMessage askForResultMessage = new AskForResultMessage(
                    AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                    serventRunner.getIpAddress(), serventRunner.getListenerPort(), jobName, fractalId);
            MessageUtil.sendMessage(askForResultMessage);
        }
    }

}
