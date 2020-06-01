package app;

import java.io.Serializable;
import java.util.Objects;

public class JobDetails implements Serializable {

    private static final long serialVersionUID = -4212618203350234896L;

    private final String name;
    private final double proportion;
    private final int width;
    private final int height;

    public JobDetails(String name, double proportion, int width, int height) {
        this.name = name;
        this.proportion = proportion;
        this.width = width;
        this.height = height;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobDetails that = (JobDetails) o;
        return Double.compare(that.proportion, proportion) == 0 &&
                width == that.width &&
                height == that.height &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, proportion, width, height);
    }
}
