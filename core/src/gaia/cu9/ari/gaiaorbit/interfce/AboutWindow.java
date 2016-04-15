package gaia.cu9.ari.gaiaorbit.interfce;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
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

import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.scene2d.CollapsibleWindow;
import gaia.cu9.ari.gaiaorbit.util.scene2d.Link;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnLabel;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnScrollPane;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnTextArea;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnTextButton;

public class AboutWindow extends CollapsibleWindow {
    final private Stage stage;
    private AboutWindow me;
    private Table table;
    private OwnScrollPane scroll;

    private LabelStyle linkStyle;

    private List<OwnScrollPane> scrolls;
    private List<Actor> textareas;

    public AboutWindow(Stage stg, Skin skin) {
        super(txt("gui.help.help") + " - " + GlobalConf.APPLICATION_NAME + " v" + GlobalConf.version.version, skin);

        this.stage = stg;
        this.me = this;
        this.linkStyle = skin.get("link", LabelStyle.class);

        float tawidth = 440 * GlobalConf.SCALE_FACTOR;
        float taheight = 250 * GlobalConf.SCALE_FACTOR;
        float tabwidth = 60 * GlobalConf.SCALE_FACTOR;
        float pad = 5 * GlobalConf.SCALE_FACTOR;

        scrolls = new ArrayList<OwnScrollPane>(5);
        textareas = new ArrayList<Actor>();

        /** TABLE and SCROLL **/
        table = new Table(skin);
        //        scroll = new OwnScrollPane(table, skin, "minimalist-nobg");
        //        scroll.setFadeScrollBars(false);
        //        scroll.setScrollingDisabled(true, false);
        //        scroll.setOverscroll(false, false);
        //        scroll.setSmoothScrolling(true);
        //        scrolls.add(scroll);

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

        thanks.addActor(zah);
        thanks.addActor(dlr);
        thanks.addActor(bwt);

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
        final Image content3im = new Image(skin.newDrawable("white", 0, 0, 1, 1));
        content3.add(content3im);

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
        TextButton close = new OwnTextButton(I18n.bundle.get("gui.close"), skin, "default");
        close.setName("close");
        close.setSize(70, 20);
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

        //float width = this.getWidth();
        //for (Actor ta : textareas) {
        // ta.setWidth(width - 20 * GlobalConf.SCALE_FACTOR);
        //}

        //layout();
        //pack();

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
                            return true;
                        }
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
