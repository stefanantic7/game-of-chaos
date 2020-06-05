package cli.command;

import app.*;
import servent.message.StartJobMessage;
import servent.message.util.MessageUtil;

import java.util.*;

public class StartJobCommand implements CLICommand {
    @Override
    public String commandName() {
        return "start";
    }

    @Override
    public void execute(String jobName, Scanner scanner) {
        if (jobName == null || jobName.equals("")) {
            Job newJob = this.createJobByUser(scanner);
            jobName = newJob.getName();
        }
//        AppConfig.chordState.clearFractalIdToNodeId();

        Job job = AppConfig.myServentInfo.findJob(jobName);
        if (job == null) {
            AppConfig.timestampedErrorPrint("Job \"" + jobName + "\" does not exists");
            return;
        }

        AppConfig.timestampedStandardPrint("Starting work for \"" + job.getName() + "\"...");

        startJob(job, new HashSet<>());
    }

    @Override
    public void execute(String args) {
        throw new Error("Wrong method call");
    }

    public static void startJob(Job job, Set<Point> precomputedPoints) {
        int serventCount = AppConfig.chordState.getAllNodeInfo().size();
        // WARNING: this should be on the top(here) because we will remove elements of the precomputedPoints set
        int precomputedPointsCount = precomputedPoints.size();

        int neededNodes = calculateNeededNodes(serventCount, job.getInitialPointsCount());
        AppConfig.timestampedStandardPrint("Active nodes for \"" + job.getName() + "\": " + neededNodes);

        List<String> fractalIds = computeFractalIds(neededNodes, job.getInitialPointsCount());
        AppConfig.timestampedStandardPrint("Fractal IDs for job \"" + job.getName() + "\": " + fractalIds.toString());

        List<String> fractalIdsStack = new LinkedList<>(fractalIds);

        for (Map.Entry<Integer, ServentInfo> entry: AppConfig.chordState.getAllNodeInfo().entrySet()) {
            AppConfig.chordState.registerFractalId(fractalIdsStack.remove(0), entry.getKey());
            if (fractalIdsStack.isEmpty()) {
                break;
            }
        }
        AppConfig.timestampedStandardPrint("Fractal id assigned to node: " + AppConfig.chordState.getFractalIdToNodeIdMap());

        List<Point> jobPoints = job.getPoints();
        double proportion = job.getProportion();
        int pointsCount = job.getInitialPointsCount();

        if (neededNodes < pointsCount) {
            ServentInfo serventRunner = AppConfig.chordState.getAllNodeInfo().get(0);

            StartJobMessage startJobMessage = new StartJobMessage(
                    AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                    serventRunner.getIpAddress(), serventRunner.getListenerPort(),
                    fractalIds, jobPoints, job, 0, AppConfig.chordState.getFractalIdToNodeIdMap(), precomputedPoints);
            MessageUtil.sendMessage(startJobMessage);
            return;
        }

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

            int runnerId = AppConfig.chordState.getIdForFractalId(partialFractalIds.get(0));
            ServentInfo serventRunner = AppConfig.chordState.getAllNodeInfo().get(runnerId);

            Set<Point> forwardedPoints = Boundary.takePoints(
                    precomputedPoints, precomputedPointsCount,
                    regionPoints, job.getProportion());

            StartJobMessage startJobMessage = new StartJobMessage(
                    AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                    serventRunner.getIpAddress(), serventRunner.getListenerPort(),
                    partialFractalIds, regionPoints, job, 0, AppConfig.chordState.getFractalIdToNodeIdMap(), forwardedPoints);
            MessageUtil.sendMessage(startJobMessage);
        }
    }

    private static int calculateNeededNodes(int serventCount, int pointsCount) {
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


    private static List<String> computeFractalIds(int nodesCount, int pointsCount) {
        List<String> fractalIds = new ArrayList<>();
        if (nodesCount < pointsCount) {
            fractalIds.add("0");
            return fractalIds;
        }

        while (fractalIds.size() < nodesCount) {
            addNewLevelOfFractalIds(fractalIds, pointsCount);
        }
        return fractalIds;
    }

    private static void addNewLevelOfFractalIds(List<String> fractalIds, int numberOfPoints) {
        String base = "";
        if (fractalIds.size() > 0) {
            base = fractalIds.remove(0);
        }

        for (int id = 0; id < numberOfPoints; id++) {
            String newFractalId = base+id;
            fractalIds.add(newFractalId);
        }
    }

    private Job createJobByUser(Scanner scanner) {
        System.out.println("Enter name of the job:");
        String name = scanner.nextLine();
        while (AppConfig.myServentInfo.findJob(name) != null) {
            AppConfig.timestampedErrorPrint("Job with this name already exists");
            System.out.println("Enter name of the job:");
            name = scanner.nextLine();
        }

        System.out.println("Enter coordinates (example: `(200,500);(500,200);(500,700)`):");
        String[] pointsCoordinates = scanner.nextLine().split(";");


        double proportion = 0;
        while (true) {
            try {
                System.out.println("Enter proportion:");
                proportion = Double.parseDouble(scanner.nextLine());
                break;
            } catch (NumberFormatException numberFormatException) {
                AppConfig.timestampedErrorPrint("Wrong number format, try again!");
            }
        }

        int width = 0;
        while (true) {
            try {
                System.out.println("Enter width:");
                width = Integer.parseInt(scanner.nextLine());
                break;
            } catch (NumberFormatException numberFormatException) {
                AppConfig.timestampedErrorPrint("Wrong number format, try again!");
            }
        }

        int height = 0;
        while (true) {
            try {
                System.out.println("Enter height:");
                height = Integer.parseInt(scanner.nextLine());
                break;
            } catch (NumberFormatException numberFormatException) {
                AppConfig.timestampedErrorPrint("Wrong number format, try again!");
            }
        }

        List<Point> points = new ArrayList<>();
        for (String coordinates: pointsCoordinates) {
            String[] xy = coordinates.substring(1, coordinates.length() - 1).split(",");
            points.add(new Point(Integer.parseInt(xy[0]), Integer.parseInt(xy[1])));
        }

        Job job = new Job(name, proportion, width, height, points);
        AppConfig.myServentInfo.addJob(job);

        return job;
    }
}
