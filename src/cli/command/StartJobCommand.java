package cli.command;

import app.AppConfig;
import app.Job;
import app.Point;
import app.ServentInfo;
import servent.message.StartJobMessage;
import servent.message.util.MessageUtil;

import java.util.*;

public class StartJobCommand implements CLICommand {
    @Override
    public String commandName() {
        return "start-job";
    }

    @Override
    public void execute(String args) {
        AppConfig.chordState.clearFractalIdToNodeId();

        Job job = AppConfig.myServentInfo.getFirstJob();
        if (job == null) {
            AppConfig.timestampedErrorPrint("Please provide job's configuration in properties file");
            return;
        }

        AppConfig.timestampedStandardPrint("Starting work for \"" + job.getName() + "\"...");

        int serventCount = AppConfig.chordState.getAllNodeInfo().size();

        // compute number of servents needed to execute the job
        int jobNodesCount = this.calculateNeededNodes(serventCount, job.getInitialPointsCount());
        AppConfig.timestampedStandardPrint("Active nodes for \"" + job.getName() + "\": " + jobNodesCount);

        // compute fractal ids
        List<String> fractalIds = this.computeFractalIds(jobNodesCount, job.getInitialPointsCount());
        AppConfig.timestampedStandardPrint("Fractal IDs for job \"" + job.getName() + "\": " + fractalIds.toString());

        List<String> fractalIdsStack = new LinkedList<>(fractalIds);
        // TODO: all available nodes
        for (Map.Entry<Integer, ServentInfo> entry: AppConfig.chordState.getAllNodeInfo().entrySet()) {
            AppConfig.chordState.registerFractalId(fractalIdsStack.remove(0), entry.getKey());
            if (fractalIdsStack.isEmpty()) {
                break;
            }
        }
        AppConfig.timestampedStandardPrint("Fractal id assigned to node: " + AppConfig.chordState.getFractalIdToNodeIdMap());

        // compute initial job division and send messages
        List<Point> jobPoints = job.getPoints();
        double proportion = job.getProportion();
        int pointsCount = job.getInitialPointsCount();

        if (jobNodesCount < pointsCount) {
            // only one node is executing the job
//            int executorId = AppConfig.myServentInfo.getId();
//            ServentInfo executorServent = AppConfig.myServentInfo;
            ServentInfo executorServent = AppConfig.chordState.getAllNodeInfo().get(0);

            StartJobMessage startJobMessage = new StartJobMessage(
                    AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                    executorServent.getIpAddress(), executorServent.getListenerPort(),
                    fractalIds, jobPoints, job, 0, AppConfig.chordState.getFractalIdToNodeIdMap());
            MessageUtil.sendMessage(startJobMessage);
            return;
        }

        // split work and send to servents
        for (int i = 0; i < jobPoints.size(); i++) {
            List<Point> regionPoints = new ArrayList<>();

            Point startPoint = jobPoints.get(i);
            for (int j = 0; j < jobPoints.size(); j++) {
                if (i == j) {
                    regionPoints.add(startPoint);
                    continue;
                }

                Point other = jobPoints.get(j);
                int newX = (int) (startPoint.getX() + proportion * (other.getX() - startPoint.getX()));
                int newY = (int) (startPoint.getY() + proportion * (other.getY() - startPoint.getY()));
                Point newPoint = new Point(newX, newY);

                regionPoints.add(newPoint);
            }

            List<String> partialFractalIds = new ArrayList<>();
            for (String fractal: fractalIds) {
                if (fractal.startsWith(Integer.toString(i))) {
                    partialFractalIds.add(fractal);
                }
            }

            // todo: FIX THIS ASAP - send over successor table
            int executorId = AppConfig.chordState.getIdForFractalId(partialFractalIds.get(0));
            ServentInfo executorServent = AppConfig.chordState.getAllNodeInfo().get(executorId);
            // send to one node partialFractalIds, regionPoints and job
            StartJobMessage startJobMessage = new StartJobMessage(
                    AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                    executorServent.getIpAddress(), executorServent.getListenerPort(),
                    partialFractalIds, regionPoints, job, 0, AppConfig.chordState.getFractalIdToNodeIdMap());
            MessageUtil.sendMessage(startJobMessage);
        }
    }

    private int calculateNeededNodes(int serventCount, int pointsCount) {
        int i = 1;

        int neededNodes = 1;
        int neededNodesTmp = 1;
        while (neededNodesTmp <= serventCount) {
            neededNodes = neededNodesTmp;
            neededNodesTmp = 1 + (i * (pointsCount - 1));

            i++;
        }

        return neededNodes;
    }

    // TODO: change
    private List<String> computeFractalIds(int nodesCount, int pointsCount) {
        List<String> fractalIds = new ArrayList<>();
        int length = 0;
        String base = "";

        if (nodesCount == 1) {
            fractalIds.add("0");
            return fractalIds;
        }

        while (nodesCount > 0) {
            if (length >= 1) {
                boolean hasLength = false;
                for (String fractalId: fractalIds) {
                    if (fractalId.length() == length) {
                        base = fractalId;
                        fractalIds.remove(fractalId);
                        hasLength = true;
                        break;
                    }
                }
                if (!hasLength) {
                    length++;
                    continue;
                }

                nodesCount++;
            }

            for (int i = 0; i < pointsCount; i++) {
                fractalIds.add(base + i);
            }
            if (length == 0) {
                length++;
            }
            nodesCount -= pointsCount;
        }
        Collections.sort(fractalIds);
        return fractalIds;
    }
}
