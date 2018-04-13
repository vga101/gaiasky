package gaia.cu9.ari.gaiaorbit.interfce;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnTextArea;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnTextButton;

public class ChooseDatasetWindow extends GenericDialog {

    private Map<Button, String> candidates;
    private OwnTextButton[] cbs;

    public ChooseDatasetWindow(Stage stage, Skin skin) {
        super(txt("gui.dschooser.title"), skin, stage);

        candidates = new HashMap<Button, String>();

        setCancelText(txt("gui.cancel"));
        setAcceptText(txt("gui.ok"));

        // Build
        buildSuper();
    }

    @Override
    protected void build() {
        // Discover datasets, add as buttons
        String assetsLoc = System.getProperty("assets.location") != null ? System.getProperty("assets.location") : "";
        FileHandle dataFolder = Gdx.files.absolute(assetsLoc + File.separatorChar + "data");
        FileHandle[] catalogFiles = dataFolder.list(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().startsWith("catalog-") && pathname.getName().endsWith(".json");
            }
        });
        float pad = 3 * GlobalConf.SCALE_FACTOR;
        float tawidth = 300 * GlobalConf.SCALE_FACTOR;
        float taheight = GlobalConf.SCALE_FACTOR > 1 ? 50 : 35;

        JsonReader reader = new JsonReader();

        // Sort by name
        Comparator<FileHandle> byName = (FileHandle a, FileHandle b) -> a.name().compareTo(b.name());
        Arrays.sort(catalogFiles, byName);

        cbs = new OwnTextButton[catalogFiles.length];
        int i = 0;
        String[] currentSetting = GlobalConf.data.CATALOG_JSON_FILES.split("\\s*,\\s*");
        for (FileHandle catalogFile : catalogFiles) {
            String candidate = catalogFile.path().substring(assetsLoc.length(), catalogFile.path().length());

            String name = null;
            String desc = null;
            try {
                JsonValue val = reader.parse(catalogFile);
                if (val.has("description"))
                    desc = val.get("description").asString();
                if (val.has("name"))
                    name = val.get("name").asString();
            } catch (Exception e) {
            }
            if (desc == null)
                desc = candidate;
            if (name == null)
                name = catalogFile.nameWithoutExtension();

            OwnTextButton cb = new OwnTextButton(name, skin, "toggle-big");

            cb.setChecked(contains(catalogFile.name(), currentSetting));
            cb.addListener(new TextTooltip(candidate, skin));
            content.add(cb).left().top().padRight(pad);

            // Description
            TextArea description = new OwnTextArea(desc, skin.get("regular", TextFieldStyle.class));
            description.setDisabled(true);
            description.setPrefRows(2);
            description.setWidth(tawidth);
            description.setHeight(taheight);
            content.add(description).left().top().padTop(pad).padLeft(pad).row();

            candidates.put(cb, candidate);

            cbs[i++] = cb;

        }
        ButtonGroup<OwnTextButton> bg = new ButtonGroup<OwnTextButton>();
        bg.setMinCheckCount(0);
        bg.setMaxCheckCount(catalogFiles.length);
        bg.add(cbs);

        float maxw = 0;
        for (Button b : cbs) {
            if (b.getWidth() > maxw)
                maxw = b.getWidth();
        }
        for (Button b : cbs)
            b.setWidth(maxw + 10 * GlobalConf.SCALE_FACTOR);

    }

    private boolean contains(String name, String[] list) {
        for (String candidate : list)
            if (candidate.contains(name))
                return true;
        return false;
    }

    @Override
    protected void accept() {
        boolean first = true;
        // Update setting
        GlobalConf.data.CATALOG_JSON_FILES = "";
        for (Button b : cbs) {
            if (b.isChecked()) {
                // Add all selected to list
                if (!first) {
                    GlobalConf.data.CATALOG_JSON_FILES += "," + candidates.get(b);
                } else {
                    GlobalConf.data.CATALOG_JSON_FILES += candidates.get(b);
                    first = false;
                }
            }
        }
        // No change to execute exit event, manually restore cursor to default
        Gdx.graphics.setSystemCursor(SystemCursor.Arrow);
        // Data load can start
        EventManager.instance.post(Events.LOAD_DATA_CMD);
    }

    @Override
    protected void cancel() {
        Gdx.app.exit();
    }

}
