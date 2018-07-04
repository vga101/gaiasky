package gaia.cu9.ari.gaiaorbit.interfce.components;

import java.util.Iterator;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.util.CatalogInfo;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnLabel;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnScrollPane;

public class DatasetsComponent extends GuiComponent implements IObserver {

    private VerticalGroup group;
    private float pad = 3 * GlobalConf.SCALE_FACTOR;

    public DatasetsComponent(Skin skin, Stage stage) {
        super(skin, stage);
        EventManager.instance.subscribe(this, Events.ADD_CATALOG_INFO);
    }

    @Override
    public void initialize() {

        group = new VerticalGroup();
        group.space(pad);

        Array<CatalogInfo> cis = CatalogInfo.catalogs;

        if (cis != null) {
            Iterator<CatalogInfo> it = cis.iterator();
            while (it.hasNext()) {
                CatalogInfo ci = it.next();
                addCatalogInfo(ci);
            }
        }

        component = group;

    }

    private void addCatalogInfo(CatalogInfo ci) {
        Table t = new Table();
        t.add(new OwnLabel("Type: " + ci.type.toString(), skin, "hud-subheader")).left().row();
        t.add(new OwnLabel(ci.name, skin)).left().row();
        t.add(new OwnLabel(ci.description, skin)).left().padBottom(pad).row();

        ScrollPane scroll = new OwnScrollPane(t, skin, "minimalist-nobg");
        scroll.setScrollingDisabled(false, true);
        scroll.setForceScroll(false, false);
        scroll.setFadeScrollBars(false);
        scroll.setOverscroll(false, false);
        scroll.setSmoothScrolling(true);
        scroll.setWidth(170 * GlobalConf.SCALE_FACTOR);
        scroll.setHeight(GlobalConf.SCALE_FACTOR > 1 ? 90 : 50);

        group.addActor(scroll);
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case ADD_CATALOG_INFO:
            addCatalogInfo((CatalogInfo) data[0]);
            break;
        default:
            break;
        }

    }

    @Override
    public void dispose() {
        EventManager.instance.removeAllSubscriptions(this);
    }

}
