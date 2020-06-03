package app;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Point implements Serializable {

    private static final long serialVersionUID = -3575643676226110588L;

    private final int x;

    private final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return "Point{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return x == point.x &&
                y == point.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    public boolean inPolygon(List<Point> polygonPoints)
    {
        int i;
        int j;
        boolean result = false;
        for (i = 0, j = polygonPoints.size() - 1; i < polygonPoints.size(); j = i++) {
            if ((polygonPoints.get(i).getY() > this.getY()) != (polygonPoints.get(j).getY() > this.getY()) &&
                    (this.getX() < (polygonPoints.get(j).getX() - polygonPoints.get(i).getX()) * (this.getY() - polygonPoints.get(i).getY()) / (polygonPoints.get(j).getY()-polygonPoints.get(i).getY()) + polygonPoints.get(i).getY())) {
                result = !result;
            }
        }
        return result;
    }

    public static Set<Point> getPointsForPolygon(Set<Point> allPoints, List<Point> polygon)
    {
        return allPoints.stream()
                .filter(point -> point.inPolygon(polygon))
                .collect(Collectors.toSet());
    }
}
