package gaia.cu9.ari.gaiaorbit.desktop.gui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.JsonValue;

import gaia.cu9.ari.gaiaorbit.desktop.GaiaSkyDesktop;
import gaia.cu9.ari.gaiaorbit.desktop.gui.swing.callback.Callback;
import gaia.cu9.ari.gaiaorbit.desktop.gui.swing.callback.CallbackTask;
import gaia.cu9.ari.gaiaorbit.desktop.gui.swing.jsplash.GuiUtility;
import gaia.cu9.ari.gaiaorbit.desktop.gui.swing.jsplash.JSplashLabel;
import gaia.cu9.ari.gaiaorbit.desktop.gui.swing.version.VersionChecker;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.interfce.KeyBindings;
import gaia.cu9.ari.gaiaorbit.interfce.KeyBindings.ProgramAction;
import gaia.cu9.ari.gaiaorbit.interfce.TextUtils;
import gaia.cu9.ari.gaiaorbit.util.ConfInit;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import net.miginfocom.swing.MigLayout;
import slider.RangeSlider;

/**
 * The configuration dialog to set the resolution, the screen mode, etc.
 * 
 * @author Toni Sagrista
 *
 */
public class ConfigDialog extends I18nJFrame {
    private static long fiveDaysMs = 5 * 24 * 60 * 60 * 1000;

    private static ConfigDialog singleton = null;

    public static void initialise(GaiaSkyDesktop gsd, boolean startup) {
        if (singleton == null) {
            singleton = new ConfigDialog(gsd, startup);
        } else {
            singleton.setVisible(true);
            singleton.toFront();
        }
    }

    JFrame frame;
    JLabel checkLabel;
    JPanel checkPanel;
    Color darkgreen, darkred, transparent;
    JButton cancelButton, okButton;
    String vislistdata;
    JTree visualisationsTree;

    private DecimalFormat nf3;

    public ConfigDialog(final GaiaSkyDesktop gsd, boolean startup) {
        super(startup ? GlobalConf.getFullApplicationName() : txt("gui.settings"));

        FontUtilities.setFontScale(GlobalConf.SCALE_FACTOR);

        nf3 = new DecimalFormat("#.000");

        // Initialize
        initialize(gsd, startup);

        if (startup) {
            /** SPLASH IMAGE **/
            URL url = this.getClass().getResource("/img/splash/splash-s.jpg");

            JSplashLabel label = new JSplashLabel(url, txt("gui.build", GlobalConf.version.build) + " - " + txt("gui.version", GlobalConf.version.version), null, Color.lightGray);
            JPanel imagePanel = new JPanel(new GridLayout(1, 1, 0, 0));
            imagePanel.add(label);
            imagePanel.setBackground(Color.black);
            frame.add(imagePanel, BorderLayout.NORTH);
        } else {
            JPanel imagePanel = new JPanel(new GridLayout(1, 1, 0, 0));
            imagePanel.setBorder(new EmptyBorder(5, 5, 5, 5));
            JLabel buildtext = new JLabel("<html><font color='white'>" + txt("gui.build", GlobalConf.version.build) + " - " + txt("gui.version", GlobalConf.version.version) + "</font></html>");

            buildtext.setHorizontalAlignment(JLabel.CENTER);
            imagePanel.add(buildtext);
            imagePanel.setBackground(Color.black);
            frame.add(imagePanel, BorderLayout.NORTH);
        }

        frame.pack();
        GuiUtility.centerOnScreen(frame);
        frame.setVisible(true);
        frame.setEnabled(true);
        frame.setAutoRequestFocus(true);

        // ESC closes the frame
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "closeTheDialog");
        getRootPane().getActionMap().put("closeTheDialog", new AbstractAction() {

            private static final long serialVersionUID = 8360999630557775801L;

            @Override
            public void actionPerformed(ActionEvent e) {
                // This should be replaced by the action you want to
                // perform
                cancelButton.doClick();
            }
        });

        // Window close hook
        WindowListener exitListener = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                singleton = null;
            }
        };
        frame.addWindowListener(exitListener);

        // Request focus
        frame.getRootPane().setDefaultButton(okButton);
        okButton.requestFocus();
    }

    private void initialize(final GaiaSkyDesktop gsd, final boolean startup) {
        frame = this;
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setResizable(false);

        darkgreen = new Color(0, .5f, 0);
        darkred = new Color(.8f, 0, 0);
        transparent = new Color(0f, 0f, 0f, 0f);

        // Build content
        frame.setLayout(new BorderLayout(0, 0));

        /** BODY **/
        JPanel body = new JPanel(new MigLayout("", "[grow,fill][]", ""));

        /** VERSION CHECK **/
        checkPanel = new JPanel(new MigLayout("", "[][]", "[]4[]"));
        checkLabel = new JLabel("");
        checkPanel.add(checkLabel);
        if (GlobalConf.program.LAST_CHECKED == null || GlobalConf.program.LAST_VERSION.isEmpty() || new Date().getTime() - GlobalConf.program.LAST_CHECKED.getTime() > fiveDaysMs) {
            // Check!
            checkLabel.setText(txt("gui.newversion.checking"));
            getCheckVersionThread().start();
        } else {
            // Inform latest
            newVersionCheck(GlobalConf.program.LAST_VERSION);

        }

        /** TABBED PANEL **/

        final JXTabbedPane tabbedPane = new JXTabbedPane(JTabbedPane.LEFT);

        /**
         * ====== GRAPHICS TAB =======
         */

        /** RESOLUTION **/
        JPanel mode = new JPanel(new MigLayout("fillx", "[grow,fill][grow,fill]", ""));
        mode.setBorder(new TitledBorder(txt("gui.resolutionmode")));

        // Full screen mode resolutions
        DisplayMode[] modes = LwjglApplicationConfiguration.getDisplayModes();
        final JComboBox<DisplayMode> fullScreenResolutions = new JComboBox<DisplayMode>(modes);

        DisplayMode selectedMode = null;
        for (DisplayMode dm : modes) {
            if (dm.width == GlobalConf.screen.FULLSCREEN_WIDTH && dm.height == GlobalConf.screen.FULLSCREEN_HEIGHT) {
                selectedMode = dm;
                break;
            }
        }
        if (selectedMode != null)
            fullScreenResolutions.setSelectedItem(selectedMode);

        // Get current resolution
        DisplayMode nativeMode = LwjglApplicationConfiguration.getDesktopDisplayMode();

        // Windowed mode resolutions
        JPanel windowedResolutions = new JPanel(new MigLayout("", "[][grow,fill][][grow,fill]", "[][]4[][]"));
        final JSpinner widthField = new JSpinner(new SpinnerNumberModel(MathUtils.clamp(GlobalConf.screen.SCREEN_WIDTH, 100, nativeMode.width), 100, nativeMode.width, 1));
        final JSpinner heightField = new JSpinner(new SpinnerNumberModel(MathUtils.clamp(GlobalConf.screen.SCREEN_HEIGHT, 100, nativeMode.height), 100, nativeMode.height, 1));
        final JCheckBox resizable = new JCheckBox("Resizable", GlobalConf.screen.RESIZABLE);
        final JLabel widthLabel = new JLabel(txt("gui.width") + ":");
        final JLabel heightLabel = new JLabel(txt("gui.height") + ":");

        windowedResolutions.add(widthLabel);
        windowedResolutions.add(widthField);
        windowedResolutions.add(heightLabel);
        windowedResolutions.add(heightField, "wrap");
        windowedResolutions.add(resizable, "span");

        // Radio buttons
        final JRadioButton fullscreen = new JRadioButton(txt("gui.fullscreen"));
        fullscreen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GlobalConf.screen.FULLSCREEN = fullscreen.isSelected();
                selectFullscreen(fullscreen.isSelected(), widthField, heightField, fullScreenResolutions, resizable, widthLabel, heightLabel);
            }
        });
        fullscreen.setSelected(GlobalConf.screen.FULLSCREEN);

        final JRadioButton windowed = new JRadioButton(txt("gui.windowed"));
        windowed.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GlobalConf.screen.FULLSCREEN = !windowed.isSelected();
                selectFullscreen(!windowed.isSelected(), widthField, heightField, fullScreenResolutions, resizable, widthLabel, heightLabel);
            }
        });
        windowed.setSelected(!GlobalConf.screen.FULLSCREEN);
        selectFullscreen(GlobalConf.screen.FULLSCREEN, widthField, heightField, fullScreenResolutions, resizable, widthLabel, heightLabel);

        ButtonGroup modeButtons = new ButtonGroup();
        modeButtons.add(fullscreen);
        modeButtons.add(windowed);

        mode.add(fullscreen);
        mode.add(fullScreenResolutions, "wrap");
        mode.add(windowed);
        mode.add(windowedResolutions);

        /** GRAPHICS **/
        JPanel graphics = new JPanel(new MigLayout("", "[grow,fill][grow,fill][]", ""));
        graphics.setBorder(new TitledBorder(txt("gui.graphicssettings")));

        // Quality
        JLabel gqTooltip = new JLabel(IconManager.get("config/info-tooltip"));
        gqTooltip.setToolTipText(txt("gui.gquality.info"));

        ComboBoxBean[] gqs = new ComboBoxBean[] { new ComboBoxBean(txt("gui.gquality.high"), 0), new ComboBoxBean(txt("gui.gquality.normal"), 1), new ComboBoxBean(txt("gui.gquality.low"), 2) };
        final JComboBox<ComboBoxBean> gquality = new JComboBox<ComboBoxBean>(gqs);
        int index = -1;
        for (int i = 0; i < GlobalConf.data.OBJECTS_JSON_FILE_GQ.length; i++) {
            if (GlobalConf.data.OBJECTS_JSON_FILE_GQ[i].equals(GlobalConf.data.OBJECTS_JSON_FILE)) {
                index = i;
                break;
            }
        }
        int gqidx = index;
        gquality.setSelectedItem(gqs[gqidx]);

        // AA
        JLabel aaTooltip = new JLabel(IconManager.get("config/info-tooltip"));
        aaTooltip.setToolTipText(txt("gui.aa.info"));

        //ComboBoxBean[] aas = new ComboBoxBean[] { new ComboBoxBean(txt("gui.aa.no"), 0), new ComboBoxBean(txt("gui.aa.fxaa"), -1), new ComboBoxBean(txt("gui.aa.nfaa"), -2), new ComboBoxBean(txt("gui.aa.msaa", 2), 2), new ComboBoxBean(txt("gui.aa.msaa", 4), 4), new ComboBoxBean(txt("gui.aa.msaa", 8), 8), new ComboBoxBean(txt("gui.aa.msaa", 16), 16) };
        ComboBoxBean[] aas = new ComboBoxBean[] { new ComboBoxBean(txt("gui.aa.no"), 0), new ComboBoxBean(txt("gui.aa.fxaa"), -1), new ComboBoxBean(txt("gui.aa.nfaa"), -2) };
        final JComboBox<ComboBoxBean> msaa = new JComboBox<ComboBoxBean>(aas);
        msaa.setSelectedItem(aas[idxAa(2, GlobalConf.postprocess.POSTPROCESS_ANTIALIAS)]);

        // Vsync
        final JCheckBox vsync = new JCheckBox(txt("gui.vsync"), GlobalConf.screen.VSYNC);

        // Line renderer
        ComboBoxBean[] lineRenderers = new ComboBoxBean[] { new ComboBoxBean(txt("gui.linerenderer.normal"), 0), new ComboBoxBean(txt("gui.linerenderer.quad"), 1) };
        final JComboBox<ComboBoxBean> lineRenderer = new JComboBox<ComboBoxBean>(lineRenderers);
        lineRenderer.setSelectedItem(lineRenderers[GlobalConf.scene.LINE_RENDERER]);

        // Add all
        graphics.add(new JLabel(txt("gui.gquality") + ":"));
        graphics.add(gquality);
        graphics.add(gqTooltip, "wrap");

        graphics.add(new JLabel(txt("gui.aa") + ":"));
        graphics.add(msaa);
        graphics.add(aaTooltip, "wrap");

        graphics.add(new JLabel(txt("gui.linerenderer") + ":"));
        graphics.add(lineRenderer, "span");

        graphics.add(vsync, "span,wrap");

        /** NOTICE **/
        JPanel notice = new JPanel(new MigLayout("", "[]", ""));
        JLabel noticeText = new JLabel(txt("gui.graphics.info"));
        noticeText.setForeground(darkgreen);
        notice.add(noticeText);

        /** SUB TABBED PANE **/
        // JTabbedPane graphicsTabbedPane = new JTabbedPane();
        // graphicsTabbedPane.setTabPlacement(JTabbedPane.TOP);
        //
        // graphicsTabbedPane.addTab(txt("gui.resolutionmode"), mode);
        // graphicsTabbedPane.addTab(txt("gui.graphicssettings"), graphics);

        JPanel graphicsPanel = new JPanel(new MigLayout("", "[grow,fill][]", ""));
        // graphicsPanel.add(graphicsTabbedPane, "wrap");
        graphicsPanel.add(mode, "wrap");
        graphicsPanel.add(graphics, "wrap");
        if (!startup) {
            graphicsPanel.add(notice, "wrap");
        }

        tabbedPane.addTab(txt("gui.graphics"), IconManager.get("config/graphics"), graphicsPanel);
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

        /**
         * ====== USER INTERFACE TAB =======
         **/
        JPanel ui = new JPanel(new MigLayout("", "[grow,fill][grow,fill]", ""));
        ui.setBorder(new TitledBorder(txt("gui.ui.interfacesettings")));

        File i18nfolder = new File(GaiaSkyDesktop.ASSETS_LOC + "i18n/");

        String i18nname = "gsbundle";
        String[] files = i18nfolder.list();
        LangComboBoxBean[] langs = new LangComboBoxBean[files.length];
        int i = 0;
        for (String file : files) {
            if (file.startsWith("gsbundle") && file.endsWith(".properties")) {
                String locale = file.substring(i18nname.length(), file.length() - ".properties".length());
                if (locale.length() != 0) {
                    // Remove underscore _
                    locale = locale.substring(1).replace("_", "-");
                    Locale loc = Locale.forLanguageTag(locale);
                    langs[i] = new LangComboBoxBean(loc);
                } else {
                    langs[i] = new LangComboBoxBean(I18n.bundle.getLocale());
                }
            }
            i++;
        }
        Arrays.sort(langs);
        final JComboBox<LangComboBoxBean> lang = new JComboBox<LangComboBoxBean>(langs);
        lang.setSelectedItem(langs[idxLang(GlobalConf.program.LOCALE, langs)]);

        // Theme sample image
        JPanel sampleImagePanel = new JPanel(new MigLayout("", "push[]", ""));
        // final JLabel sampleImage = new JLabel();
        // sampleImagePanel.add(sampleImage);

        // Theme chooser
        String[] themes = new String[] { "dark-orange", "dark-orange-large", "dark-green", "light-blue", "HiDPI" };
        final JComboBox<String> theme = new JComboBox<String>(themes);
        // theme.addActionListener(new ActionListener() {
        // @Override
        // public void actionPerformed(ActionEvent e) {
        // String selected = (String) theme.getSelectedItem();
        // ImageIcon icon = new ImageIcon(GaiaSandboxDesktop.ASSETS_LOC +
        // "img/themes/" + selected + ".png");
        // sampleImage.setIcon(icon);
        // }
        // });
        theme.setSelectedItem(GlobalConf.program.UI_THEME);

        ui.add(new JLabel(txt("gui.ui.language") + ":"));
        ui.add(lang, "wrap");
        ui.add(new JLabel(txt("gui.ui.theme") + ":"));
        ui.add(theme, "wrap");
        ui.add(sampleImagePanel, "span, wrap");

        /** NOTICE **/
        JPanel uiNotice = new JPanel(new MigLayout("", "[]", ""));
        JLabel uinoticeText = new JLabel(txt("gui.ui.info"));
        uinoticeText.setForeground(darkgreen);
        uiNotice.add(uinoticeText);

        JPanel uiPanel = new JPanel(new MigLayout("", "[grow,fill][]", ""));
        uiPanel.add(ui, "wrap");
        if (!startup) {
            uiPanel.add(uiNotice, "wrap");
        }

        tabbedPane.addTab(txt("gui.ui.interface"), IconManager.get("config/interface"), uiPanel);
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

        /**
         * ====== PERFORMANCE TAB =======
         */

        /** MULTITHREAD **/
        JPanel multithread = new JPanel(new MigLayout("", "[grow,fill][grow,fill]", ""));
        multithread.setBorder(new TitledBorder(txt("gui.multithreading")));

        int maxthreads = Runtime.getRuntime().availableProcessors();
        ComboBoxBean[] cbs = new ComboBoxBean[maxthreads + 1];
        cbs[0] = new ComboBoxBean(txt("gui.letdecide"), 0);
        for (i = 1; i <= maxthreads; i++) {
            cbs[i] = new ComboBoxBean(txt("gui.thread", i), i);
        }
        final JComboBox<ComboBoxBean> numThreads = new JComboBox<ComboBoxBean>(cbs);
        numThreads.setSelectedIndex(GlobalConf.performance.NUMBER_THREADS);

        final JCheckBox multithreadCb = new JCheckBox(txt("gui.thread.enable"));
        multithreadCb.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                numThreads.setEnabled(multithreadCb.isSelected());
            }
        });
        multithreadCb.setSelected(GlobalConf.performance.MULTITHREADING);
        numThreads.setEnabled(multithreadCb.isSelected());

        multithread.add(multithreadCb, "span");
        multithread.add(new JLabel(txt("gui.thread.number") + ":"));
        multithread.add(numThreads);

        /** LEVELS OF DETAIL **/
        JPanel lod = new JPanel(new MigLayout("", "[grow,fill][grow,fill]", ""));
        lod.setBorder(new TitledBorder(txt("gui.lod")));

        // LOD fade
        final JCheckBox lodFadeCb = new JCheckBox(txt("gui.lod.fade"), GlobalConf.scene.OCTREE_PARTICLE_FADE);

        final JLabel lod0 = new JLabel(nf3.format(GlobalConf.scene.OCTANT_THRESHOLD_0));
        final JLabel lod1 = new JLabel(nf3.format(GlobalConf.scene.OCTANT_THRESHOLD_1));
        JPanel lodInfo = new JPanel();
        lodInfo.add(new JLabel("Min: "));
        lodInfo.add(lod0);
        lodInfo.add(new JLabel("    Max: "));
        lodInfo.add(lod1);

        // LOD transitions
        final RangeSlider lodTransitions = new RangeSlider((int) Constants.MIN_SLIDER, (int) Constants.MAX_SLIDER);

        lodTransitions.setUpperValue(Math.round(MathUtilsd.lint(GlobalConf.scene.OCTANT_THRESHOLD_1, Constants.MIN_LOD_TRANS_ANGLE, Constants.MAX_LOD_TRANS_ANGLE, Constants.MIN_SLIDER, Constants.MAX_SLIDER)));
        lodTransitions.setValue(Math.round(MathUtilsd.lint(GlobalConf.scene.OCTANT_THRESHOLD_0, Constants.MIN_LOD_TRANS_ANGLE, Constants.MAX_LOD_TRANS_ANGLE, Constants.MIN_SLIDER, Constants.MAX_SLIDER)));
        lodTransitions.setUpperValue(Math.round(MathUtilsd.lint(GlobalConf.scene.OCTANT_THRESHOLD_1, Constants.MIN_LOD_TRANS_ANGLE, Constants.MAX_LOD_TRANS_ANGLE, Constants.MIN_SLIDER, Constants.MAX_SLIDER)));

        lodTransitions.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                RangeSlider slider = (RangeSlider) e.getSource();
                lod0.setText(nf3.format(MathUtilsd.lint(slider.getValue(), Constants.MIN_SLIDER, Constants.MAX_SLIDER, Constants.MIN_LOD_TRANS_ANGLE, Constants.MAX_LOD_TRANS_ANGLE)));
                lod1.setText(nf3.format(MathUtilsd.lint(slider.getUpperValue(), Constants.MIN_SLIDER, Constants.MAX_SLIDER, Constants.MIN_LOD_TRANS_ANGLE, Constants.MAX_LOD_TRANS_ANGLE)));
            }
        });

        lod.add(lodFadeCb, "span");
        lod.add(new JLabel(txt("gui.lod.thresholds") + ":"));
        lod.add(lodTransitions, "wrap");
        lod.add(lodInfo, "span");

        JPanel performancePanel = new JPanel(new MigLayout("", "[grow,fill]", ""));
        performancePanel.add(multithread, "wrap");
        performancePanel.add(lod, "wrap");

        tabbedPane.addTab(txt("gui.performance"), IconManager.get("config/performance"), performancePanel);
        tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);

        /**
         * ====== CONTROLS TAB =======
         */
        JPanel controls = new JPanel(new MigLayout("", "[grow,fill][]", ""));
        controls.setBorder(new TitledBorder(txt("gui.keymappings")));

        Map<TreeSet<Integer>, ProgramAction> maps = KeyBindings.instance.getSortedMappings();
        Set<TreeSet<Integer>> keymaps = maps.keySet();

        String[] headers = new String[] { txt("gui.keymappings.action"), txt("gui.keymappings.keys") };
        String[][] data = new String[maps.size()][2];

        i = 0;
        for (TreeSet<Integer> keys : keymaps) {
            ProgramAction action = maps.get(keys);
            data[i][0] = action.actionName;
            data[i][1] = keysToString(keys);
            i++;
        }

        JTable table = new JTable(data, headers);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);
        table.setRowHeight(scale(table.getRowHeight()));

        JScrollPane controlsScrollPane = new JScrollPane(table);
        controlsScrollPane.setPreferredSize(new Dimension(0, scale(180)));

        //        JLabel lab = new JLabel(txt("gui.noteditable"));
        //        lab.setForeground(darkred);
        //        controls.add(lab, "wrap");
        controls.add(controlsScrollPane, "wrap");

        JPanel controlsPanel = new JPanel(new MigLayout("", "[grow,fill]", ""));
        controlsPanel.add(controls, "wrap");

        tabbedPane.addTab(txt("gui.controls"), IconManager.get("config/controls"), controlsPanel);
        tabbedPane.setMnemonicAt(3, KeyEvent.VK_4);

        /**
         * ====== SCREENSHOTS TAB =======
         */

        /** SCREENSHOTS CONFIG **/
        JPanel screenshots = new JPanel(new MigLayout("", "[grow,fill][grow,fill]", ""));
        screenshots.setBorder(new TitledBorder(txt("gui.screencapture")));

        JTextArea screenshotsInfo = new JTextArea(txt("gui.screencapture.info")) {
            @Override
            public void setBorder(Border border) {
                // No!
            }
        };
        screenshotsInfo.setEditable(false);
        screenshotsInfo.setBackground(transparent);
        screenshotsInfo.setForeground(darkgreen);

        // SCREENSHOTS LOCATION
        JLabel screenshotsLocationLabel = new JLabel(txt("gui.screenshots.save") + ":");
        final File currentLocation = new File(GlobalConf.screenshot.SCREENSHOT_FOLDER);
        String dirText = txt("gui.screenshots.directory.choose");
        final Label screenshotsTextContainer = new Label(currentLocation.getAbsolutePath());
        if (currentLocation.exists() && currentLocation.isDirectory()) {
            dirText = currentLocation.getName();
        }

        final JButton screenshotsDir = new JButton(dirText);
        screenshotsDir.addActionListener(new ActionListener() {
            JFileChooser chooser = null;

            @Override
            public void actionPerformed(ActionEvent e) {
                SecurityManager sm = System.getSecurityManager();
                System.setSecurityManager(null);
                chooser = new JFileChooser();

                chooser.setFileHidingEnabled(false);
                chooser.setMultiSelectionEnabled(false);
                chooser.setAcceptAllFileFilterUsed(false);
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                File f = new File(screenshotsTextContainer.getText());
                if (f.exists() && f.isDirectory()) {
                    chooser.setCurrentDirectory(f);
                    chooser.setSelectedFile(f);
                }
                chooser.setDialogTitle(txt("gui.directory.chooseany"));

                int v = chooser.showOpenDialog(null);

                switch (v) {
                case JFileChooser.APPROVE_OPTION:
                    File choice = null;
                    if (chooser.getSelectedFile() != null && chooser.getSelectedFile().isDirectory()) {
                        choice = chooser.getSelectedFile();
                    } else if (chooser.getCurrentDirectory() != null) {
                        choice = chooser.getCurrentDirectory();
                    }
                    screenshotsTextContainer.setText(choice.getAbsolutePath());
                    screenshotsDir.setText(choice.getName());
                    break;
                case JFileChooser.CANCEL_OPTION:
                case JFileChooser.ERROR_OPTION:
                }
                chooser.removeAll();
                chooser = null;
                System.setSecurityManager(sm);
            }
        });

        // SCREENSHOT WIDTH AND HEIGHT
        final JLabel screenshotsSizeLabel = new JLabel(txt("gui.screenshots.size") + ":");
        final JLabel xLabel = new JLabel("\u2715");
        final JSpinner sswidthField = new JSpinner(new SpinnerNumberModel(GlobalConf.screenshot.SCREENSHOT_WIDTH, 50, 25000, 1));
        final JSpinner ssheightField = new JSpinner(new SpinnerNumberModel(GlobalConf.screenshot.SCREENSHOT_HEIGHT, 50, 25000, 1));

        // SCREENSHOTS MODE
        ComboBoxBean[] screenshotModes = new ComboBoxBean[] { new ComboBoxBean(txt("gui.screenshots.mode.simple"), 0), new ComboBoxBean(txt("gui.screenshots.mode.redraw"), 1) };
        final JComboBox<ComboBoxBean> screenshotsMode = new JComboBox<ComboBoxBean>(screenshotModes);
        screenshotsMode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (((ComboBoxBean) screenshotsMode.getSelectedItem()).value == 0) {
                    // Simple
                    enableComponents(false, sswidthField, ssheightField, screenshotsSizeLabel, xLabel);
                } else {
                    // Redraw
                    enableComponents(true, sswidthField, ssheightField, screenshotsSizeLabel, xLabel);
                }
            }
        });
        screenshotsMode.setSelectedItem(screenshotModes[GlobalConf.screenshot.SCREENSHOT_MODE.ordinal()]);

        JLabel screenshotsModeTooltip = new JLabel(IconManager.get("config/info-tooltip"));
        screenshotsModeTooltip.setToolTipText(txt("gui.tooltip.screenshotmode"));

        JPanel screenshotsModePanel = new JPanel(new MigLayout("", "[grow,fill][]", ""));
        screenshotsModePanel.add(screenshotsMode);
        screenshotsModePanel.add(screenshotsModeTooltip, "wrap");

        screenshots.add(screenshotsInfo, "span,wrap");
        screenshots.add(screenshotsLocationLabel);
        screenshots.add(screenshotsDir, "span,wrap");
        screenshots.add(new JLabel(txt("gui.screenshots.mode") + ":"));
        screenshots.add(screenshotsModePanel, "span,wrap");
        screenshots.add(screenshotsSizeLabel);
        screenshots.add(sswidthField);
        screenshots.add(xLabel);
        screenshots.add(ssheightField);

        JPanel screenshotsPanel = new JPanel(new MigLayout("", "[grow,fill]", ""));
        screenshotsPanel.add(screenshots, "wrap");

        tabbedPane.addTab(txt("gui.screenshots"), IconManager.get("config/screenshots"), screenshotsPanel);
        tabbedPane.setMnemonicAt(4, KeyEvent.VK_5);

        /**
         * ====== FRAME OUTPUT TAB =======
         */

        /** IMAGE OUTPUT CONFIG **/
        JPanel imageOutput = new JPanel(new MigLayout("", "[grow,fill][grow,fill]", ""));
        imageOutput.setBorder(new TitledBorder(txt("gui.frameoutput")));

        JTextArea frameInfo = new JTextArea(txt("gui.frameoutput.info")) {
            @Override
            public void setBorder(Border border) {
                // No!
            }
        };
        frameInfo.setEditable(false);
        frameInfo.setBackground(transparent);
        frameInfo.setForeground(darkgreen);

        // FRAME SAVE LOCATION
        final File currentFrameLocation = new File(GlobalConf.frame.RENDER_FOLDER);
        String dirFrameText = txt("gui.frameoutput.directory.choose");
        final Label frameTextContainer = new Label(currentFrameLocation.getAbsolutePath());
        if (currentFrameLocation.exists() && currentFrameLocation.isDirectory()) {
            dirText = currentFrameLocation.getName();
        }

        final JButton frameDir = new JButton(dirText);
        frameDir.addActionListener(new ActionListener() {
            JFileChooser chooser = null;

            @Override
            public void actionPerformed(ActionEvent e) {
                SecurityManager sm = System.getSecurityManager();
                System.setSecurityManager(null);
                chooser = new JFileChooser();

                chooser.setFileHidingEnabled(false);
                chooser.setMultiSelectionEnabled(false);
                chooser.setAcceptAllFileFilterUsed(false);
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                File f = new File(frameTextContainer.getText());
                if (f.exists() && f.isDirectory()) {
                    chooser.setCurrentDirectory(f);
                    chooser.setSelectedFile(f);
                }
                chooser.setDialogTitle(txt("gui.directory.chooseany"));

                int v = chooser.showOpenDialog(null);

                switch (v) {
                case JFileChooser.APPROVE_OPTION:
                    File choice = null;
                    if (chooser.getSelectedFile() != null && chooser.getSelectedFile().isDirectory()) {
                        choice = chooser.getSelectedFile();
                    } else if (chooser.getCurrentDirectory() != null) {
                        choice = chooser.getCurrentDirectory();
                    }
                    frameTextContainer.setText(choice.getAbsolutePath());
                    frameDir.setText(choice.getName());
                    break;
                case JFileChooser.CANCEL_OPTION:
                case JFileChooser.ERROR_OPTION:
                }
                chooser.removeAll();
                chooser = null;
                System.setSecurityManager(sm);
            }
        });

        // NAME
        final JTextField frameFileName = new JTextField();
        frameFileName.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                warn();
            }

            public void removeUpdate(DocumentEvent e) {
                warn();
            }

            public void insertUpdate(DocumentEvent e) {
                warn();
            }

            public void warn() {
                String text = frameFileName.getText();
                // Only word characters
                if (!text.matches("^\\w+$")) {
                    frameFileName.setForeground(Color.red);
                } else {
                    frameFileName.setForeground(Color.black);
                }
            }
        });
        frameFileName.setText(GlobalConf.frame.RENDER_FILE_NAME);

        // TARGET FPS
        final JSpinner targetFPS = new JSpinner(new SpinnerNumberModel(GlobalConf.frame.RENDER_TARGET_FPS, 1, 60, 1));

        // FRAME OUTPUT WIDTH AND HEIGHT
        final JLabel frameSizeLabel = new JLabel(txt("gui.frameoutput.size") + ":");
        final JLabel frameXLabel = new JLabel("\u2715");
        final JSpinner frameWidthField = new JSpinner(new SpinnerNumberModel(GlobalConf.frame.RENDER_WIDTH, 50, 25000, 1));
        final JSpinner frameHeightField = new JSpinner(new SpinnerNumberModel(GlobalConf.frame.RENDER_HEIGHT, 50, 25000, 1));

        // FRAME OUTPUT MODE
        ComboBoxBean[] frameModesBean = new ComboBoxBean[] { new ComboBoxBean(txt("gui.screenshots.mode.simple"), 0), new ComboBoxBean(txt("gui.screenshots.mode.redraw"), 1) };
        final JComboBox<ComboBoxBean> frameMode = new JComboBox<ComboBoxBean>(frameModesBean);
        frameMode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (((ComboBoxBean) frameMode.getSelectedItem()).value == 0) {
                    // Simple
                    enableComponents(false, frameWidthField, frameHeightField, frameSizeLabel, frameXLabel);
                } else {
                    // Redraw
                    enableComponents(true, frameWidthField, frameHeightField, frameSizeLabel, frameXLabel);
                }
            }
        });
        frameMode.setSelectedItem(frameModesBean[GlobalConf.frame.FRAME_MODE.ordinal()]);

        JLabel frameModeTooltip = new JLabel(IconManager.get("config/info-tooltip"));
        frameModeTooltip.setToolTipText(txt("gui.tooltip.screenshotmode"));

        JPanel frameModePanel = new JPanel(new MigLayout("", "[grow,fill][]", ""));
        frameModePanel.add(frameMode);
        frameModePanel.add(frameModeTooltip, "wrap");

        // FRAME OUTPUT CHECKBOX
        imageOutput.add(frameInfo, "span");
        imageOutput.add(new JLabel(txt("gui.frameoutput.location") + ":"));
        imageOutput.add(frameDir, "wrap, span");
        imageOutput.add(new JLabel(txt("gui.frameoutput.prefix") + ":"));
        imageOutput.add(frameFileName, "wrap, span");
        imageOutput.add(new JLabel(txt("gui.frameoutput.fps") + ":"));
        imageOutput.add(targetFPS, "span");
        imageOutput.add(new JLabel(txt("gui.screenshots.mode") + ":"));
        imageOutput.add(frameModePanel, "span,wrap");
        imageOutput.add(frameSizeLabel);
        imageOutput.add(frameWidthField);
        imageOutput.add(frameXLabel);
        imageOutput.add(frameHeightField, "wrap");

        JPanel imageOutputPanel = new JPanel(new MigLayout("", "[grow,fill]", ""));
        imageOutputPanel.add(imageOutput, "wrap");

        tabbedPane.addTab(txt("gui.frameoutput.title"), IconManager.get("config/frameoutput"), imageOutputPanel);
        tabbedPane.setMnemonicAt(5, KeyEvent.VK_6);

        /**
         * ====== CAMERA RECORDING TAB =======
         */

        JPanel cameraRec = new JPanel(new MigLayout("", "[grow,fill][grow,fill]", ""));
        cameraRec.setBorder(new TitledBorder(txt("gui.camerarec")));

        // TARGET FPS
        final JSpinner targetFPScamera = new JSpinner(new SpinnerNumberModel(GlobalConf.frame.CAMERA_REC_TARGET_FPS, 1, 60, 1));

        // AUTOMATICALLY ENABLE FRAME OUTPUT WHEN PLAYING CAMERA
        final JCheckBox autoFrameOutput = new JCheckBox(txt("gui.camerarec.frameoutput"), GlobalConf.frame.AUTO_FRAME_OUTPUT_CAMERA_PLAY);
        autoFrameOutput.setToolTipText(txt("gui.tooltip.playcamera.frameoutput"));
        JLabel afoTooltip = new JLabel(IconManager.get("config/info-tooltip"));
        afoTooltip.setToolTipText(txt("gui.tooltip.playcamera.frameoutput"));

        cameraRec.add(new JLabel(txt("gui.camerarec.fps") + ":"));
        cameraRec.add(targetFPScamera, "span,wrap");
        cameraRec.add(autoFrameOutput);
        cameraRec.add(afoTooltip);

        JPanel cameraRecPanel = new JPanel(new MigLayout("", "[grow,fill]", ""));
        cameraRecPanel.add(cameraRec, "wrap");

        tabbedPane.addTab(txt("gui.camerarec.title"), IconManager.get("config/camera"), cameraRecPanel);
        tabbedPane.setMnemonicAt(6, KeyEvent.VK_7);

        /**
         * ====== 360 MODE =======
         */

        JTextArea mode360Info = new JTextArea(txt("gui.360.info")) {
            @Override
            public void setBorder(Border border) {
                // No!
            }
        };
        mode360Info.setEditable(false);
        mode360Info.setBackground(transparent);
        mode360Info.setForeground(darkgreen);

        JPanel mode360 = new JPanel(new MigLayout("", "[grow,fill][grow,fill]", ""));
        mode360.setBorder(new TitledBorder(txt("gui.360")));

        // CUBEMAP RESOLUTION
        final JSpinner cubemapResolution = new JSpinner(new SpinnerNumberModel(GlobalConf.scene.CUBEMAP_FACE_RESOLUTION, 20, 15000, 1));

        mode360.add(mode360Info, "span");
        mode360.add(new JLabel(txt("gui.360.resolution") + ":"));
        mode360.add(cubemapResolution, "span");

        JPanel mode360Panel = new JPanel(new MigLayout("", "[grow,fill]", ""));
        mode360Panel.add(mode360, "wrap");

        tabbedPane.addTab(txt("gui.360.title"), IconManager.get("config/360"), mode360Panel);
        tabbedPane.setMnemonicAt(7, KeyEvent.VK_8);

        /**
         * ====== DATA TAB =======
         */
        JPanel datasource = new JPanel(new MigLayout("", "[][grow,fill][]", ""));
        datasource.setBorder(new TitledBorder(txt("gui.data.source")));

        // HYG 
        final JRadioButton hyg = new JRadioButton(txt("gui.data.hyg"));
        hyg.setSelected(GlobalConf.data.CATALOG_JSON_FILE.equals(GlobalConf.data.HYG_JSON_FILE));

        // TGAS
        final JRadioButton tgas = new JRadioButton(txt("gui.data.tgas"));
        tgas.setSelected(GlobalConf.data.CATALOG_JSON_FILE.equals(GlobalConf.data.TGAS_JSON_FILE));

        ButtonGroup dataButtons = new ButtonGroup();
        dataButtons.add(hyg);
        dataButtons.add(tgas);

        datasource.add(hyg, "span,wrap");
        datasource.add(tgas, "span,wrap");

        final JPanel dataPanel = new JPanel(new MigLayout("", "[grow,fill]", ""));
        dataPanel.add(datasource, "wrap");

        tabbedPane.addTab(txt("gui.data"), IconManager.get("config/data"), dataPanel);
        tabbedPane.setMnemonicAt(8, KeyEvent.VK_9);

        /**
         * ====== GAIA TAB =======
         */
        JPanel gaia = new JPanel(new MigLayout("", "[][grow,fill][]", ""));
        gaia.setBorder(new TitledBorder(txt("gui.gaia.attitude")));

        // REAL OR NSL attitude
        final JRadioButton real = new JRadioButton(txt("gui.gaia.real"));
        real.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GlobalConf.data.REAL_GAIA_ATTITUDE = real.isSelected();
            }
        });
        real.setSelected(GlobalConf.data.REAL_GAIA_ATTITUDE);

        final JRadioButton nsl = new JRadioButton(txt("gui.gaia.nsl"));
        nsl.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GlobalConf.data.REAL_GAIA_ATTITUDE = !nsl.isSelected();
            }
        });
        nsl.setSelected(!GlobalConf.data.REAL_GAIA_ATTITUDE);

        ButtonGroup gaiaButtons = new ButtonGroup();
        gaiaButtons.add(real);
        gaiaButtons.add(nsl);

        gaia.add(real, "span,wrap");
        gaia.add(nsl, "span,wrap");

        final JPanel gaiaPanel = new JPanel(new MigLayout("", "[grow,fill]", ""));
        gaiaPanel.add(gaia, "wrap");

        tabbedPane.addTab(txt("gui.gaia"), IconManager.get("config/gaia"), gaiaPanel);

        /** SHOW AGAIN? **/

        // Do not show again
        final JCheckBox showAgain = new JCheckBox(txt("gui.showatstartup"));
        showAgain.setSelected(GlobalConf.program.SHOW_CONFIG_DIALOG);
        showAgain.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                GlobalConf.program.SHOW_CONFIG_DIALOG = showAgain.isSelected();
            }
        });

        body.add(tabbedPane, "wrap");
        body.add(checkPanel, "wrap");
        body.add(showAgain, "wrap");

        /** BUTTONS **/
        JPanel buttons = new JPanel(new MigLayout("", "push[][]", ""));

        okButton = new JButton(startup ? txt("gui.launchapp") : txt("gui.saveprefs"));
        okButton.addActionListener(new ActionListener() {
            boolean goahead;

            @Override
            public void actionPerformed(ActionEvent evt) {
                goahead = true;

                if (goahead) {
                    // Add all properties to GlobalConf.instance
                    GlobalConf.screen.FULLSCREEN = fullscreen.isSelected();

                    // Fullscreen options
                    GlobalConf.screen.FULLSCREEN_WIDTH = ((DisplayMode) fullScreenResolutions.getSelectedItem()).width;
                    GlobalConf.screen.FULLSCREEN_HEIGHT = ((DisplayMode) fullScreenResolutions.getSelectedItem()).height;

                    // Windowed options
                    GlobalConf.screen.SCREEN_WIDTH = ((Integer) widthField.getValue());
                    GlobalConf.screen.SCREEN_HEIGHT = ((Integer) heightField.getValue());
                    GlobalConf.screen.RESIZABLE = resizable.isSelected();

                    // Graphics
                    ComboBoxBean bean = (ComboBoxBean) gquality.getSelectedItem();
                    GlobalConf.data.OBJECTS_JSON_FILE = GlobalConf.data.OBJECTS_JSON_FILE_GQ[bean.value];
                    GlobalConf.scene.GRAPHICS_QUALITY = bean.value;

                    bean = (ComboBoxBean) msaa.getSelectedItem();
                    GlobalConf.postprocess.POSTPROCESS_ANTIALIAS = bean.value;
                    EventManager.instance.post(Events.ANTIALIASING_CMD, bean.value);
                    GlobalConf.screen.VSYNC = vsync.isSelected();

                    // Line renderer
                    bean = (ComboBoxBean) lineRenderer.getSelectedItem();
                    GlobalConf.scene.LINE_RENDERER = bean.value;

                    // Interface
                    LangComboBoxBean lbean = (LangComboBoxBean) lang.getSelectedItem();
                    GlobalConf.program.LOCALE = lbean.locale.toLanguageTag();
                    I18n.forceinit(Gdx.files.internal("i18n/gsbundle"));
                    GlobalConf.program.UI_THEME = (String) theme.getSelectedItem();
                    if (GlobalConf.program.UI_THEME.equalsIgnoreCase("hidpi")) {
                        GlobalConf.updateScaleFactor(Math.max(GlobalConf.SCALE_FACTOR, 1.6f));
                    }

                    // Performance
                    bean = (ComboBoxBean) numThreads.getSelectedItem();
                    GlobalConf.performance.NUMBER_THREADS = bean.value;
                    GlobalConf.performance.MULTITHREADING = multithreadCb.isSelected();

                    GlobalConf.scene.OCTREE_PARTICLE_FADE = lodFadeCb.isSelected();
                    GlobalConf.scene.OCTANT_THRESHOLD_0 = MathUtilsd.lint(lodTransitions.getValue(), Constants.MIN_SLIDER, Constants.MAX_SLIDER, Constants.MIN_LOD_TRANS_ANGLE, Constants.MAX_LOD_TRANS_ANGLE);
                    GlobalConf.scene.OCTANT_THRESHOLD_1 = MathUtilsd.lint(lodTransitions.getUpperValue(), Constants.MIN_SLIDER, Constants.MAX_SLIDER, Constants.MIN_LOD_TRANS_ANGLE, Constants.MAX_LOD_TRANS_ANGLE);

                    // Data
                    if (hyg.isSelected())
                        GlobalConf.data.CATALOG_JSON_FILE = GlobalConf.data.HYG_JSON_FILE;
                    else if (tgas.isSelected())
                        GlobalConf.data.CATALOG_JSON_FILE = GlobalConf.data.TGAS_JSON_FILE;
                    else if (GlobalConf.data.CATALOG_JSON_FILE == null || GlobalConf.data.CATALOG_JSON_FILE.length() == 0)
                        Logger.error(this.getClass().getSimpleName(), "No catalog file selected!");

                    // Screenshots
                    File ssfile = new File(screenshotsTextContainer.getText());
                    if (ssfile.exists() && ssfile.isDirectory())
                        GlobalConf.screenshot.SCREENSHOT_FOLDER = ssfile.getAbsolutePath();
                    GlobalConf.screenshot.SCREENSHOT_MODE = GlobalConf.ScreenshotMode.values()[screenshotsMode.getSelectedIndex()];
                    GlobalConf.screenshot.SCREENSHOT_WIDTH = ((Integer) sswidthField.getValue());
                    GlobalConf.screenshot.SCREENSHOT_HEIGHT = ((Integer) ssheightField.getValue());
                    EventManager.instance.post(Events.SCREENSHOT_SIZE_UDPATE, GlobalConf.screenshot.SCREENSHOT_WIDTH, GlobalConf.screenshot.SCREENSHOT_HEIGHT);

                    // Frame output
                    File fofile = new File(frameTextContainer.getText());
                    if (fofile.exists() && fofile.isDirectory())
                        GlobalConf.frame.RENDER_FOLDER = fofile.getAbsolutePath();
                    String text = frameFileName.getText();
                    if (text.matches("^\\w+$")) {
                        GlobalConf.frame.RENDER_FILE_NAME = text;
                    }
                    GlobalConf.frame.FRAME_MODE = GlobalConf.ScreenshotMode.values()[frameMode.getSelectedIndex()];
                    GlobalConf.frame.RENDER_WIDTH = ((Integer) frameWidthField.getValue());
                    GlobalConf.frame.RENDER_HEIGHT = ((Integer) frameHeightField.getValue());
                    GlobalConf.frame.RENDER_TARGET_FPS = ((Integer) targetFPS.getValue());
                    EventManager.instance.post(Events.FRAME_SIZE_UDPATE, GlobalConf.frame.RENDER_WIDTH, GlobalConf.frame.RENDER_HEIGHT);

                    // Camera recording
                    GlobalConf.frame.CAMERA_REC_TARGET_FPS = (Integer) targetFPScamera.getValue();
                    GlobalConf.frame.AUTO_FRAME_OUTPUT_CAMERA_PLAY = (Boolean) autoFrameOutput.isSelected();

                    // Cube map resolution
                    GlobalConf.scene.CUBEMAP_FACE_RESOLUTION = (Integer) cubemapResolution.getValue();

                    // Save configuration
                    ConfInit.instance.persistGlobalConf(new File(System.getProperty("properties.file")));

                    EventManager.instance.post(Events.PROPERTIES_WRITTEN);

                    if (startup) {
                        gsd.launchMainApp();
                    }
                    frame.dispose();
                    singleton = null;
                }
            }

        });
        okButton.setMinimumSize(new Dimension(100, 20));

        cancelButton = new JButton(txt("gui.cancel"));
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (frame.isDisplayable()) {
                    frame.dispose();
                    singleton = null;
                    if (startup) {
                        gsd.terminate();
                    }

                }
            }
        });
        cancelButton.setMinimumSize(new Dimension(100, 20));

        buttons.add(okButton);
        buttons.add(cancelButton);

        frame.add(body, BorderLayout.CENTER);
        frame.add(buttons, BorderLayout.SOUTH);

    }

    private void enableComponents(boolean enabled, JComponent... components) {
        for (JComponent c : components) {
            if (c != null)
                c.setEnabled(enabled);
        }
    }

    private void selectFullscreen(boolean fullscreen, JSpinner widthField, JSpinner heightField, JComboBox<DisplayMode> fullScreenResolutions, JCheckBox resizable, JLabel widthLabel, JLabel heightLabel) {
        if (fullscreen) {
            GlobalConf.screen.SCREEN_WIDTH = ((DisplayMode) fullScreenResolutions.getSelectedItem()).width;
            GlobalConf.screen.SCREEN_HEIGHT = ((DisplayMode) fullScreenResolutions.getSelectedItem()).height;
        } else {
            GlobalConf.screen.SCREEN_WIDTH = (Integer) widthField.getValue();
            GlobalConf.screen.SCREEN_HEIGHT = (Integer) heightField.getValue();
        }

        enableComponents(!fullscreen, widthField, heightField, resizable, widthLabel, heightLabel);
        enableComponents(fullscreen, fullScreenResolutions);
    }

    private int idxAa(int base, int x) {
        if (x == -1)
            return 1;
        if (x == -2)
            return 2;
        if (x == 0)
            return 0;
        return (int) (Math.log(x) / Math.log(2) + 1e-10) + 2;
    }

    private int idxLang(String code, LangComboBoxBean[] langs) {
        if (code.isEmpty()) {
            code = I18n.bundle.getLocale().toLanguageTag();
        }
        for (int i = 0; i < langs.length; i++) {
            if (langs[i].locale.toLanguageTag().equals(code)) {
                return i;
            }
        }
        return -1;
    }

    private class ComboBoxBean {
        public String name;
        public int value;

        public ComboBoxBean(String name, int samples) {
            super();
            this.name = name;
            this.value = samples;
        }

        @Override
        public String toString() {
            return name;
        }

    }

    private class LangComboBoxBean implements Comparable<LangComboBoxBean> {
        public Locale locale;
        public String name;

        public LangComboBoxBean(Locale locale) {
            super();
            this.locale = locale;
            this.name = TextUtils.capitalise(locale.getDisplayName(locale));
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public int compareTo(LangComboBoxBean o) {
            return this.name.compareTo(o.name);
        }

    }

    private Thread getCheckVersionThread() {
        return new Thread(new CallbackTask(new VersionChecker(GlobalConf.program.VERSION_CHECK_URL), new Callback() {
            @Override
            public void complete(Object result) {
                checkPanel.removeAll();
                checkPanel.add(checkLabel);
                if (result instanceof String) {
                    // Error
                    checkLabel.setText("Error checking version: " + (String) result);
                    checkLabel.setForeground(Color.RED);
                } else if (result instanceof JsonValue) {
                    // Ok!
                    JsonValue json = (JsonValue) result;

                    JsonValue last = json.get(0);
                    String version = last.getString("name");
                    if (version.matches("^(\\D{1})?\\d+.\\d+(\\D{1})?(.\\d+)?$")) {
                        GlobalConf.program.LAST_VERSION = new String(version);
                        GlobalConf.program.LAST_CHECKED = new Date();
                        newVersionCheck(version);
                    }
                    checkPanel.validate();
                }

            }
        }));
    }

    /**
     * Checks the given version against the current version and:
     * <ul>
     * <li>Displays a "new version available" message if the given version is
     * newer than the current.</li>
     * <li>Display a "you have the latest version" message and a "check now"
     * button if the given version is older.</li>
     * </ul>
     * 
     * @param version
     *            The version to check.
     */
    private void newVersionCheck(String version) {
        int[] majmin = GlobalConf.VersionConf.getMajorMinorRevFromString(version);

        if (majmin[0] > GlobalConf.version.major || (majmin[0] == GlobalConf.version.major && majmin[1] > GlobalConf.version.minor) || (majmin[0] == GlobalConf.version.major && majmin[1] == GlobalConf.version.minor) && majmin[2] > GlobalConf.version.rev) {
            // There's a new version!
            checkLabel.setText(txt("gui.newversion.available", GlobalConf.version, version));
            try {
                final URI uri = new URI(GlobalConf.WEBPAGE);

                JButton button = new JButton();
                button.setText(txt("gui.newversion.getit"));
                button.setHorizontalAlignment(SwingConstants.LEFT);
                button.setToolTipText(uri.toString());
                button.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (Desktop.isDesktopSupported()) {
                            try {
                                Desktop.getDesktop().browse(uri);
                            } catch (IOException ex) {
                            }
                        } else {
                        }
                    }

                });
                checkPanel.add(button);
            } catch (URISyntaxException e1) {
            }
        } else {
            checkLabel.setText(txt("gui.newversion.nonew", GlobalConf.program.getLastCheckedString()));
            // Add check now button
            JButton button = new JButton();
            button.setText(txt("gui.newversion.checknow"));
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setToolTipText(txt("gui.newversion.checknow.tooltip"));
            button.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    getCheckVersionThread().start();
                }

            });
            checkPanel.add(button);
        }
        checkLabel.setForeground(darkgreen);
    }

    private String keysToString(TreeSet<Integer> keys) {
        String s = "";

        int i = 0;
        int n = keys.size();
        for (Integer key : keys) {
            s += Keys.toString(key).toUpperCase();
            if (i < n - 1) {
                s += " + ";
            }

            i++;
        }

        return s;
    }

    class JXTabbedPane extends JTabbedPane {

        private ITabRenderer tabRenderer = new DefaultTabRenderer();

        public JXTabbedPane() {
            super();
        }

        public JXTabbedPane(int tabPlacement) {
            super(tabPlacement);
        }

        public JXTabbedPane(int tabPlacement, int tabLayoutPolicy) {
            super(tabPlacement, tabLayoutPolicy);
        }

        public ITabRenderer getTabRenderer() {
            return tabRenderer;
        }

        public void setTabRenderer(ITabRenderer tabRenderer) {
            this.tabRenderer = tabRenderer;
        }

        @Override
        public void addTab(String title, Component component) {
            this.addTab(title, null, component, null);
        }

        @Override
        public void addTab(String title, Icon icon, Component component) {
            this.addTab(title, icon, component, null);
        }

        @Override
        public void addTab(String title, Icon icon, Component component, String tip) {
            super.addTab(title, icon, component, tip);
            int tabIndex = getTabCount() - 1;
            Component tab = tabRenderer.getTabRendererComponent(this, title, icon, tabIndex);
            super.setTabComponentAt(tabIndex, tab);
        }
    }

    interface ITabRenderer {

        public Component getTabRendererComponent(JTabbedPane tabbedPane, String text, Icon icon, int tabIndex);

    }

    abstract class AbstractTabRenderer implements ITabRenderer {

        private String prototypeText = "";
        private Icon prototypeIcon = UIManager.getIcon("OptionPane.informationIcon");
        private int horizontalTextAlignment = SwingConstants.CENTER;
        private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

        public AbstractTabRenderer() {
            super();
        }

        public void setPrototypeText(String text) {
            String oldText = this.prototypeText;
            this.prototypeText = text;
            firePropertyChange("prototypeText", oldText, text);
        }

        public String getPrototypeText() {
            return prototypeText;
        }

        public Icon getPrototypeIcon() {
            return prototypeIcon;
        }

        public void setPrototypeIcon(Icon icon) {
            Icon oldIcon = this.prototypeIcon;
            this.prototypeIcon = icon;
            firePropertyChange("prototypeIcon", oldIcon, icon);
        }

        public int getHorizontalTextAlignment() {
            return horizontalTextAlignment;
        }

        public void setHorizontalTextAlignment(int horizontalTextAlignment) {
            this.horizontalTextAlignment = horizontalTextAlignment;
        }

        public PropertyChangeListener[] getPropertyChangeListeners() {
            return propertyChangeSupport.getPropertyChangeListeners();
        }

        public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
            return propertyChangeSupport.getPropertyChangeListeners(propertyName);
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            propertyChangeSupport.addPropertyChangeListener(listener);
        }

        public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
            propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
        }

        protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
            PropertyChangeListener[] listeners = getPropertyChangeListeners();
            for (int i = listeners.length - 1; i >= 0; i--) {
                listeners[i].propertyChange(new PropertyChangeEvent(this, propertyName, oldValue, newValue));
            }
        }
    }

    class DefaultTabRenderer extends AbstractTabRenderer implements PropertyChangeListener {

        private Component prototypeComponent;

        public DefaultTabRenderer() {
            super();
            prototypeComponent = generateRendererComponent(getPrototypeText(), getPrototypeIcon(), getHorizontalTextAlignment());
            addPropertyChangeListener(this);
        }

        private Component generateRendererComponent(String text, Icon icon, int horizontalTabTextAlignmen) {
            JPanel rendererComponent = new JPanel(new GridBagLayout());
            rendererComponent.setOpaque(false);

            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(scale(2), scale(4), scale(2), scale(4));
            c.fill = GridBagConstraints.HORIZONTAL;
            rendererComponent.add(new JLabel(icon), c);

            c.gridx = 1;
            c.weightx = 1;
            rendererComponent.add(new JLabel(text, horizontalTabTextAlignmen), c);

            return rendererComponent;
        }

        @Override
        public Component getTabRendererComponent(JTabbedPane tabbedPane, String text, Icon icon, int tabIndex) {
            Component rendererComponent = generateRendererComponent(text, icon, getHorizontalTextAlignment());
            int prototypeWidth = prototypeComponent.getPreferredSize().width;
            int prototypeHeight = prototypeComponent.getPreferredSize().height;
            float fontsize = new JLabel().getFont().getSize();
            prototypeWidth = scale(Math.min(250, Math.round(150 * fontsize / 12f)));
            prototypeHeight = scale(Math.min(30, Math.round(20 * fontsize / 12f)));
            rendererComponent.setPreferredSize(new Dimension(prototypeWidth, prototypeHeight));
            return rendererComponent;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propertyName = evt.getPropertyName();
            if ("prototypeText".equals(propertyName) || "prototypeIcon".equals(propertyName)) {
                this.prototypeComponent = generateRendererComponent(getPrototypeText(), getPrototypeIcon(), getHorizontalTextAlignment());
            }
        }
    }

    private int scale(int value) {
        return Math.round(value * GlobalConf.SCALE_FACTOR);
    }

}
