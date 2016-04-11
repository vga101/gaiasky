package gaia.cu9.ari.gaiaorbit.interfce;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.Planet;
import gaia.cu9.ari.gaiaorbit.scenegraph.Star;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.format.INumberFormat;
import gaia.cu9.ari.gaiaorbit.util.format.NumberFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnLabel;

/**
 * Part of the user interface which holds the information on the current focus object and on the camera.
 * @author tsagrista
 *
 */
public class FocusInfoInterface extends Table implements IObserver {

    protected OwnLabel focusName, focusId, focusRA, focusDEC, focusAngle, focusDist, focusAppMag, focusAbsMag, focusRadius, focusLonLat;
    protected OwnLabel camName, camVel, camPos, lonLatLabel;

    private Table focusInfo, cameraInfo, moreInfo;
    private Skin skin;
    Vector3d pos;

    INumberFormat nf, sf;

    private INetworkChecker daemon;

    float pad5, pad10;

    public FocusInfoInterface(Skin skin) {
        super(skin);
        this.setBackground("table-bg");
        this.skin = skin;

        nf = NumberFormatFactory.getFormatter("##0.###");
        sf = NumberFormatFactory.getFormatter("##0.###E0");

        pad10 = 10 * GlobalConf.SCALE_FACTOR;
        pad5 = 5 * GlobalConf.SCALE_FACTOR;

        focusInfo = new Table();
        focusInfo.pad(pad5);
        cameraInfo = new Table();
        cameraInfo.pad(pad5);
        moreInfo = new Table();

        focusName = new OwnLabel("", skin, "hud-header");
        focusId = new OwnLabel("", skin, "hud");
        focusRA = new OwnLabel("", skin, "hud");
        focusDEC = new OwnLabel("", skin, "hud");
        focusAppMag = new OwnLabel("", skin, "hud");
        focusAbsMag = new OwnLabel("", skin, "hud");
        focusAngle = new OwnLabel("", skin, "hud");
        focusDist = new OwnLabel("", skin, "hud");
        focusRadius = new OwnLabel("", skin, "hud");
        focusLonLat = new OwnLabel("", skin, "hud");

        camName = new OwnLabel(I18n.bundle.get("gui.camera"), skin, "hud-header");
        camVel = new OwnLabel("", skin, "hud");
        camPos = new OwnLabel("", skin, "hud");

        lonLatLabel = new OwnLabel("Lat/Lon", skin, "hud-big");

        float w = 100 * GlobalConf.SCALE_FACTOR;
        focusId.setWidth(w);
        focusRA.setWidth(w);
        focusDEC.setWidth(w);
        focusAngle.setWidth(w);
        focusDist.setWidth(w);
        camVel.setWidth(w);

        focusInfo.add(focusName).left().colspan(2);
        focusInfo.row();
        focusInfo.add(new OwnLabel("ID", skin, "hud-big")).left();
        focusInfo.add(focusId).left().padLeft(pad10);
        focusInfo.row();
        focusInfo.add(new OwnLabel(txt("gui.focusinfo.alpha"), skin, "hud-big")).left();
        focusInfo.add(focusRA).left().padLeft(pad10);
        focusInfo.row();
        focusInfo.add(new OwnLabel(txt("gui.focusinfo.delta"), skin, "hud-big")).left();
        focusInfo.add(focusDEC).left().padLeft(pad10);
        focusInfo.row();
        focusInfo.add(new OwnLabel(txt("gui.focusinfo.appmag"), skin, "hud-big")).left();
        focusInfo.add(focusAppMag).left().padLeft(pad10);
        focusInfo.row();
        focusInfo.add(new OwnLabel(txt("gui.focusinfo.absmag"), skin, "hud-big")).left();
        focusInfo.add(focusAbsMag).left().padLeft(pad10);
        focusInfo.row();
        focusInfo.add(new OwnLabel(txt("gui.focusinfo.angle"), skin, "hud-big")).left();
        focusInfo.add(focusAngle).left().padLeft(pad10);
        focusInfo.row();
        focusInfo.add(new OwnLabel(txt("gui.focusinfo.distance"), skin, "hud-big")).left();
        focusInfo.add(focusDist).left().padLeft(pad10);
        focusInfo.row();
        focusInfo.add(new OwnLabel(txt("gui.focusinfo.radius"), skin, "hud-big")).left();
        focusInfo.add(focusRadius).left().padLeft(pad10);
        focusInfo.row();
        focusInfo.add(lonLatLabel).left();
        focusInfo.add(focusLonLat).left().padLeft(pad10);
        focusInfo.row();
        //        focusInfo.add(new OwnLabel(txt("gui.focusinfo.moreinfo"), skin, "hud-big")).left();
        focusInfo.add(moreInfo).center().colspan(2).padBottom(pad5).padTop(pad10);

        cameraInfo.add(camName).left().colspan(2);
        cameraInfo.row();
        cameraInfo.add(new OwnLabel(txt("gui.camera.vel"), skin, "hud-big")).left();
        cameraInfo.add(camVel).left().padLeft(pad10);
        cameraInfo.row();
        cameraInfo.add(camPos).left().colspan(2);

        add(focusInfo);
        row();
        add(cameraInfo);
        pack();

        daemon = NetworkCheckerManager.getNewtorkChecker();
        daemon.setParameters(moreInfo, skin, pad10);
        daemon.start();

        pos = new Vector3d();
        EventManager.instance.subscribe(this, Events.FOCUS_CHANGED, Events.FOCUS_INFO_UPDATED, Events.CAMERA_MOTION_UPDATED, Events.CAMERA_MODE_CMD, Events.LON_LAT_UPDATED);
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case FOCUS_CHANGED:
            CelestialBody cb = null;
            if (data[0] instanceof String) {
                cb = (CelestialBody) GaiaSky.instance.sg.getNode((String) data[0]);
            } else {
                cb = (CelestialBody) data[0];
            }

            String id = "";
            if (cb instanceof Star) {
                Star s = (Star) cb;
                if (s.hip >= 0) {
                    id = "HIP " + s.hip;
                } else if (s.tycho >= 0) {
                    id = "TYC " + s.tycho;
                } else {
                    id = "" + s.id;
                }
            } else {
                id = "-";
            }

            if (cb instanceof Planet) {
                lonLatLabel.setVisible(true);
                focusLonLat.setVisible(true);
                focusLonLat.setText("-/-");
            } else {
                lonLatLabel.setVisible(false);
                focusLonLat.setVisible(false);
            }

            focusId.setText(id);

            // Update focus information
            String objectName = cb.name;

            focusName.setText(objectName);
            if (cb.posSph != null && cb.posSph.len() > 0f) {
                focusRA.setText(nf.format(cb.posSph.x) + "°");
                focusDEC.setText(nf.format(cb.posSph.y) + "°");
            } else {
                Coordinates.cartesianToSpherical(cb.pos, pos);

                focusRA.setText(nf.format(cb.pos.x % 360) + "°");
                focusDEC.setText(nf.format(cb.pos.y % 360) + "°");
            }

            Float appmag = cb.appmag;

            if (appmag != null) {
                focusAppMag.setText(nf.format(appmag));
            } else {
                focusAppMag.setText("-");
            }
            Float absmag = cb.absmag;

            if (absmag != null) {
                focusAbsMag.setText(nf.format(absmag));
            } else {
                focusAbsMag.setText("-");
            }
            focusRadius.setText(sf.format(cb.getRadius() * Constants.U_TO_KM) + " km");

            // Update more info table
            if (!daemon.executing()) {
                moreInfo.clear();
                daemon.setFocus(cb);
                daemon.doNotify();
            }

            break;
        case FOCUS_INFO_UPDATED:
            focusAngle.setText(sf.format(Math.toDegrees((float) data[1]) % 360) + "°");
            Object[] dist = GlobalResources.floatToDistanceString((float) data[0]);
            focusDist.setText(sf.format(Math.max(0d, (float) dist[0])) + " " + dist[1]);
            break;
        case CAMERA_MOTION_UPDATED:
            Vector3d campos = (Vector3d) data[0];
            camPos.setText("X: " + nf.format(campos.x * Constants.U_TO_PC) + " pc\nY: " + nf.format(campos.y * Constants.U_TO_PC) + " pc\nZ: " + nf.format(campos.z * Constants.U_TO_PC) + " pc");
            camVel.setText(sf.format((double) data[1]) + " km/h");
            break;
        case CAMERA_MODE_CMD:
            // Update camera mode selection
            CameraMode mode = (CameraMode) data[0];
            if (mode.equals(CameraMode.Focus)) {
                displayFocusInfo();
            } else {
                hideFocusInfo();
            }
            break;
        case LON_LAT_UPDATED:
            Double lon = (Double) data[0];
            Double lat = (Double) data[1];
            focusLonLat.setText(nf.format(lat) + "°/" + nf.format(lon) + "°");
        }
    }

    private String txt(String key) {
        return I18n.bundle.get(key);
    }

    public void displayFocusInfo() {
        this.clearChildren();
        add(focusInfo);
        row();
        add(cameraInfo);
        pack();
    }

    public void hideFocusInfo() {
        this.clearChildren();
        add(cameraInfo);
        pack();
    }

}
