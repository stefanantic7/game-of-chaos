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

            int firstServentId = this.getFirstActiveNodeId();
            int lastServentId = this.getLastActiveNodeId();
            ServentInfo firstServent = AppConfig.chordState.getAllNodeInfo().get(firstServentId);

            // todo: fix asap ne slati ovako
            AskForResultMessage askForResultMessage = new AskForResultMessage(
                    AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                    firstServent.getIpAddress(), firstServent.getListenerPort());
            MessageUtil.sendMessage(askForResultMessage);
//        }
//        // get result for specific job and fractalId
//        else {
//            int executorId = AppConfig.chordState.getIdForFractalIDAndJob(fractalId, jobName);
//            ServentInfo executorServent = AppConfig.chordState.getAllNodeIdInfoMap().get(executorId);
//            // todo: fix sending
//            AskJobFractalIDResultMessage message = new AskJobFractalIDResultMessage(
//                    AppConfig.myServentInfo.getListenerPort(),
//                    executorServent.getListenerPort(),
//                    AppConfig.myServentInfo.getIpAddress(),
//                    executorServent.getIpAddress(),
//                    jobName);
//            MessageUtil.sendMessage(message);
        }
//
//
//        // todo: BUG - kad se trazi rez na istom cvoru koji je zapoceo posao, ne radi
    }

    private int getFirstActiveNodeId() {
        return Collections.min(AppConfig.chordState.getFractalIdToNodeIdMap().values());
    }

    private int getLastActiveNodeId() {
        return Collections.max(AppConfig.chordState.getFractalIdToNodeIdMap().values());
    }

}
