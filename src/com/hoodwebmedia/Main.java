package com.hoodwebmedia;

import com.hoodwebmedia.DynamiteMediaPlayer.OnTrackEndListener;
import com.hoodwebmedia.DynamiteMediaPlayer.OnTrackPauseListener;
import com.hoodwebmedia.DynamiteMediaPlayer.OnTrackProgressListener;
import com.hoodwebmedia.DynamiteMediaPlayer.OnTrackStartListener;
import com.hoodwebmedia.MusicFolderCrawler.OnCompleteListener;
import com.hoodwebmedia.MusicFolderCrawler.OnProgressListener;
import com.sun.javafx.scene.control.skin.LabeledText;
import com.sun.javafx.scene.control.skin.TreeCellSkin;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

/**
 *
 * @author dhoodlum
 */
public class Main extends Application {
    public static DynamiteMediaManager dmm = new DynamiteMediaManager();
    public static DynamiteMediaPlayer dmp = new DynamiteMediaPlayer();
    Stage stage;
    Stage dialogStage;
    Scene scene;
    StackPane root;
    final private BorderPane bp = new BorderPane();
    
    private final Image imagePrev = new Image(getClass()
            .getResourceAsStream("rec/control_previous.png"));
    private final Image imagePlay = new Image(getClass()
            .getResourceAsStream("rec/control_play.png"));
    private final Image imageNext = new Image(getClass()
            .getResourceAsStream("rec/control_next.png"));
    Image imagePaus = new Image(getClass()
            .getResourceAsStream("rec/control_pause.png"));
    Image imagePrevDwn = new Image(getClass()
            .getResourceAsStream("rec/control_previous_down.png"));
    Image imagePlayDwn = new Image(getClass()
            .getResourceAsStream("rec/control_play_down.png"));
    Image imageNextDwn = new Image(getClass()
            .getResourceAsStream("rec/control_next_down.png"));
    Image imagePausDwn = new Image(getClass()
            .getResourceAsStream("rec/control_pause_down.png"));
    VBox leftBar;
    
    boolean viewingPlaylist = false;
    String viewingPlaylistName;
    
    int lastTableIndex;
    
    final private VBox bottomBar = new VBox();
    
    TableColumn tblcChecked;
    
    TableView tv;
    
    public static ObservableList<MusicItem> tableData
            = FXCollections.observableArrayList();
    
    @Override
    public void start(Stage primaryStage) {
        dmm.initiateStorage();
        setUpUI();
        
        scene = new Scene(bp, 700, 500);
        scene.getStylesheets().add(getClass().getResource("rec/style.css").toExternalForm());
        
        primaryStage.setTitle("Project Dynamite");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                dmp.shutDown();
                if(dialogStage != null) {
                    dialogStage.close();
                }
            }
        });
    }

    public void setUpUI() {
        bp.setTop(getTopBar());
        bp.setLeft(getLeftBar());
        bp.setCenter(getCenter());
        bp.setBottom(bottomBar);
    }
    
    public VBox getTopBar() {
        VBox vTopBar = new VBox();
        HBox topBar = new HBox();
        HBox leftControl = new HBox(8); // spacing is 8
        HBox leftVolume = new HBox();
        VBox left = new VBox();
        VBox middle = new VBox();
        HBox right = new HBox(5);
        
        leftControl.setAlignment(Pos.CENTER);
        leftVolume.setAlignment(Pos.CENTER);
        middle.setAlignment(Pos.CENTER);
        right.setAlignment(Pos.CENTER);
        
        // left top bar    
        Image imageVolumeUp = new Image(getClass().getResourceAsStream("rec/volumeUp.png"));
        Image imageVolumeDwn = new Image(getClass().getResourceAsStream("rec/volumeDwn.png"));
        
        final Button btnPrev = new Button("", new ImageView(imagePrev));
        final Button btnPlay = new Button("", new ImageView(imagePlay));
        final Button btnNext = new Button("", new ImageView(imageNext));
        
        btnPlay.getStyleClass().add("btnTrans");
        btnPrev.getStyleClass().add("btnTrans");
        btnNext.getStyleClass().add("btnTrans");
        
        // left volume
        final Slider sliderVolume = new Slider();
        sliderVolume.setValue(100);
        ImageView volumeUp = new ImageView(imageVolumeUp);
        ImageView volumeDwn = new ImageView(imageVolumeDwn);
        volumeUp.setFitWidth(20);
        volumeDwn.setFitWidth(20);
        volumeUp.setFitHeight(20);
        volumeDwn.setFitHeight(20);
        leftVolume.getChildren().addAll(volumeDwn, sliderVolume, volumeUp);
        
        // middle top bar
        final Label label = new Label("...");
        label.setAlignment(Pos.CENTER);
        label.setTextAlignment(TextAlignment.CENTER);
        final Slider slider = new Slider();
        slider.setDisable(true);
        middle.getChildren().addAll(label, slider);
        
        btnPlay.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                if(dmp.isPlaying()) {
                    btnPlay.setGraphic(new ImageView(imagePausDwn));
                } else {
                    btnPlay.setGraphic(new ImageView(imagePlayDwn));
                }
            }
        });
        
        btnPlay.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                if(dmp.isPlaying()) {
                    btnPlay.setGraphic(new ImageView(imagePaus));
                    dmp.pause();
                } else {
                    btnPlay.setGraphic(new ImageView(imagePlay));
                    dmp.play();
                }
            }
        });
        
        btnNext.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent arg0) {
                btnNext.setGraphic(new ImageView(imageNextDwn));
            }
        });
        
        btnNext.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent arg0) {
                btnNext.setGraphic(new ImageView(imageNext));
                playNextTrack(false);
            }
        });
        
        btnPrev.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent arg0) {
                btnPrev.setGraphic(new ImageView(imagePrevDwn));
            }
        });
        
        btnPrev.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent arg0) {
                btnPrev.setGraphic(new ImageView(imagePrev));
                playNextTrack(true);
            }
        });
        
        dmp.addOnTrackEndListener(new OnTrackEndListener() {
            @Override
            public void onEnd(String track) {
                btnPlay.setGraphic(new ImageView(imagePlay));
                playNextTrack(false);
            }
        });
        dmp.addOnTrackStartListener(new OnTrackStartListener() {
            @Override
            public void onStart(String track) {
                btnPlay.setDisable(false);
                btnPlay.setGraphic(new ImageView(imagePaus));
            }
        });
        dmp.addOnTrackPauseListener(new OnTrackPauseListener() {
            @Override
            public void onPause(String track) {
                btnPlay.setGraphic(new ImageView(imagePlay));
            }
        });
        
        sliderVolume.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent arg0) {
                dmp.setVolume(sliderVolume.getValue() * 0.01);
            }
        });
        
        dmp.addOnTrackProgressListener(new OnTrackProgressListener() {
            @Override
            public void onProgress(double currentTime, double stopTime) {
                if(!slider.isPressed()) {
                    double value = currentTime / stopTime;
                    value *= 100;
                    slider.setValue(value);
                }
            }
        });
        dmp.addOnTrackEndListener(new OnTrackEndListener() {
            @Override
            public void onEnd(String track) {
                slider.setDisable(true);
            }
        });
        dmp.addOnTrackStartListener(new OnTrackStartListener() {
            @Override
            public void onStart(String track) {
                slider.setDisable(false);
                try {
                    File file = new File(new URI(track));
                    ResultSet rs = dmm.getAudioFile(file.getAbsolutePath());
                    if(rs.next()) {
                        label.setText(rs.getString(2) + " by "
                                + rs.getString(3) + " on "
                                + rs.getString(4));
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                } catch (URISyntaxException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
        });
        
        slider.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                dmp.seekByPercent(slider.getValue());
            }
        });
        
        leftControl.getChildren().addAll(btnPrev, btnPlay, btnNext);
        
        left.getChildren().addAll(leftControl, leftVolume);
        
        // right top bar
        Button btnSearch = new Button();
        Image imageSearchBtn = new Image(getClass().getResourceAsStream("rec/searchIcon.png"));
        ImageView btnImage = new ImageView(imageSearchBtn);
        btnImage.setFitHeight(25);
        btnImage.setFitWidth(25);
        btnSearch.setGraphic(btnImage);
        btnSearch.setMaxHeight(25);
        btnSearch.setMaxWidth(25);
        final TextField tf = new TextField();
        tf.setMaxHeight(25);
        tf.setPromptText("Search music");
        tf.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                if(e.getCode().equals(KeyCode.ENTER)) {
                    if(tf.getText().length() > 2) {
                        ResultSet rs = dmm.getAllAudioFilesSearch(tf.getText());
                        viewingPlaylist = false;
                        fillMusicTable(rs);
                    }
                } else if(tf.getText().equals("")) {
                    viewingPlaylist = false;
                    fillMusicTable(dmm.getAllAudioFiles());
                }
            }
        });
        btnSearch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                if(!tf.getText().equals("")) {
                    viewingPlaylist = false;
                    ResultSet rs = dmm.getAllAudioFilesSearch(tf.getText());
                    fillMusicTable(rs);
                }
            }
        });
        right.getChildren().addAll(tf, btnSearch);
        
        leftControl.setMinWidth(200);
        middle.setMinWidth(300);
        right.setMinWidth(200);
        topBar.getChildren().addAll(left, middle, right);
        vTopBar.getChildren().addAll(getMenuBar(), topBar);
        return vTopBar;
    }
    
    public MenuBar getMenuBar() {
        MenuBar menuBar = new MenuBar();
        Menu menuFile = new Menu("File");
        MenuItem menuAddFile = new MenuItem("Add file to library");
        MenuItem menuAddFolder = new MenuItem("Add folder to libary");
        menuFile.getItems().addAll(menuAddFile, menuAddFolder);
        menuBar.getMenus().addAll(menuFile);
        
        menuAddFolder.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent ae) {
                final ProgressBar pb = new ProgressBar();
                final Label progressLabel = new Label();
                progressLabel.setGraphic(pb);
                final HBox hBox = new HBox(8);
                hBox.getChildren().add(progressLabel);
                bottomBar.getChildren().add(hBox);
                
                DirectoryChooser dc = new DirectoryChooser();
                dc.equals("Choose a music folder");
                final File dirToCrawl = dc.showDialog(scene.getWindow());
                Task progressTask = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        if(dirToCrawl != null) {
                            bp.setDisable(true);
                            MusicFolderCrawler mfc = new MusicFolderCrawler(new File(dirToCrawl.getAbsolutePath()));
                            mfc.addProgressListener(new OnProgressListener() {
                                @Override
                                public void onProgress(int type, int progress, int total) {
                                    // TODO : Implement a progress bar or something
                                    if(type == MusicFolderCrawler.FILESCHECKED) {
                                        updateMessage("Please wait... Found " + progress + " files out of " + total);
                                        updateProgress(progress / 2, total);
                                    }
                                }
                            });
                            mfc.addCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(ArrayList<File> files) {
                                    bp.setDisable(false);
                                    int i = 1;
                                    int iSize = files.size();
                                    for(File file : files) {
                                        dmm.addAudioFile(file, false);
                                        updateProgress(i, iSize);
                                        updateMessage("Added " + i + " files out of " + iSize + " to library");
                                        i++;
                                    }
                                    updateMessage("Done adding " + iSize + " files to library");
                                    succeeded();
                                }
                            });
                            mfc.start();
                        } else {
                            updateProgress(100, 100);
                            updateMessage("ERROR: Could not recognize music folder.");
                            succeeded();
                        }
                        return null;
                    }
                };
                pb.progressProperty().bind(progressTask.progressProperty());
                progressLabel.textProperty().bind(progressTask.messageProperty());
                progressTask.setOnSucceeded(new EventHandler() {
                    @Override
                    public void handle(Event e) {
                        try {
                            Button closeBtn = new Button("Close");
                            hBox.getChildren().add(closeBtn);
                            closeBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
                                @Override
                                public void handle(MouseEvent e) {
                                    hBox.getChildren().clear();
                                }
                            });
                            dmm.dbConnection.commit();
                            tableData.remove(0, tableData.size());
                            viewingPlaylist = false;
                            fillMusicTable(dmm.getAllAudioFiles());
                            bp.setLeft(getLeftBar());
                        } catch (SQLException ex) {
                            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
                Thread fxThread = new Thread(progressTask);
                fxThread.start();
            }
        });
        return menuBar;
    }

    public VBox getLeftBar() {
        VBox body = new VBox();
        
        body.setPrefWidth(175);
        
        // Music Item
        Image musicIcon = new Image(getClass()
                .getResourceAsStream("rec/ico_music.png"));
        
        
        ImageView musicImage = new ImageView(musicIcon);
        
        musicImage.setFitHeight(20);
        musicImage.setFitWidth(20);
        
        
        TreeItem<String> musicItem;
        musicItem = new TreeItem<> ("Music", musicImage);
        TreeItem<String> artistsItem;
        artistsItem = new TreeItem<> ("Artists");
        
        musicItem.getChildren().add(artistsItem);

        ResultSet rs = dmm.getDistinctArtist();
        
        ArrayList<String> artistList;
        artistList = new ArrayList<>();
        try {
            while(rs.next()) {
                String artist = rs.getString(1);
                if(artist.equals("")) {
                    artist = "Unknown";
                }
                artistList.add(artist);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        for(String artist : artistList) {
            TreeItem<String> artistItem;
            artistItem = new TreeItem<> (artist);
            artistsItem.getChildren().add(artistItem);
            ResultSet rs2 = dmm.getDistinctAlbumsByArtist(artist);
            try {
                while(rs2.next()) {
                    String album = rs2.getString(1);
                    if(album.equals("")) {
                        album = "Unknown";
                    }
                    TreeItem<String> albumItem;
                    albumItem = new TreeItem<> (album);
                    artistItem.getChildren().add(albumItem);
                }
            } catch (SQLException ex) {
                Logger.getLogger(Main.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }

        musicItem.setExpanded(true);
        
        final TreeView<String> musicTreeView;
        musicTreeView = new TreeView<>(musicItem);
        
        musicTreeView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                MouseButton mb = e.getButton();
                if(mb == MouseButton.PRIMARY) {
                    TreeItem ti = musicTreeView.getSelectionModel().getSelectedItem();
                    TreeItem tiParent = ti.getParent();
                    String itemValue = (String) ti.getValue();
                    if(tiParent != null) {
                        String parentValue = (String) tiParent.getValue();
                        if(itemValue.equals("Unknown")) {
                            itemValue = "";
                        }
                        if(itemValue.equals("Artists")) {
                            ti.setExpanded(true);
                        } else if(parentValue.equals("Artists")) {
                            // fill music table specific by album
                            viewingPlaylist = false;
                            fillMusicTable(dmm.getFilesFromArtist(itemValue));
                        } else {
                            // fill music table specific by artist
                            viewingPlaylist = false;
                            fillMusicTable(dmm
                                    .getFilesFromAlbum(parentValue, itemValue));
                        }
                    } else {
                        viewingPlaylist = false;
                        fillMusicTable(dmm.getAllAudioFiles());
                    }
                }
            }
        });
                
        body.getChildren().addAll(musicTreeView, getPlaylistTreeView());
        leftBar = body;
        return body;
    }
    
    public TreeView<String> getPlaylistTreeView() {
        Image playlistIcon = new Image(getClass()
                .getResourceAsStream("rec/ico_playlist.png"));
        ImageView playlistImage = new ImageView(playlistIcon);
        playlistImage.setFitHeight(20);
        playlistImage.setFitWidth(20);
        TreeItem<String> playlistItem;
        playlistItem = new TreeItem<> ("Playlist", playlistImage);
        playlistItem.setExpanded(true);
        
        TreeView<String> playlistTreeView;
        playlistTreeView = new TreeView<> (playlistItem);
        
        ContextMenu cm = new ContextMenu();
        MenuItem addPlaylist = new MenuItem("Add Playlist");
        final MenuItem deletePlaylist = new MenuItem("");
        
        deletePlaylist.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                String deleteText = deletePlaylist.getText();
                if(!deleteText.equals("")) {
                    String item = deleteText.substring(7, deleteText.length());
                    dmm.removePlaylist(item);
                    leftBar.getChildren().remove(1);
                    leftBar.getChildren().add(getPlaylistTreeView());
                }
            }
        });
        addPlaylist.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                showCreatePlaylistDialog();
            }
        });
        
        cm.getItems().addAll(addPlaylist, deletePlaylist);
        
        ResultSet rs = dmm.getAllPlaylists();
        try {
            while(rs.next()) {
                TreeItem<String> playlistChild = new TreeItem<>(rs.getString(1));
                playlistItem.getChildren().add(playlistChild);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        playlistTreeView.setContextMenu(cm);
        
        playlistTreeView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                try {
                    String item;
                    String className = t.getTarget().getClass().getName();
                    System.out.println();
                    if(className.equals("com.sun.javafx.scene.control.skin.LabeledText")) {
                        LabeledText lt = (LabeledText) t.getTarget();
                        item = lt.getText();
                    } else {
                        TreeCellSkin tcs = (TreeCellSkin) t.getTarget();
                        LabeledText lt = (LabeledText) tcs.getChildren().get(0);
                        item = lt.getText();
                    }
                    if(t.getButton() == MouseButton.PRIMARY) {
                        viewingPlaylist = true;
                        viewingPlaylistName = item;
                        fillMusicTable(dmm.getFilesFromPlaylist(item));
                    } else if(t.getButton() == MouseButton.SECONDARY) {
                        if(!item.equals("Playlist")) {
                            deletePlaylist.setText("Delete " + item);
                        } else {
                            deletePlaylist.setText("");
                        }
                        
                    }
                } catch(Exception e) {
                    deletePlaylist.setText("");
                }
            }
        });
        
        return playlistTreeView;
    }
    
    public TableView getCenter() {
        tv = new TableView();
        
        // Context menu...
        final ContextMenu cm = new ContextMenu();
        tv.setContextMenu(cm);
        
        // Clustered GUI behavior. Please read carefully (sorry).
        tv.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                if(t.getClickCount() == 2) {
                    MusicItem mi = (MusicItem) tv.getFocusModel().getFocusedItem();
                    dmp.playFile("file:///"
                            + FileHelper.fixFileUri(mi.getLocation()));
                } else if(t.getButton().equals(MouseButton.SECONDARY)) {
                    try {
                        cm.getItems().clear();
                        ResultSet rs = dmm.getAllPlaylists();
                        Menu miAddTo = new Menu("Add Checked To Playlist");
                        while(rs.next()) {
                            final MenuItem miAddToThis = new MenuItem(rs.getString(1));
                            miAddTo.getItems().add(miAddToThis);
                            miAddToThis.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent t) {
                                    String item = miAddToThis.getText();
                                    addCheckedItemsToPlaylist(item);
                                }
                            });
                        }
                        if(viewingPlaylist) {
                            MenuItem miRemove = new MenuItem("Remove checked from " + viewingPlaylistName);
                            cm.getItems().add(miRemove);
                            miRemove.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent t) {
                                    ObservableList<MusicItem> ol = tv.getItems();
                                    for(MusicItem mi : ol) {
                                        if(mi.getChecked()) {
                                            dmm.removeFileFromPlaylist(viewingPlaylistName, mi.getLocation());
                                        }
                                    }
                                    fillMusicTable(dmm.getFilesFromPlaylist(viewingPlaylistName));
                                }
                            });
                        }
                        
                        MenuItem miCreate = new MenuItem("Create Playlist");
                        
                        miCreate.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent t) {
                                showCreatePlaylistDialog();
                            }
                        });
                        
                        cm.getItems().addAll(miCreate, miAddTo);
                        tv.getContextMenu().show(scene.getWindow());
                    } catch (SQLException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        
        Callback<TableColumn<MusicItem, Boolean>, TableCell<MusicItem, Boolean>> booleanCellFactory = 
            new Callback<TableColumn<MusicItem, Boolean>, TableCell<MusicItem, Boolean>>() {
                @Override
                public TableCell<MusicItem, Boolean> call(TableColumn<MusicItem, Boolean> p) {
                    return new BooleanCell();
            }
        };

        tblcChecked = new TableColumn();
        TableColumn tblcName = new TableColumn("Name");
        TableColumn tblcArtist = new TableColumn("Artist");
        TableColumn tblcAlbum = new TableColumn("Album");
        TableColumn tblcGenre = new TableColumn("Genre");
        TableColumn tblcTrack = new TableColumn("Track");
        TableColumn tblcTime = new TableColumn("Time");
        
        tblcChecked.setPrefWidth(22);
        tblcName.setPrefWidth(140);
        tblcArtist.setPrefWidth(140);
        tblcAlbum.setPrefWidth(140);
        tblcGenre.setPrefWidth(75);
        
        tblcChecked.setResizable(false);

        tblcChecked.setCellValueFactory(new PropertyValueFactory<MusicItem, Boolean>("checked"));
        tblcChecked.setCellFactory(booleanCellFactory);
        tblcName.setCellValueFactory(new PropertyValueFactory<MusicItem, String>("name"));
        tblcArtist.setCellValueFactory(new PropertyValueFactory<MusicItem, String>("artist"));
        tblcAlbum.setCellValueFactory(new PropertyValueFactory<MusicItem, String>("album"));
        tblcGenre.setCellValueFactory(new PropertyValueFactory<MusicItem, String>("genre"));
        tblcTrack.setCellValueFactory(new PropertyValueFactory<MusicItem, String>("track"));
        tblcTime.setCellValueFactory(new PropertyValueFactory<MusicItem, String>("time"));
        
        tv.getColumns().addAll(tblcChecked, tblcName, tblcArtist, tblcAlbum, tblcGenre, tblcTrack, tblcTime);
        
        tv.getSelectionModel().setCellSelectionEnabled(true);
        
        tv.setItems(tableData);
        viewingPlaylist = false;
        fillMusicTable(dmm.getAllAudioFiles());        
        return tv;
    }
    
    class BooleanCell extends TableCell<MusicItem, Boolean> {
        private CheckBox checkBox;
        public BooleanCell() {
            checkBox = new CheckBox();
            
            checkBox.selectedProperty().addListener(new ChangeListener<Boolean> () {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    MusicItem mi = (MusicItem) getTableRow().getItem();
                    if(mi == null) {
                        checkBox.setDisable(true);
                    } else {
                        mi.setChecked(newValue);
                    }
                }
            });
            
            // Select multiple checkbox's while holding shift
            checkBox.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent t) {
                    TableCell tc = (TableCell) checkBox.getParent().getParent();
                    TableRow tr = tc.getTableRow();
                    int index = tr.getIndex();
                    TableViewSelectionModel tvsm = tv.getSelectionModel();
                    if(t.isShiftDown()) {
                        checkBox.setSelected(true);
                        if(Integer.valueOf(lastTableIndex) != null) {
                            ObservableList<MusicItem> musicItems = tv.getItems();
                            while(index != lastTableIndex) {
                                if(index < lastTableIndex) {
                                    lastTableIndex--;
                                } else if(index > lastTableIndex) {
                                    lastTableIndex++;
                                }
                                tvsm.select(lastTableIndex, tblcChecked);
                                MusicItem mi = musicItems.get(lastTableIndex);
                                mi.setChecked(true);
                            }                        
                        }
                    }
                    lastTableIndex = index;
                }
            });
            this.setGraphic(checkBox);
        }
        @Override
        public void updateItem(Boolean item, boolean empty) {
            super.updateItem(item, empty);
            if(!empty) {
                checkBox.setSelected(item);
            } else {
                checkBox.setDisable(true);
            }
        }
        @Override
        public void updateSelected(boolean b) {
            super.updateSelected(b);
            checkBox.setSelected(true);
        }
    }
    
    public void playNextTrack(boolean previous) {
        if(previous) {
            tv.getFocusModel().focusPrevious();
        } else {
            tv.getFocusModel().focusNext();
        }
        MusicItem mi = (MusicItem) tv.getFocusModel().getFocusedItem();
        dmp.playFile("file:///" + FileHelper.fixFileUri(mi.getLocation()));
    }
    
    public void fillMusicTable(ResultSet audioFiles) {
        try {
            tableData.remove(0, tableData.size());
            while(audioFiles.next()) {
                tableData.add(new MusicItem(
                    audioFiles.getString(2),
                    audioFiles.getString(3),
                    audioFiles.getString(4),
                    audioFiles.getString(16),
                    audioFiles.getString(6),
                    audioFiles.getString(12),
                    audioFiles.getString(1),
                    false
                ));
            }
        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void showCreatePlaylistDialog() {
        final Stage dialog = new Stage(StageStyle.TRANSPARENT);
        dialogStage = dialog;
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(stage);
        VBox vb = new VBox(8);
        HBox hBox = new HBox(8);
        Scene dialogScene = new Scene(vb);
        dialog.setScene(dialogScene);
        final TextField tf = new TextField();
        Button bOk = new Button("Ok");
        Button bCancel = new Button("Cancel");
        hBox.getChildren().addAll(bOk, bCancel);
        vb.getChildren().addAll(tf, hBox);
        vb.setAlignment(Pos.CENTER);
        hBox.setAlignment(Pos.CENTER);
        vb.setPadding(new Insets(8));

        bOk.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                if(!tf.getText().equals("")) {
                    addPlaylist(tf.getText());
                    dialog.close();
                }
            }
        });
        bCancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                dialog.close();
            }
        });
        tf.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                if(t.getCode() == KeyCode.ENTER) {
                    if(!tf.getText().equals("")) {
                        addPlaylist(tf.getText());
                        dialog.close();
                    }
                }
            }
        });
        dialog.show();
    }
    
    public void addPlaylist(String name) {
        System.out.println("Adding a playlist called " + name);
        dmm.addPlaylist(name, true);
        leftBar.getChildren().remove(1);
        leftBar.getChildren().add(getPlaylistTreeView());
    }
    
    public void addCheckedItemsToPlaylist(String playlistName) {
        System.out.println("Adding these checked items to " + playlistName);
        ObservableList<MusicItem> tvData = tv.getItems();
        for(MusicItem mi : tvData) {
            if(mi.getChecked()) {
                dmm.addToPlaylist(playlistName, mi.getLocation());
            }
        }
    }
}