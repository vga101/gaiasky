package gaia.cu9.ari.gaiaorbit.desktop.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.python.core.PyCode;
import org.python.core.PySyntaxError;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.script.JythonFactory;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.scene2d.CollapsibleWindow;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnLabel;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnScrollPane;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnTextButton;

/**
 * The run script window, which allows the user to choose a script to run.
 * @author tsagrista
 *
 */
public class RunScriptWindow extends CollapsibleWindow {

    final private Stage stage;
    private RunScriptWindow me;
    private Table table;

    private PyCode code;

    private TextButton run;

    private Label outConsole;
    private Color originalColor;

    private List<FileHandle> scripts = null;
    private FileHandle selectedScript = null;

    public RunScriptWindow(Stage stg, Skin skin) {
        super(txt("gui.script.title"), skin);

        this.stage = stg;
        this.me = this;

        float pad = 5 * GlobalConf.SCALE_FACTOR;

        table = new Table(skin);

        // Choose script
        FileHandle scriptFolder1 = Gdx.files.internal(GlobalConf.program.SCRIPT_LOCATION);
        FileHandle scriptFolder2 = Gdx.files.absolute(SysUtils.getDefaultScriptDir().getPath());

        scripts = new ArrayList<FileHandle>();

        if (scriptFolder1.exists())
            scripts = listRec(scriptFolder1, scripts);

        if (scriptFolder2.exists())
            scripts = listRec(scriptFolder2, scripts);

        Label choosetitle = new OwnLabel(txt("gui.script.choose"), skin, "help-title");
        table.add(choosetitle).align(Align.left).padTop(pad * 2);
        table.row();

        final com.badlogic.gdx.scenes.scene2d.ui.List<FileHandle> scriptsList = new com.badlogic.gdx.scenes.scene2d.ui.List<FileHandle>(skin, "normal");
        scriptsList.setName("scripts list");

        Array<String> names = new Array<String>();
        for (FileHandle fh : scripts)
            names.add(fh.name());

        scriptsList.setItems(names);
        scriptsList.pack();//
        scriptsList.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    ChangeEvent ce = (ChangeEvent) event;
                    Actor actor = ce.getTarget();
                    final String name = ((com.badlogic.gdx.scenes.scene2d.ui.List<String>) actor).getSelected();
                    if (name != null) {
                        for (FileHandle fh : scripts) {
                            if (fh.name().equals(name)) {
                                selectedScript = fh;
                                break;
                            }
                        }
                        if (selectedScript != null) {
                            File choice = selectedScript.file();
                            try {
                                code = JythonFactory.getInstance().compileJythonScript(choice);
                                outConsole.setText(txt("gui.script.ready"));
                                outConsole.setColor(0, 1, 0, 1);
                                run.setDisabled(false);
                                me.pack();
                            } catch (PySyntaxError e1) {
                                outConsole.setText(txt("gui.script.error", e1.type, e1.value));
                                outConsole.setColor(1, 0, 0, 1);
                                run.setDisabled(true);
                                me.pack();
                            } catch (Exception e2) {
                                outConsole.setText(txt("gui.script.error2", e2.getMessage()));
                                outConsole.setColor(1, 0, 0, 1);
                                run.setDisabled(true);
                                me.pack();
                            }
                        }
                    }
                    return true;
                }
                return false;
            }
        });
        ScrollPane scriptsScroll = new OwnScrollPane(scriptsList, skin, "minimalist");
        scriptsScroll.setName("scripts list scroll");
        scriptsScroll.setFadeScrollBars(false);
        scriptsScroll.setScrollingDisabled(true, false);

        scriptsScroll.setHeight(200 * GlobalConf.SCALE_FACTOR);
        scriptsScroll.setWidth(300 * GlobalConf.SCALE_FACTOR);

        table.add(scriptsScroll).align(Align.center).pad(pad);
        table.row();

        // Compile results
        outConsole = new OwnLabel("...", skin);
        originalColor = new Color(outConsole.getColor());
        table.add(outConsole).align(Align.center).pad(pad);
        table.row();

        /** BUTTONS **/
        HorizontalGroup buttonGroup = new HorizontalGroup();
        buttonGroup.space(pad);
        TextButton cancel = new OwnTextButton(txt("gui.cancel"), skin, "default");
        cancel.setName("cancel");
        cancel.setSize(70 * GlobalConf.SCALE_FACTOR, 20 * GlobalConf.SCALE_FACTOR);
        cancel.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    me.hide();
                    return true;
                }

                return false;
            }

        });
        run = new OwnTextButton(txt("gui.script.run"), skin, "default");
        run.setName("run");
        run.setSize(70 * GlobalConf.SCALE_FACTOR, 20 * GlobalConf.SCALE_FACTOR);
        run.setDisabled(true);
        run.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                boolean async = true;
                if (event instanceof ChangeEvent) {
                    me.hide();
                    if (code != null) {
                        EventManager.instance.post(Events.RUN_SCRIPT_PYCODE, code, GlobalConf.program.SCRIPT_LOCATION, async);
                    }
                    return true;
                }

                return false;
            }

        });
        buttonGroup.addActor(cancel);
        buttonGroup.addActor(run);

        add(table).pad(pad);
        row();
        add(buttonGroup).pad(pad).bottom().right();
        getTitleTable().align(Align.left);

        pack();

        this.setPosition(Math.round(stage.getWidth() / 2f - this.getWidth() / 2f), Math.round(stage.getHeight() / 2f - this.getHeight() / 2f));
    }

    List<FileHandle> listRec(FileHandle f, List<FileHandle> l) {
        if (f.exists()) {
            if (f.isDirectory()) {
                FileHandle[] partial = f.list();
                for (FileHandle fh : partial) {
                    l = listRec(fh, l);
                }

            } else {
                if (f.name().endsWith(".py")) {
                    l.add(f);
                }
            }
        }

        return l;
    }

    public void hide() {
        if (stage.getActors().contains(me, true))
            me.remove();
    }

    public void display() {
        if (!stage.getActors().contains(me, true))
            stage.addActor(this);

        outConsole.setText("...");
        outConsole.setColor(originalColor);
    }

    protected static String txt(String key) {
        return I18n.bundle.get(key);
    }

    protected static String txt(String key, Object... args) {
        return I18n.bundle.format(key, args);
    }

}
