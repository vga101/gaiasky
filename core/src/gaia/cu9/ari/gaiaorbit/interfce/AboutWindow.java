package gaia.cu9.ari.gaiaorbit.interfce;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.BufferUtils;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.scene2d.CollapsibleWindow;
import gaia.cu9.ari.gaiaorbit.util.scene2d.Link;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnLabel;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnScrollPane;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnTextArea;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnTextButton;

/**
 * The help window with About, Help and System sections.
 * 
 * @author tsagrista
 *
 */
public class AboutWindow extends CollapsibleWindow {
    final private Stage stage;
    final private Skin skin;
    private AboutWindow me;
    private Table table;
    private OwnScrollPane scroll;

    private LabelStyle linkStyle;

    private List<OwnScrollPane> scrolls;
    private List<Actor> textareas;

    public AboutWindow(Stage stg, Skin sk) {
        super(txt("gui.help.help") + " - " + GlobalConf.APPLICATION_NAME + " v" + GlobalConf.version.version, sk);

        this.stage = stg;
        this.skin = sk;
        this.me = this;
        this.linkStyle = skin.get("link", LabelStyle.class);

        float tawidth = 440 * GlobalConf.SCALE_FACTOR;
        float taheight = 250 * GlobalConf.SCALE_FACTOR;
        float taheight_s = 60 * GlobalConf.SCALE_FACTOR;
        float tabwidth = 60 * GlobalConf.SCALE_FACTOR;
        float pad = 5 * GlobalConf.SCALE_FACTOR;

        scrolls = new ArrayList<OwnScrollPane>(5);
        textareas = new ArrayList<Actor>();

        /** TABLE and SCROLL **/
        table = new Table(skin);

        // Create the tab buttons
        HorizontalGroup group = new HorizontalGroup();
        group.align(Align.left);

        final Button tab1 = new OwnTextButton(txt("gui.help.help"), skin, "toggle-big");
        tab1.pad(pad);
        tab1.setWidth(tabwidth);
        final Button tab2 = new OwnTextButton(txt("gui.help.about"), skin, "toggle-big");
        tab2.pad(pad);
        tab2.setWidth(tabwidth);
        final Button tab3 = new OwnTextButton(txt("gui.help.system"), skin, "toggle-big");
        tab3.pad(pad);
        tab3.setWidth(tabwidth);

        group.addActor(tab1);
        group.addActor(tab2);
        group.addActor(tab3);
        table.add(group).align(Align.left).padLeft(pad);
        table.row();

        // Create the tab content. Just using images here for simplicity.
        Stack content = new Stack();

        /** CONTENT 1 - HELP **/
        final Table content1 = new Table(skin);
        content1.align(Align.top);
        Image gaiasky = new Image(getSpriteDrawable(Gdx.files.internal("img/gaiaskylogo.png")));

        // User manual
        Label usermantitle = new OwnLabel(txt("gui.help.usermanual"), skin, "ui-12");
        Label usermantxt = new OwnLabel(txt("gui.help.help1"), skin, "ui-11");
        Link usermanlink = new Link(GlobalConf.WEBPAGE, linkStyle, GlobalConf.WEBPAGE);

        // Wiki
        Label wikititle = new OwnLabel("Wiki", skin, "ui-12");
        Label wikitxt = new OwnLabel(txt("gui.help.help2"), skin, "ui-11");
        Link wikilink = new Link(GlobalConf.WIKI, linkStyle, GlobalConf.WIKI);

        // Readme
        Label readmetitle = new OwnLabel(txt("gui.help.readme"), skin, "ui-12");
        FileHandle readmefile = Gdx.files.internal("README.md");
        if (!readmefile.exists()) {
            readmefile = Gdx.files.internal("../README.md");
        }
        String readmestr = readmefile.readString();
        int lines = GlobalResources.countOccurrences(readmestr, '\n');
        TextArea readme = new TextArea(readmestr, skin);
        readme.setDisabled(true);
        readme.setPrefRows(lines);
        textareas.add(readme);

        OwnScrollPane readmescroll = new OwnScrollPane(readme, skin, "default-nobg");
        readmescroll.setWidth(tawidth);
        readmescroll.setHeight(taheight);
        readmescroll.setForceScroll(false, true);
        readmescroll.setSmoothScrolling(true);
        readmescroll.setFadeScrollBars(false);

        scrolls.add(readmescroll);

        // Add all to content
        content1.add(gaiasky).colspan(2);
        content1.row();
        content1.add(usermantitle).align(Align.left).padRight(pad * 2);
        content1.add(usermantxt).align(Align.left);
        content1.row();
        content1.add(new OwnLabel("", skin, "ui-11"));
        content1.add(usermanlink).align(Align.left);
        content1.row();
        content1.add(wikititle).align(Align.left).padRight(pad * 2);
        content1.add(wikitxt).align(Align.left);
        content1.row();
        content1.add(new OwnLabel("", skin, "ui-11"));
        content1.add(wikilink).align(Align.left);
        content1.row();
        content1.add(readmetitle).colspan(2).align(Align.left);
        content1.row();
        content1.add(readmescroll).colspan(2).expand().pad(pad * 2, 0, pad * 2, 0).align(Align.center);

        /** CONTENT 2 - ABOUT **/
        final Table content2 = new Table(skin);
        content2.align(Align.top);

        // Intro
        TextArea intro = new OwnTextArea(txt("gui.help.gscredits", GlobalConf.version.version), skin.get("msg-11", TextFieldStyle.class));
        intro.setDisabled(true);
        intro.setPrefRows(3);
        intro.setWidth(tawidth);
        textareas.add(intro);

        // Home page
        Label homepagetitle = new OwnLabel(txt("gui.help.homepage"), skin, "ui-12");
        Link homepage = new Link(GlobalConf.WEBPAGE, linkStyle, GlobalConf.WEBPAGE);

        // Author
        Label authortitle = new OwnLabel(txt("gui.help.author"), skin, "ui-12");

        VerticalGroup author = new VerticalGroup();
        author.align(Align.left);
        Label authorname = new OwnLabel("Toni Sagristà Sellés", skin, "ui-11");
        Link authormail = new Link("tsagrista@ari.uni-heidelberg.de", linkStyle, "mailto:tsagrista@ari.uni-heidelberg.de");
        Link authorpage = new Link("www.tonisagrista.com", linkStyle, "http://tonisagrista.com");
        author.addActor(authorname);
        author.addActor(authormail);
        author.addActor(authorpage);

        // Contributor
        Label contribtitle = new OwnLabel(txt("gui.help.contributors"), skin, "ui-12");

        VerticalGroup contrib = new VerticalGroup();
        contrib.align(Align.left);
        Label contribname = new OwnLabel("Apl. Prof. Dr. Stefan Jordan", skin, "ui-11");
        Link contribmail = new Link("jordan@ari.uni-heidelberg.de", linkStyle, "mailto:jordan@ari.uni-heidelberg.de");
        contrib.addActor(contribname);
        contrib.addActor(contribmail);

        // License
        HorizontalGroup licenseh = new HorizontalGroup();
        licenseh.space(pad * 2);
        Image license = new Image(getSpriteDrawable(Gdx.files.internal("img/license.png")));

        VerticalGroup licensev = new VerticalGroup();
        TextArea licensetext = new OwnTextArea(txt("gui.help.license"), skin.get("msg-11", TextFieldStyle.class));
        licensetext.setDisabled(true);
        licensetext.setPrefRows(3);
        licensetext.setWidth(tawidth / 2f);
        Link licenselink = new Link("https://www.gnu.org/licenses/lgpl.html", linkStyle, "https://www.gnu.org/licenses/lgpl.html");

        licensev.addActor(licensetext);
        licensev.addActor(licenselink);

        licenseh.addActor(license);
        licenseh.addActor(licensev);

        // Thanks

        HorizontalGroup thanks = new HorizontalGroup();
        thanks.space(pad * 2);
        Container thanksc = new Container(thanks);
        thanksc.setBackground(skin.getDrawable("bg-clear"));

        Image zah = new Image(getSpriteDrawable(Gdx.files.internal("img/zah.png")));
        Image dlr = new Image(getSpriteDrawable(Gdx.files.internal("img/dlr.png")));
        Image bwt = new Image(getSpriteDrawable(Gdx.files.internal("img/bwt.png")));
        Image dpac = new Image(getSpriteDrawable(Gdx.files.internal("img/dpac.png")));

        thanks.addActor(zah);
        thanks.addActor(dlr);
        thanks.addActor(bwt);
        thanks.addActor(dpac);

        content2.add(intro).colspan(2).align(Align.left).padTop(pad * 2);
        content2.row();
        content2.add(homepagetitle).align(Align.topLeft).padRight(pad * 2);
        content2.add(homepage).align(Align.left);
        content2.row();
        content2.add(authortitle).align(Align.topLeft).padRight(pad * 2).padTop(pad);
        content2.add(author).align(Align.left).padTop(pad);
        content2.row();
        content2.add(contribtitle).align(Align.topLeft).padRight(pad * 2).padTop(pad);
        content2.add(contrib).align(Align.left).padTop(pad);
        content2.row();
        content2.add(licenseh).colspan(2).align(Align.center).padTop(pad * 4);
        content2.row();
        content2.add(thanksc).colspan(2).align(Align.center).padTop(pad * 8);

        /** CONTENT 3 - SYSTEM **/
        final Table content3 = new Table(skin);
        content3.align(Align.top);

        // Build info
        Label buildinfo = new OwnLabel(txt("gui.help.buildinfo"), skin, "help-title");

        Label versiontitle = new OwnLabel(txt("gui.help.version", GlobalConf.APPLICATION_NAME), skin, "ui-12");
        Label version = new OwnLabel(GlobalConf.version.version, skin, "ui-11");

        Label revisiontitle = new OwnLabel(txt("gui.help.buildnumber"), skin, "ui-12");
        Label revision = new OwnLabel(GlobalConf.version.build, skin, "ui-11");

        Label timetitle = new OwnLabel(txt("gui.help.buildtime"), skin, "ui-12");
        Label time = new OwnLabel(GlobalConf.version.buildtime, skin, "ui-11");

        Label systemtitle = new OwnLabel(txt("gui.help.buildsys"), skin, "ui-12");
        TextArea system = new OwnTextArea(GlobalConf.version.system, skin.get("msg-11", TextFieldStyle.class));
        system.setDisabled(true);
        system.setPrefRows(3);
        system.setWidth(tawidth * 2f / 3f);
        textareas.add(system);

        Label buildertitle = new OwnLabel(txt("gui.help.builder"), skin, "ui-12");
        Label builder = new OwnLabel(GlobalConf.version.builder, skin, "ui-11");

        // Java info
        Label javainfo = new OwnLabel(txt("gui.help.javainfo"), skin, "help-title");

        Label javaversiontitle = new OwnLabel(txt("gui.help.javaversion"), skin, "ui-12");
        Label javaversion = new OwnLabel(System.getProperty("java.version"), skin, "ui-11");

        Label javaruntimetitle = new OwnLabel(txt("gui.help.javaname"), skin, "ui-12");
        Label javaruntime = new OwnLabel(System.getProperty("java.runtime.name"), skin, "ui-11");

        Label javavmnametitle = new OwnLabel(txt("gui.help.javavmname"), skin, "ui-12");
        Label javavmname = new OwnLabel(System.getProperty("java.vm.name"), skin, "ui-11");

        Label javavmversiontitle = new OwnLabel(txt("gui.help.javavmversion"), skin, "ui-12");
        Label javavmversion = new OwnLabel(System.getProperty("java.vm.version"), skin, "ui-11");

        Label javavmvendortitle = new OwnLabel(txt("gui.help.javavmvendor"), skin, "ui-12");
        Label javavmvendor = new OwnLabel(System.getProperty("java.vm.vendor"), skin, "ui-11");

        TextButton memoryinfobutton = new OwnTextButton(txt("gui.help.meminfo"), skin, "default");
        memoryinfobutton.setName("memoryinfo");
        memoryinfobutton.setSize(150 * GlobalConf.SCALE_FACTOR, 20 * GlobalConf.SCALE_FACTOR);
        memoryinfobutton.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.DISPLAY_MEM_INFO_WINDOW, stage, skin);
                    return true;
                }

                return false;
            }

        });

        // OpenGL info
        Label glinfo = new OwnLabel(txt("gui.help.openglinfo"), skin, "help-title");

        Label glversiontitle = new OwnLabel(txt("gui.help.openglversion"), skin, "ui-12");
        Label glversion = new OwnLabel(Gdx.gl.glGetString(GL20.GL_VERSION), skin, "ui-11");

        Label glslversiontitle = new OwnLabel(txt("gui.help.glslversion"), skin, "ui-12");
        Label glslversion = new OwnLabel(Gdx.gl.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION), skin, "ui-11");

        Label glextensionstitle = new OwnLabel(txt("gui.help.glextensions"), skin, "ui-12");
        String glextensionsstr = Gdx.gl.glGetString(GL20.GL_EXTENSIONS).replace(' ', '\r');
        lines = GlobalResources.countOccurrences(glextensionsstr, '\r') + 1;
        IntBuffer buf = BufferUtils.newIntBuffer(16);
        Gdx.gl.glGetIntegerv(Gdx.graphics.getGL20().GL_MAX_TEXTURE_SIZE, buf);
        int maxSize = buf.get(0);
        TextArea glextensions = new TextArea("Max texture size: " + maxSize + "\r" + glextensionsstr, skin);
        glextensions.setDisabled(true);
        glextensions.setPrefRows(lines);

        textareas.add(glextensions);

        OwnScrollPane glextensionsscroll = new OwnScrollPane(glextensions, skin, "default-nobg");
        glextensionsscroll.setWidth(tawidth / 1.7f);
        glextensionsscroll.setHeight(taheight_s);
        glextensionsscroll.setForceScroll(false, true);
        glextensionsscroll.setSmoothScrolling(true);
        glextensionsscroll.setFadeScrollBars(false);
        scrolls.add(glextensionsscroll);

        content3.add(buildinfo).colspan(2).align(Align.left).padTop(pad * 3);
        content3.row();
        content3.add(versiontitle).align(Align.topLeft).padRight(pad * 2);
        content3.add(version).align(Align.left);
        content3.row();
        content3.add(revisiontitle).align(Align.topLeft).padRight(pad * 2);
        content3.add(revision).align(Align.left);
        content3.row();
        content3.add(timetitle).align(Align.topLeft).padRight(pad * 2);
        content3.add(time).align(Align.left);
        content3.row();
        content3.add(buildertitle).align(Align.topLeft).padRight(pad * 2);
        content3.add(builder).align(Align.left).padBottom(pad * 3);
        content3.row();
        content3.add(systemtitle).align(Align.topLeft).padRight(pad * 2);
        content3.add(system).align(Align.left);
        content3.row();

        content3.add(javainfo).colspan(2).align(Align.left).padTop(pad * 2);
        content3.row();
        content3.add(javaversiontitle).align(Align.topLeft).padRight(pad * 2);
        content3.add(javaversion).align(Align.left);
        content3.row();
        content3.add(javaruntimetitle).align(Align.topLeft).padRight(pad * 2);
        content3.add(javaruntime).align(Align.left);
        content3.row();
        content3.add(javavmnametitle).align(Align.topLeft).padRight(pad * 2);
        content3.add(javavmname).align(Align.left);
        content3.row();
        content3.add(javavmversiontitle).align(Align.topLeft).padRight(pad * 2);
        content3.add(javavmversion).align(Align.left);
        content3.row();
        content3.add(javavmvendortitle).align(Align.topLeft).padRight(pad * 2);
        content3.add(javavmvendor).align(Align.left).padBottom(pad * 2);
        content3.row();
        content3.add(memoryinfobutton).colspan(2).align(Align.left).padBottom(pad * 3);
        content3.row();
        content3.add(glinfo).colspan(2).align(Align.left).padTop(pad * 2);
        content3.row();
        content3.add(glversiontitle).align(Align.topLeft).padRight(pad * 2);
        content3.add(glversion).align(Align.left);
        content3.row();
        content3.add(glslversiontitle).align(Align.topLeft).padRight(pad * 2);
        content3.add(glslversion).align(Align.left);
        content3.row();
        content3.add(glextensionstitle).align(Align.topLeft).padRight(pad * 2);
        content3.add(glextensionsscroll).align(Align.left);

        /** ADD ALL CONTENT **/
        content.addActor(content1);
        content.addActor(content2);
        content.addActor(content3);

        table.add(content).expand().fill();

        // Listen to changes in the tab button checked states
        // Set visibility of the tab content to match the checked state
        ChangeListener tab_listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                content1.setVisible(tab1.isChecked());
                content2.setVisible(tab2.isChecked());
                content3.setVisible(tab3.isChecked());
            }
        };
        tab1.addListener(tab_listener);
        tab2.addListener(tab_listener);
        tab3.addListener(tab_listener);

        // Let only one tab button be checked at a time
        ButtonGroup tabs = new ButtonGroup();
        tabs.setMinCheckCount(1);
        tabs.setMaxCheckCount(1);
        tabs.add(tab1);
        tabs.add(tab2);
        tabs.add(tab3);

        /** BUTTONS **/
        HorizontalGroup buttonGroup = new HorizontalGroup();
        TextButton close = new OwnTextButton(txt("gui.close"), skin, "default");
        close.setName("close");
        close.setSize(70 * GlobalConf.SCALE_FACTOR, 20 * GlobalConf.SCALE_FACTOR);
        close.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    me.hide();
                    return true;
                }

                return false;
            }

        });
        buttonGroup.addActor(close);

        add(table).pad(pad);
        row();
        add(buttonGroup).pad(pad).bottom().right();
        getTitleTable().align(Align.left);

        pack();

        this.setPosition(Math.round(stage.getWidth() / 2f - this.getWidth() / 2f), Math.round(stage.getHeight() / 2f - this.getHeight() / 2f));

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

    }

    public void hide() {
        if (stage.getActors().contains(me, true))
            me.remove();
    }

    public void display() {
        if (!stage.getActors().contains(me, true))
            stage.addActor(this);
    }

    protected static String txt(String key) {
        return I18n.bundle.get(key);
    }

    protected static String txt(String key, Object... args) {
        return I18n.bundle.format(key, args);
    }

    private SpriteDrawable getSpriteDrawable(FileHandle fh) {
        Texture tex = new Texture(fh);
        return new SpriteDrawable(new Sprite(tex));
    }

}
