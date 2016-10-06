package gaia.cu9.ari.gaiaorbit.util.math;

import com.badlogic.gdx.math.collision.Ray;

public class Intersectord {

    /** Quick check whether the given {@link Ray} and {@link BoundingBoxd} intersect.
     * 
     * @param ray The ray
     * @param center The center of the bounding box
     * @param dimensions The dimensions (width, height and depth) of the bounding box
     * @return Whether the ray and the bounding box intersect. */
    public static boolean intersectRayBoundsFast(Rayd ray, Vector3d center, Vector3d dimensions) {
        final double divX = 1f / ray.direction.x;
        final double divY = 1f / ray.direction.y;
        final double divZ = 1f / ray.direction.z;

        double minx = ((center.x - dimensions.x * .5f) - ray.origin.x) * divX;
        double maxx = ((center.x + dimensions.x * .5f) - ray.origin.x) * divX;
        if (minx > maxx) {
            final double t = minx;
            minx = maxx;
            maxx = t;
        }

        double miny = ((center.y - dimensions.y * .5f) - ray.origin.y) * divY;
        double maxy = ((center.y + dimensions.y * .5f) - ray.origin.y) * divY;
        if (miny > maxy) {
            final double t = miny;
            miny = maxy;
            maxy = t;
        }

        double minz = ((center.z - dimensions.z * .5f) - ray.origin.z) * divZ;
        double maxz = ((center.z + dimensions.z * .5f) - ray.origin.z) * divZ;
        if (minz > maxz) {
            final double t = minz;
            minz = maxz;
            maxz = t;
        }

        double min = Math.max(Math.max(minx, miny), minz);
        double max = Math.min(Math.min(maxx, maxy), maxz);

        return max >= 0 && max >= min;
    }

    public static Vector3d[] lineSphereIntersections(Vector3d linePoint0, Vector3d linePoint1, Vector3d circleCenter, double circleRadius) {
        // http://www.codeproject.com/Articles/19799/Simple-Ray-Tracing-in-C-Part-II-Triangles-Intersec

        double cx = circleCenter.x;
        double cy = circleCenter.y;
        double cz = circleCenter.z;

        double px = linePoint0.x;
        double py = linePoint0.y;
        double pz = linePoint0.z;

        double vx = linePoint1.x - px;
        double vy = linePoint1.y - py;
        double vz = linePoint1.z - pz;

        double A = vx * vx + vy * vy + vz * vz;
        double B = 2.0 * (px * vx + py * vy + pz * vz - vx * cx - vy * cy - vz * cz);
        double C = px * px - 2 * px * cx + cx * cx + py * py - 2 * py * cy + cy * cy + pz * pz - 2 * pz * cz + cz * cz - circleRadius * circleRadius;

        // discriminant
        double D = B * B - 4 * A * C;

        if (D < 0) {
            return new Vector3d[0];
        }

        double t1 = (-B - Math.sqrt(D)) / (2.0 * A);

        Vector3d solution1 = new Vector3d(linePoint0.x * (1 - t1) + t1 * linePoint1.x, linePoint0.y * (1 - t1) + t1 * linePoint1.y, linePoint0.z * (1 - t1) + t1 * linePoint1.z);
        if (D == 0) {
            return new Vector3d[] { solution1 };
        }

        double t2 = (-B + Math.sqrt(D)) / (2.0 * A);
        Vector3d solution2 = new Vector3d(linePoint0.x * (1 - t2) + t2 * linePoint1.x, linePoint0.y * (1 - t2) + t2 * linePoint1.y, linePoint0.z * (1 - t2) + t2 * linePoint1.z);

        // prefer a solution that's on the line segment itself

        if (Math.abs(t1 - 0.5) < Math.abs(t2 - 0.5)) {
            return new Vector3d[] { solution1, solution2 };
        }

        return new Vector3d[] { solution2, solution1 };
    }

}
