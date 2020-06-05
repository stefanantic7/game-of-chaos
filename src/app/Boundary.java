package app;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Boundary {

    /**
     * Return true if the given point is contained inside the boundary.
     * See: http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
     * @param target The point to check
     * @param polygon Points of the polygon
     * @return true if the point is inside the boundary, false otherwise
     *
     */
    public static boolean insidePolygon(Point target, List<Point> polygon) {
        boolean result = false;
        for (int i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
            if ((polygon.get(i).getY() > target.getY()) != (polygon.get(j).getY() > target.getY()) &&
                    (target.getX() <
                            (polygon.get(j).getX() - polygon.get(i).getX()) *
                                    (target.getY() - polygon.get(i).getY()) /
                                    (polygon.get(j).getY() - polygon.get(i).getY()) +
                                    polygon.get(i).getX())) {
                result = !result;
            }
        }
        return result;
    }

    private static Set<Point> takePointsInPolygon(Set<Point> points, List<Point> polygonPoints) {
        Set<Point> myPoints = new HashSet<>();
        for (Point point: new HashSet<>(points)) {
            if (Boundary.insidePolygon(point, polygonPoints)) {
                myPoints.add(point);
                points.remove(point);
            }
        }

        return myPoints;
    }

    private static Set<Point> takeRandomPoints(Set<Point> points, int allPointsCount, int divideForServents) {
        Set<Point> myPoints = new HashSet<>();

        int pointsCount = Math.min(allPointsCount, allPointsCount/divideForServents);
        for (Point point : new HashSet<>(points)) {
            myPoints.add(point);
            points.remove(point);
            if (myPoints.size() > pointsCount) {
                break;
            }
        }

        return myPoints;
    }

    public static Set<Point> takePoints(Set<Point> points, int allPointsCount, List<Point> polygonPoints, double proportion) {
        if (proportion > 0.5) {
            if (AppConfig.chordState.getAllNodeInfo().size() < polygonPoints.size()) {
                return takeRandomPoints(points, allPointsCount, 1);
            }
            return takeRandomPoints(points, allPointsCount, polygonPoints.size());
        }

        return takePointsInPolygon(points, polygonPoints);
    }
}
