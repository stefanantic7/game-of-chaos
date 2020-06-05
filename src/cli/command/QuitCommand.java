package cli.command;

import app.AppConfig;
import app.Job;
import app.Point;
import cli.CLIParser;
import servent.SimpleServentListener;
import servent.message.QuitMessage;
import servent.message.util.MessageUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class QuitCommand implements CLICommand {

    private CLIParser parser;
    private SimpleServentListener listener;

    public QuitCommand(CLIParser parser, SimpleServentListener listener) {
        this.parser = parser;
        this.listener = listener;
    }

    @Override
    public String commandName() {
        return "quit";
    }

    @Override
    public void execute(String args) {
        AppConfig.timestampedStandardPrint("Please wait, stopping...");

        // 1. Update first neighbour and start chain reaction :)
        if (AppConfig.chordState.hasNextNode()) {
            Job activeJob = null;
            if (AppConfig.chordState.getJobRunner() != null) {
                activeJob = AppConfig.chordState.getJobRunner().getOriginalJob();
            }
            Set<Point> computedPoints = new HashSet<>();

            // 1. daj svoje tacke, ako sam ja radio job
            if (AppConfig.chordState.getJobRunner() != null) {
                computedPoints.addAll(AppConfig.chordState.getJobRunner().getComputedPoints());
                // 2. zaustavi posao
                AppConfig.chordState.getJobRunner().stop();
                AppConfig.chordState.setJobRunner(null);
                AppConfig.chordState.clearFractalIdToNodeId();
            }

            QuitMessage quitMessage = new QuitMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                    AppConfig.chordState.getNextNodeIp(), AppConfig.chordState.getNextNodePort(),
                    AppConfig.myServentInfo.getId(), activeJob, computedPoints);
            MessageUtil.sendMessage(quitMessage);
        }

        if (AppConfig.chordState.getJobRunner() != null) {
            // samo stopiraj runnera, protiv uroka :)
            AppConfig.chordState.getJobRunner().stop();
        }

        // 2. Notify bootstrap server
        try {
            Socket bsSocket = new Socket(AppConfig.BOOTSTRAP_IP, AppConfig.BOOTSTRAP_PORT);

            PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
            bsWriter.write("Quit\n" + AppConfig.myServentInfo.getIpAddress() + ":" + AppConfig.myServentInfo.getListenerPort() + "\n");
            bsWriter.flush();

            bsSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        parser.stop();
        listener.stop();
    }
}
