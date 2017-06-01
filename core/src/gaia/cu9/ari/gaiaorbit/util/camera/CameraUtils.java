package gaia.cu9.ari.gaiaorbit.util.camera;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.Planet;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

/**
 * Contains camera utilities/
 * 
 * @author tsagrista
 *
 */
public class CameraUtils {

    public static boolean projectLonLat(Planet p, ICamera camera, double lon, double lat, Vector3 point, Vector3 pos,
	    Vector3d in, Vector3d out, Matrix4 localTransform, Vector2 xy) {
	lon = Math.toRadians(lon - 90);
	lat = Math.toRadians(lat);
	in.set(lon, lat, p.getRadius());
	Coordinates.sphericalToCartesian(in, out);
	out.put(point);

	p.setToLocalTransform(1, localTransform, false);

	point.mul(localTransform);
	p.transform.getTranslationf(pos);
	// Here we get the absolute position of [lon|lat] in cartesian
	// coordinates
	point.add(pos);

	camera.getCamera().project(point);

	xy.x = point.x;
	xy.y = point.y;
	return true;
    }

    public static boolean getLonLat(Planet p, ICamera camera, int sx, int sy, Vector3 v0, Vector3 v1, Vector3 vec,
	    Vector3d in, Vector3d out, Matrix4 localTransformInv, double[] lonlat) {
	p.transform.getTranslationf(vec);

	v0.set(sx, sy, 0f);
	v1.set(sx, sy, 0.5f);
	camera.getCamera().unproject(v0);
	camera.getCamera().unproject(v1);
	Ray ray = new Ray(v0, v1.sub(v0));
	Vector3 intersection = new Vector3();
	boolean inter = Intersector.intersectRaySphere(ray, vec, p.getRadius(), intersection);

	if (inter) {
	    // We found an intersection point
	    p.setToLocalTransform(1, localTransformInv, false);
	    localTransformInv.inv();
	    intersection.mul(localTransformInv);

	    in.set(intersection);
	    Coordinates.cartesianToSpherical(in, out);

	    lonlat[0] = (Math.toDegrees(out.x) + 90) % 360;
	    lonlat[1] = Math.toDegrees(out.y);
	    return true;
	} else {
	    return false;
	}
    }

}
