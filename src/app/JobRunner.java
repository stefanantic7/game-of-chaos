package app;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class JobRunner implements Runnable, Cancellable {

    private final String jobName;
    private final String fractalId;
    private final double proportion;
    private final int width;
    private final int height;
    private final List<Point> startingPoints;
    private Point lastPoint;
    private final Set<Point> computedPoints;
    private final Job originalJob;

    private volatile boolean working;

    public JobRunner(Job originalJob, String jobName, String fractalId, double proportion, int width, int height, List<Point> startingPoints) {
        this.jobName = jobName;
        this.fractalId = fractalId;
        this.proportion = proportion;
        this.width = width;
        this.height = height;
        this.startingPoints = startingPoints;
        this.computedPoints = new HashSet<>();
        this.working = true;

        this.originalJob = originalJob;
    }

    @Override
    public void run() {
        AppConfig.timestampedStandardPrint("Computing points...");
        while (working) {
            this.lastPoint = this.computeNewPoint();
            computedPoints.add(this.lastPoint);

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveImage() {
        Set<Point> resultPoints = this.computedPoints;
        String jobName = this.jobName;
        int width = this.width;
        int height = this.height;
        double proportion = this.proportion;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

        WritableRaster writableRaster = image.getRaster();
        int[] rgb = new int[3];
        rgb[0] = 255;
        rgb[1] = 255;
        rgb[2] = 255;
        for (Point p : resultPoints) {
            writableRaster.setPixel(p.getX(), p.getY(), rgb);
        }
        BufferedImage newImage = new BufferedImage(writableRaster.getWidth(), writableRaster.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        newImage.setData(writableRaster);
        try {
            ImageIO.write(newImage, "PNG", new File("fractals/" + jobName + "_" + proportion + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        this.working = false;
    }

    public String getJobName() {
        return jobName;
    }

    public String getFractalId() {
        return fractalId;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Set<Point> getComputedPoints() { return computedPoints; }

    public boolean isWorking() { return working; }

    public double getProportion() {
        return proportion;
    }

    private Point getRandomStartPoint() {
        Random r = new Random();
        int index = r.nextInt(startingPoints.size());
        return startingPoints.get(index);
    }

    private Point getRandomPoint() {
        Random r = new Random();
        int x = r.nextInt(width + 1);
        int y = r.nextInt(height + 1);
        return new Point(x, y);
    }

    private Point computeNewPoint() {
        if (computedPoints.isEmpty() || this.lastPoint == null) {
            return getRandomPoint();
        }

        Point lastPoint = this.lastPoint;
        Point randomPoint = getRandomStartPoint();
        if (lastPoint == null) {
            System.out.println("last: "+lastPoint);
        }
        if (randomPoint == null) {
            System.out.println("random: "+randomPoint);
        }
        int newX = (int) (randomPoint.getX() + proportion * (lastPoint.getX() - randomPoint.getX()));
        int newY = (int) (randomPoint.getY() + proportion * (lastPoint.getY() - randomPoint.getY()));
        return new Point(newX, newY);
    }

    public Job getOriginalJob() {
        return originalJob;
    }
}
