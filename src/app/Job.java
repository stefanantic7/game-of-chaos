package app;

import java.io.Serializable;
import java.util.List;

public class Job implements Serializable {

    private static final long serialVersionUID = -9078236472470580085L;

    private final String name;
    private final double proportion;
    private final int width;
    private final int height;
    private final int initialPointsCount;
    private final List<Point> points;

    public Job(String name, double proportion, int width, int height, List<Point> points) {
        this.name = name;
        this.proportion = proportion;
        this.width = width;
        this.height = height;
        this.points = points;
        this.initialPointsCount = this.points.size();
    }

    public String getName() {
        return name;
    }

    public double getProportion() {
        return proportion;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getInitialPointsCount() {
        return initialPointsCount;
    }

    public List<Point> getPoints() {
        return points;
    }
}
