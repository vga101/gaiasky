package gaia.cu9.ari.gaiaorbit.interfce;

import java.nio.IntBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
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
public class AboutWindow extends GenericDialog {

    private LabelStyle linkStyle;

    public AboutWindow(Stage stage, Skin skin) {
        super(txt("gui.help.help") + " - v" + GlobalConf.version.version + " - " + txt("gui.build", GlobalConf.version.build), skin, stage);
        this.linkStyle = skin.get("link", LabelStyle.class);

        setCancelText(txt("gui.close"));

        // Build
        buildSuper();

    }

    @Override
    protected void build() {
        float tawidth = 440 * GlobalConf.SCALE_FACTOR;
        float taheight = 250 * GlobalConf.SCALE_FACTOR;
        float taheight_s = 60 * GlobalConf.SCALE_FACTOR;
        float tabwidth = 60 * GlobalConf.SCALE_FACTOR;

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
        content.add(group).align(Align.left).padLeft(pad);
        content.row();

        // Create the tab content. Just using images here for simplicity.
        Stack tabContent = new Stack();

        /** CONTENT 1 - HELP **/
        final Table contentHelp = new Table(skin);
        contentHelp.align(Align.top);
        Image gaiasky = new Image(getSpriteDrawable(Gdx.files.internal("img/gaiaskylogo.png")));

        // User manual
        Label usermantitle = new OwnLabel(txt("gui.help.usermanual"), skin, "ui-12");
        Label usermantxt = new OwnLabel(txt("gui.help.help1"), skin, "ui-11");
        Link usermanlink = new Link(GlobalConf.WEBPAGE, linkStyle, GlobalConf.WEBPAGE);

        // Wiki
        Label wikititle = new OwnLabel("Docs", skin, "ui-12");
        Label wikitxt = new OwnLabel(txt("gui.help.help2"), skin, "ui-11");
        Link wikilink = new Link(GlobalConf.DOCUMENTATION, linkStyle, GlobalConf.DOCUMENTATION);

        // Readme
        Label readmetitle = new OwnLabel(txt("gui.help.readme"), skin, "ui-12");
        FileHandle readmefile = Gdx.files.internal("README.md");
        if (!readmefile.exists()) {
            readmefile = Gdx.files.internal("../README.md");
        }
        String readmestr = readmefile.readString();
        int lines = GlobalResources.countOccurrences(readmestr, '\n');
        TextArea readme = new TextArea(readmestr, skin, "no-disabled");
        readme.setDisabled(true);
        readme.setPrefRows(lines);
        readme.clearListeners();

        OwnScrollPane readmescroll = new OwnScrollPane(readme, skin, "minimalist-nobg");
        readmescroll.setWidth(tawidth);
        readmescroll.setHeight(taheight);
        readmescroll.setForceScroll(false, true);
        readmescroll.setSmoothScrolling(true);
        readmescroll.setFadeScrollBars(false);

        scrolls.add(readmescroll);

        // Add all to content
        contentHelp.add(gaiasky).colspan(2);
        contentHelp.row();
        contentHelp.add(usermantitle).align(Align.left).padRight(pad * 2);
        contentHelp.add(usermantxt).align(Align.left);
        contentHelp.row();
        contentHelp.add(new OwnLabel("", skin, "ui-11"));
        contentHelp.add(usermanlink).align(Align.left);
        contentHelp.row();
        contentHelp.add(wikititle).align(Align.left).padRight(pad * 2);
        contentHelp.add(wikitxt).align(Align.left);
        contentHelp.row();
        contentHelp.add(new OwnLabel("", skin, "ui-11"));
        contentHelp.add(wikilink).align(Align.left);
        contentHelp.row();
        contentHelp.add(readmetitle).colspan(2).align(Align.left);
        contentHelp.row();
        contentHelp.add(readmescroll).colspan(2).expand().pad(pad * 2, 0, pad * 2, 0).align(Align.center);

        /** CONTENT 2 - ABOUT **/
        final Table contentAbout = new Table(skin);
        contentAbout.align(Align.top);

        // Intro
        TextArea intro = new OwnTextArea(txt("gui.help.gscredits", GlobalConf.version.version), skin.get("msg-11", TextFieldStyle.class));
        intro.setDisabled(true);
        intro.setPrefRows(3);
        intro.setWidth(tawidth);

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
        Link licenselink = new Link("https://www.gnu.org/licenses/gpl.html", linkStyle, "https://www.gnu.org/licenses/gpl.html");

        licensev.addActor(licensetext);
        licensev.addActor(licenselink);

        licenseh.addActor(license);
        licenseh.addActor(licensev);

        // Thanks

        HorizontalGroup thanks = new HorizontalGroup();
        thanks.space(pad * 2);
        Container<Actor> thanksc = new Container<Actor>(thanks);
        thanksc.setBackground(skin.getDrawable("bg-clear"));

        Image zah = new Image(getSpriteDrawable(Gdx.files.internal("img/zah.png")));
        Image dlr = new Image(getSpriteDrawable(Gdx.files.internal("img/dlr.png")));
        Image bwt = new Image(getSpriteDrawable(Gdx.files.internal("img/bwt.png")));
        Image dpac = new Image(getSpriteDrawable(Gdx.files.internal("img/dpac.png")));

        thanks.addActor(zah);
        thanks.addActor(dlr);
        thanks.addActor(bwt);
        thanks.addActor(dpac);

        contentAbout.add(intro).colspan(2).align(Align.left).padTop(pad * 2);
        contentAbout.row();
        contentAbout.add(homepagetitle).align(Align.topLeft).padRight(pad * 2);
        contentAbout.add(homepage).align(Align.left);
        contentAbout.row();
        contentAbout.add(authortitle).align(Align.topLeft).padRight(pad * 2).padTop(pad);
        contentAbout.add(author).align(Align.left).padTop(pad);
        contentAbout.row();
        contentAbout.add(contribtitle).align(Align.topLeft).padRight(pad * 2).padTop(pad);
        contentAbout.add(contrib).align(Align.left).padTop(pad);
        contentAbout.row();
        contentAbout.add(licenseh).colspan(2).align(Align.center).padTop(pad * 4);
        contentAbout.row();
        contentAbout.add(thanksc).colspan(2).align(Align.center).padTop(pad * 8);

        /** CONTENT 3 - SYSTEM **/
        final Table contentSystem = new Table(skin);
        contentSystem.align(Align.top);

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
        Gdx.gl.glGetIntegerv(GL20.GL_MAX_TEXTURE_SIZE, buf);
        int maxSize = buf.get(0);
        TextArea glextensions = new TextArea("Max texture size: " + maxSize + "\r" + glextensionsstr, skin, "no-disabled");
        glextensions.setDisabled(true);
        glextensions.setPrefRows(lines);
        glextensions.clearListeners();

        OwnScrollPane glextensionsscroll = new OwnScrollPane(glextensions, skin, "minimalist-nobg");
        glextensionsscroll.setWidth(tawidth / 1.7f);
        glextensionsscroll.setHeight(taheight_s);
        glextensionsscroll.setForceScroll(false, true);
        glextensionsscroll.setSmoothScrolling(true);
        glextensionsscroll.setFadeScrollBars(false);
        scrolls.add(glextensionsscroll);

        contentSystem.add(buildinfo).colspan(2).align(Align.left).padTop(pad * 3);
        contentSystem.row();
        contentSystem.add(versiontitle).align(Align.topLeft).padRight(pad * 2);
        contentSystem.add(version).align(Align.left);
        contentSystem.row();
        contentSystem.add(revisiontitle).align(Align.topLeft).padRight(pad * 2);
        contentSystem.add(revision).align(Align.left);
        contentSystem.row();
        contentSystem.add(timetitle).align(Align.topLeft).padRight(pad * 2);
        contentSystem.add(time).align(Align.left);
        contentSystem.row();
        contentSystem.add(buildertitle).align(Align.topLeft).padRight(pad * 2);
        contentSystem.add(builder).align(Align.left).padBottom(pad * 3);
        contentSystem.row();
        contentSystem.add(systemtitle).align(Align.topLeft).padRight(pad * 2);
        contentSystem.add(system).align(Align.left);
        contentSystem.row();

        contentSystem.add(javainfo).colspan(2).align(Align.left).padTop(pad * 2);
        contentSystem.row();
        contentSystem.add(javaversiontitle).align(Align.topLeft).padRight(pad * 2);
        contentSystem.add(javaversion).align(Align.left);
        contentSystem.row();
        contentSystem.add(javaruntimetitle).align(Align.topLeft).padRight(pad * 2);
        contentSystem.add(javaruntime).align(Align.left);
        contentSystem.row();
        contentSystem.add(javavmnametitle).align(Align.topLeft).padRight(pad * 2);
        contentSystem.add(javavmname).align(Align.left);
        contentSystem.row();
        contentSystem.add(javavmversiontitle).align(Align.topLeft).padRight(pad * 2);
        contentSystem.add(javavmversion).align(Align.left);
        contentSystem.row();
        contentSystem.add(javavmvendortitle).align(Align.topLeft).padRight(pad * 2);
        contentSystem.add(javavmvendor).align(Align.left).padBottom(pad * 2);
        contentSystem.row();
        contentSystem.add(memoryinfobutton).colspan(2).align(Align.left).padBottom(pad * 3);
        contentSystem.row();
        contentSystem.add(glinfo).colspan(2).align(Align.left).padTop(pad * 2);
        contentSystem.row();
        contentSystem.add(glversiontitle).align(Align.topLeft).padRight(pad * 2);
        contentSystem.add(glversion).align(Align.left);
        contentSystem.row();
        contentSystem.add(glslversiontitle).align(Align.topLeft).padRight(pad * 2);
        contentSystem.add(glslversion).align(Align.left);
        contentSystem.row();
        contentSystem.add(glextensionstitle).align(Align.topLeft).padRight(pad * 2);
        contentSystem.add(glextensionsscroll).align(Align.left);

        /** ADD ALL CONTENT **/
        tabContent.addActor(contentHelp);
        tabContent.addActor(contentAbout);
        tabContent.addActor(contentSystem);

        content.add(tabContent).expand().fill();

        // Listen to changes in the tab button checked states
        // Set visibility of the tab content to match the checked state
        ChangeListener tab_listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                contentHelp.setVisible(tab1.isChecked());
                contentAbout.setVisible(tab2.isChecked());
                contentSystem.setVisible(tab3.isChecked());
            }
        };
        tab1.addListener(tab_listener);
        tab2.addListener(tab_listener);
        tab3.addListener(tab_listener);

        // Let only one tab button be checked at a time
        ButtonGroup<Button> tabs = new ButtonGroup<Button>();
        tabs.setMinCheckCount(1);
        tabs.setMaxCheckCount(1);
        tabs.add(tab1);
        tabs.add(tab2);
        tabs.add(tab3);

    }

    @Override
    protected void accept() {
    }

    @Override
    protected void cancel() {
    }

    private SpriteDrawable getSpriteDrawable(FileHandle fh) {
        Texture tex = new Texture(fh);
        return new SpriteDrawable(new Sprite(tex));
    }

}
