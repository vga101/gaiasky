package gaia.cu9.ari.gaiaorbit.interfce;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.Disableable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.scene2d.CollapsibleWindow;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnScrollPane;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnTextButton;

public abstract class GenericDialog extends CollapsibleWindow {

    final protected Stage stage;
    final protected Skin skin;
    protected GenericDialog me;
    protected Table content;
    protected float pad;
    private String acceptText = null, cancelText = null;
    private Actor previousKeyboardFocus = null;

    protected Array<OwnScrollPane> scrolls;

    public GenericDialog(String title, Skin skin, Stage stage) {
        super(title, skin);
        this.skin = skin;
        this.stage = stage;
        this.me = this;
        this.content = new Table(skin);
        this.scrolls = new Array<OwnScrollPane>(5);
    }

    protected void setAcceptText(String acceptText) {
        this.acceptText = acceptText;
    }

    protected void setCancelText(String cancelText) {
        this.cancelText = cancelText;
    }

    public void buildSuper() {
        pad = 5 * GlobalConf.SCALE_FACTOR;

        build();

        /** BUTTONS **/
        HorizontalGroup buttonGroup = new HorizontalGroup();
        buttonGroup.pad(pad);
        buttonGroup.space(pad);

        if (acceptText != null) {
            TextButton accept = new OwnTextButton(acceptText, skin, "default");
            accept.setName("accept");
            accept.setSize(130 * GlobalConf.SCALE_FACTOR, 20 * GlobalConf.SCALE_FACTOR);
            accept.addListener(new EventListener() {

                @Override
                public boolean handle(Event event) {
                    if (event instanceof ChangeEvent) {
                        accept();
                        me.hide();
                        return true;
                    }
                    return false;
                }

            });
            buttonGroup.addActor(accept);
        }
        if (cancelText != null) {
            TextButton close = new OwnTextButton(cancelText, skin, "default");
            close.setName("cancel");
            close.setSize(70 * GlobalConf.SCALE_FACTOR, 20 * GlobalConf.SCALE_FACTOR);
            close.addListener(new EventListener() {
                @Override
                public boolean handle(Event event) {
                    if (event instanceof ChangeEvent) {
                        cancel();
                        me.hide();
                        return true;
                    }

                    return false;
                }

            });
            buttonGroup.addActor(close);
        }

        add(content).pad(pad);
        row();
        add(buttonGroup).pad(pad).bottom().right();
        getTitleTable().align(Align.left);

        pack();

        // Add keys for ESC and ENTER
        me.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof InputEvent) {
                    InputEvent ievent = (InputEvent) event;
                    if (ievent.getType() == Type.keyUp) {
                        int key = ievent.getKeyCode();
                        switch (key) {
                        case Keys.ESCAPE:
                            // Exit
                            cancel();
                            me.hide();
                            return true;
                        case Keys.ENTER:
                            // Exit
                            accept();
                            me.hide();
                            return true;
                        default:
                            // Nothing
                            break;
                        }
                    }
                }
                return false;
            }
        });

        /** CAPTURE SCROLL FOCUS **/
        stage.addListener(new EventListener() {

            @Override
            public boolean handle(Event event) {
                if (event instanceof InputEvent) {
                    InputEvent ie = (InputEvent) event;

                    if (ie.getType() == Type.mouseMoved) {
                        for (OwnScrollPane scroll : scrolls) {
                            if (ie.getTarget().isDescendantOf(scroll)) {
                                stage.setScrollFocus(scroll);
                            }
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        // Set position
        setPosition(Math.round(stage.getWidth() / 2f - this.getWidth() / 2f), Math.round(stage.getHeight() / 2f - this.getHeight() / 2f));

        // Modal
        setModal(true);
    }

    /**
     * Build the content here
     */
    protected abstract void build();

    /**
     * The accept function, if any
     */
    protected abstract void accept();

    /**
     * The cancel function, if any
     */
    protected abstract void cancel();

    /** Hides the dialog **/
    public void hide() {
        if (stage.getActors().contains(me, true)) {
            me.remove();
            stage.setKeyboardFocus(previousKeyboardFocus);
        }
    }

    /** Displays the dialog **/
    public void display() {
        if (!stage.getActors().contains(me, true)) {
            previousKeyboardFocus = stage.getKeyboardFocus();
            stage.addActor(this);
            stage.setKeyboardFocus(me);
        }
    }

    /**
     * Sets the enabled property on the given components
     * @param enabled
     * @param components
     */
    protected void enableComponents(boolean enabled, Disableable... components) {
        for (Disableable c : components) {
            if (c != null)
                c.setDisabled(!enabled);
        }
    }

    protected static String txt(String key) {
        return I18n.bundle.get(key);
    }

    protected static String txt(String key, Object... args) {
        return I18n.bundle.format(key, args);
    }

}
