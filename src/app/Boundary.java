package app;

import java.util.List;

public class Boundary {

    /**
     * Return true if the given point is contained inside the boundary.
     * See: http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
     * @param test The point to check
     * @return true if the point is inside the boundary, false otherwise
     *
     */
    public static boolean contains(Point test, List<Point> points) {
        int i;
        int j;
        boolean result = false;
        for (i = 0, j = points.size() - 1; i < points.size(); j = i++) {
            if ((points.get(i).getY() > test.getY()) != (points.get(j).getY() > test.getY()) &&
                    (test.getX() < (points.get(j).getX() - points.get(i).getX()) * (test.getY() - points.get(i).getY()) / (points.get(j).getY()-points.get(i).getY()) + points.get(i).getY())) {
                result = !result;
            }
        }
        return result;
    }

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
}
