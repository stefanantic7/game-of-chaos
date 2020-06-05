package servent.handler;

import app.*;
import cli.command.StartJobCommand;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.StartJobMessage;
import servent.message.util.MessageUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StartJobHandler implements MessageHandler {

    private Message clientMessage;

    public StartJobHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }


    @Override
    public void run() {
        try {
            this.handle();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void handle() {
        if (clientMessage.getMessageType() != MessageType.START_JOB) {
            AppConfig.timestampedErrorPrint("Handler got a message that is not START_JOB");
            return;
        }

        StartJobMessage startJobMessage = (StartJobMessage) clientMessage;

        // WARNING: this should be on the top(here) because we will remove elements of the precomputedPoints set
        int precomputedPointsCount = startJobMessage.getPrecomputedPoints().size();

        List<String> fractalIds = startJobMessage.getFractalIds();
        List<Point> pointList = startJobMessage.getInitialPoints();
        AppConfig.timestampedStandardPrint("Fractal ids: " + fractalIds.toString());
        AppConfig.timestampedStandardPrint("Starting points: " + pointList.toString());

        AppConfig.chordState.setFractalIdToNodeIdMap(startJobMessage.getFractalIdToNodeIdMap());

        Job job = startJobMessage.getJob();

        if (fractalIds.size() == 1) {
            JobRunner jobRunner = new JobRunner(job, job.getName(), fractalIds.get(0), job.getProportion(),
                    job.getWidth(), job.getHeight(), pointList);

            Set<Point> precomputedPointsForMe = new HashSet<>(startJobMessage.getPrecomputedPoints());

            System.out.println("new initial points: "+pointList);
            System.out.println("Precomputed points: "+startJobMessage.getPrecomputedPoints().size());
            jobRunner.getComputedPoints().addAll(precomputedPointsForMe);

            AppConfig.chordState.setJobRunner(jobRunner);
            Thread t = new Thread(jobRunner);
            t.start();
            return;
        }

        int level = startJobMessage.getLevel() + 1;
        int initialPointsCount = job.getInitialPointsCount();
        double proportion = job.getProportion();

        for (int i = 0; i < initialPointsCount; i++) {
            List<Point> regionPoints = new ArrayList<>();

            Point startPoint = pointList.get(i);
            for (int j = 0; j < pointList.size(); j++) {
                if (i == j) {
                    regionPoints.add(startPoint);
                    continue;
                }

                Point other = pointList.get(j);
                int newX = (int) (startPoint.getX() + proportion * (other.getX() - startPoint.getX()));
                int newY = (int) (startPoint.getY() + proportion * (other.getY() - startPoint.getY()));
                Point newPoint = new Point(newX, newY);

                regionPoints.add(newPoint);
            }

            // Svi koji imaju isti nivo ce se proslediti dalje
            List<String> partialFractalIds = new ArrayList<>();
            for (String fractal: fractalIds) {
                // gledam da li je drugi karakter (za level 1) isti kao i

                if (fractal.startsWith(fractal.substring(0,level) + i)) {
                    partialFractalIds.add(fractal);
                }
            }

            int runnerId = AppConfig.chordState.getFractalIdToNodeIdMap().get(partialFractalIds.get(0));
            ServentInfo serventRunner = AppConfig.chordState.getAllNodeInfo().get(runnerId);

            Set<Point> forwardedPoints = Boundary.takePoints(
                    startJobMessage.getPrecomputedPoints(), precomputedPointsCount,
                    regionPoints, proportion);


            StartJobMessage newStartJobMessage = new StartJobMessage(
                    AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                    serventRunner.getIpAddress(), serventRunner.getListenerPort(),
                    partialFractalIds, regionPoints, job, level, AppConfig.chordState.getFractalIdToNodeIdMap(), forwardedPoints);
            MessageUtil.sendMessage(newStartJobMessage);
        }

    }
}
