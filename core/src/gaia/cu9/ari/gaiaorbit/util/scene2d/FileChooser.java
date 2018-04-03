package gaia.cu9.ari.gaiaorbit.util.scene2d;

import java.io.File;
import java.io.FileFilter;
import java.util.Comparator;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class FileChooser extends Dialog {

    public interface ResultListener {
        boolean result(boolean success, FileHandle result);
    }

    private final Skin skin;
    private boolean fileNameEnabled;
    private final TextField fileNameInput;
    private final Label fileNameLabel;
    private final FileHandle baseDir;
    private final Label fileListLabel;
    private final List<FileListItem> fileList;

    private FileHandle currentDir;
    protected String result;

    protected ResultListener resultListener;

    private final TextButton ok;
    private final TextButton cancel;

    private static final Comparator<FileListItem> dirListComparator = new Comparator<FileListItem>() {
        @Override
        public int compare(FileListItem file1, FileListItem file2) {
            if (file1.file.isDirectory() && !file2.file.isDirectory()) {
                return -1;
            }
            if (file1.file.isDirectory() && file2.file.isDirectory()) {
                return file1.name.compareTo(file2.name);
            }
            if (!file1.file.isDirectory() && !file2.file.isDirectory()) {
                return file1.name.compareTo(file2.name);
            }
            return 1;
        }
    };
    private FileFilter filter = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            return true;
        }
    };
    private boolean directoryBrowsingEnabled = true;

    public FileChooser(String title, final Skin skin, FileHandle baseDir) {
        super(title, skin);
        this.skin = skin;
        this.baseDir = baseDir;

        final Table content = getContentTable();
        content.top().left();

        fileListLabel = new Label("", skin);
        fileListLabel.setAlignment(Align.left);

        fileList = new List<FileListItem>(skin, "light");
        fileList.getSelection().setProgrammaticChangeEvents(false);

        fileNameInput = new TextField("", skin);
        fileNameLabel = new Label("File name:", skin);
        fileNameInput.setTextFieldListener(new TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                result = textField.getText();
            }
        });

        ok = new TextButton("Ok", skin);
        button(ok, true);

        cancel = new TextButton("Cancel", skin);
        button(cancel, false);
        key(Keys.ENTER, true);
        key(Keys.ESCAPE, false);

        fileList.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                final FileListItem selected = fileList.getSelected();
                result = selected.name;
                fileNameInput.setText(result);
            }
        });
    }

    private void changeDirectory(FileHandle directory) {

        currentDir = directory;
        fileListLabel.setText(currentDir.path());

        final Array<FileListItem> items = new Array<FileListItem>();

        final FileHandle[] list = directory.list(filter);
        for (final FileHandle handle : list) {
            items.add(new FileListItem(handle));
        }

        items.sort(dirListComparator);

        if (directory.file().getParentFile() != null) {
            items.insert(0, new FileListItem("..", directory.parent()));
        }

        fileList.setSelected(null);
        fileList.setItems(items);
    }

    public FileHandle getResult() {
        String path = currentDir.path() + "/";
        if (result != null && result.length() > 0) {
            String folder = currentDir.file().getName();
            if (folder.equals(result)) {
                if ((new FileHandle(path + result)).exists()) {
                    path += result;
                } else {
                    // Nothing
                }
            } else {
                path += result;
            }
        }
        return new FileHandle(path);
    }

    public FileChooser setFilter(FileFilter filter) {
        this.filter = filter;
        return this;
    }

    public FileChooser setOkButtonText(String text) {
        this.ok.setText(text);
        return this;
    }

    public FileChooser setCancelButtonText(String text) {
        this.cancel.setText(text);
        return this;
    }

    public FileChooser setFileNameEnabled(boolean fileNameEnabled) {
        this.fileNameEnabled = fileNameEnabled;
        return this;
    }

    public FileChooser setResultListener(ResultListener result) {
        this.resultListener = result;
        return this;
    }

    public FileChooser disableDirectoryBrowsing() {
        this.directoryBrowsingEnabled = false;
        return this;

    }

    long lastClick = 0l;

    @Override
    public Dialog show(Stage stage, Action action) {
        final Table content = getContentTable();
        content.add(fileListLabel).top().left().expandX().fillX().row();
        content.add(new ScrollPane(fileList, skin)).size(300, 150).fill().expand().row();

        if (fileNameEnabled) {
            content.add(fileNameLabel).fillX().expandX().row();
            content.add(fileNameInput).fillX().expandX().row();
            stage.setKeyboardFocus(fileNameInput);
        }

        if (directoryBrowsingEnabled) {
            fileList.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    final FileListItem selected = fileList.getSelected();
                    if (selected.file.isDirectory() && TimeUtils.millis() - lastClick < 500) {
                        changeDirectory(selected.file);
                        lastClick = 0;
                    } else if (event.getType() == Type.touchUp) {
                        lastClick = TimeUtils.millis();
                    }
                }
            });
        }

        changeDirectory(baseDir);
        return super.show(stage, action);
    }

    public static FileChooser createSaveDialog(String title, final Skin skin, final FileHandle path) {
        final FileChooser save = new FileChooser(title, skin, path) {
            @Override
            protected void result(Object object) {

                if (resultListener == null) {
                    return;
                }

                final boolean success = (Boolean) object;
                if (!resultListener.result(success, getResult())) {
                    this.cancel();
                }
            }
        }.setFileNameEnabled(true).setOkButtonText("Save");

        return save;

    }

    public static FileChooser createLoadDialog(String title, final Skin skin, final FileHandle path) {
        final FileChooser load = new FileChooser(title, skin, path) {
            @Override
            protected void result(Object object) {

                if (resultListener == null) {
                    return;
                }

                final boolean success = (Boolean) object;
                resultListener.result(success, getResult());
            }
        }.setFileNameEnabled(false).setOkButtonText("Load");

        return load;

    }

    public static FileChooser createPickDialog(String title, final Skin skin, final FileHandle path) {
        final FileChooser pick = new FileChooser(title, skin, path) {
            @Override
            protected void result(Object object) {

                if (resultListener == null) {
                    return;
                }

                final boolean success = (Boolean) object;
                resultListener.result(success, getResult());
            }
        }.setOkButtonText("Select");

        return pick;
    }

    private class FileListItem {

        public FileHandle file;
        public String name;

        public FileListItem(FileHandle file) {
            this.file = file;
            this.name = file.name();
        }

        public FileListItem(String name, FileHandle file) {
            this.file = file;
            this.name = name;
        }

        public String toString() {
            return name;
        }

    }

}