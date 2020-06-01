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
        if (args == null) {
            AppConfig.timestampedErrorPrint("Job name as argument is required");
            return;
        }

        String[] argsArray = args.split(" ");

        String jobName = argsArray[0];

        String fractalId = null;
        if (argsArray.length > 1) {
            fractalId = argsArray[1];
        }

        // get result for whole job
        if (fractalId == null) {
            AppConfig.timestampedStandardPrint("Collecting computed points for job \"" + jobName + "\"...");

            ServentInfo firstServent = AppConfig.chordState.getAllNodeInfo().get(0);

            // todo: fix asap ne slati ovako
            AskForResultMessage askForResultMessage = new AskForResultMessage(
                    AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                    firstServent.getIpAddress(), firstServent.getListenerPort());
            MessageUtil.sendMessage(askForResultMessage);
        }
        // get result for specific job and fractalId
        else {
            Integer executorId = AppConfig.chordState.getIdForFractalId(fractalId);
            if (executorId == null) {
                AppConfig.timestampedErrorPrint("Fractal id " + fractalId + " does not exists");
                return;
            }
            ServentInfo executorServent = AppConfig.chordState.getAllNodeInfo().get(executorId);
            // todo: fix sending
            AskForResultMessage askForResultMessage = new AskForResultMessage(
                    AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                    executorServent.getIpAddress(), executorServent.getListenerPort(), fractalId);
            MessageUtil.sendMessage(askForResultMessage);
        }
    }

}
