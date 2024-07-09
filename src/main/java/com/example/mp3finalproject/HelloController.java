package com.example.mp3finalproject;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.util.*;

import static jdk.jfr.internal.consumer.EventLog.stop;

public class HelloController implements Initializable {
    private final ArrayList<Music>  musicList = new ArrayList<>();//存放音樂
    private int curr = 0;//目前音樂
    private MediaPlayer PrimaryMediaPlayer;//主播放
    private String PlayStatus = "default";//播放狀態 預設(順播),隨機,循環

    private final Image ImagePause = new Image(Objects.requireNonNull(getClass().getResourceAsStream("pause.png")));
    private final Image ImageStart = new Image(Objects.requireNonNull(getClass().getResourceAsStream("PlayButton.png")));


    @FXML
    private HBox hbox;

    @FXML
    private Circle RepeatCircle;

    @FXML
    private ImageView ImageState;


    @FXML
    private VBox Scroll;

    @FXML
    private Label SongName, deleteLabel;

    @FXML
    private ImageView delete, add;

    private ImageView image = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("ListIcon.png"))));

    @FXML
    private Button sureButton, cancelButton;

    @FXML
    private Label speedButton;
    @FXML
    private Slider musicPlayBar;

    @FXML
    private Slider volumeBar;
    @FXML
    private ImageView playMode;

    //時間軸需要的東東
    Timer timer;
    TimerTask timerTask;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        SongName.setText("目前沒有音樂~~");
        SongName.setFont(Font.font(15));
        SongName.setMaxWidth(300);
        SongName.setMinWidth(300);

    }


    //添加音樂
    @FXML
    void AddMusic(MouseEvent event) {
        Stage stage = (Stage) hbox.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MP3","*.mp3"));
        fileChooser.setTitle("選擇MP3");
        List<File> files = fileChooser.showOpenMultipleDialog(stage);
        if(files != null){
            for(File i: files){
                String road = i.toString();
                String name = i.getName().replace(".mp3","");
                musicList.add(new Music(road,name));
                for(Music j: musicList){
                    if(!j.getChoose()){
                        j.ISChoose();
                    }
                }
                SongName.setText(musicList.get(curr).getMusicName());
            }
        }
        setMusicList();
    }

    //叉叉
    @FXML
    void Close(MouseEvent event) {
        Stage stage = (Stage) hbox.getScene().getWindow();//關閉視窗~~
        if(musicList.size() > 0){
            for(Music m: musicList){
                m.clearUp();
            }
        }
        if(PrimaryMediaPlayer != null){
            PrimaryMediaPlayer.dispose();
        }
        stage.close();
        System.exit(0);
    }

    //播放鍵
    @FXML
    void Start(MouseEvent event) {
        if(PrimaryMediaPlayer != null  && !musicList.isEmpty()){
            if(PrimaryMediaPlayer.getStatus() == MediaPlayer.Status.PLAYING){//確認mediaPlayer的狀態
                PrimaryMediaPlayer.pause();
                ImageState.setImage(ImageStart);
            } else{
                PrimaryMediaPlayer.play();
                ImageState.setImage(ImagePause);
                Status();
            }

        }
        else if(!musicList.isEmpty()){
            ImageState.setImage(ImagePause);
            PrimaryMediaPlayer = musicList.get(curr).getMediaPlayer();
            SongName.setText(musicList.get(curr).getMusicName());
            PrimaryMediaPlayer.play();
            Status();
        }

        if(PrimaryMediaPlayer != null){
            progressBarBegin();
            setVolumeBar();
        }
    }

    //上一首
    @FXML
    void Previous(MouseEvent event) {
        if(curr > 0){
            curr -= 1;
            ImageState.setImage(ImagePause);
            ChangeMusic();
            Status();
        }else if (curr == 0){
            curr = musicList.size()-1;
            ChangeMusic();
            ImageState.setImage(ImagePause);
            Status();
        }
        setMusicList();
    }

    //下一首
    @FXML
    void Next(MouseEvent event) {
        if(curr < musicList.size()-1){
            curr +=1;
            ChangeMusic();
            ImageState.setImage(ImagePause);
            Status();
        } else if (curr == musicList.size()-1){
            curr = 0;
            ChangeMusic();
            ImageState.setImage(ImagePause);
            Status();
        }
        setMusicList();

    }

    //依播放模式切歌
    public void Status(){
        if(PrimaryMediaPlayer != null){
            PrimaryMediaPlayer.setOnEndOfMedia(new Runnable() {
                @Override
                public void run() {
                    switch (PlayStatus) {
                        case "default":
                            if(musicList.size() >= curr + 2) {
                                curr++;
                            } else {
                                curr = 0;
                            }
                            ChangeMusic();
                            PrimaryMediaPlayer.play();
                            ImageState.setImage(ImagePause);
                            break;
                        case "repeat":
                            PrimaryMediaPlayer.seek(PrimaryMediaPlayer.getStartTime());
                            break;

                        case "random":
                            int random = (int) (Math.random() * musicList.size());
                            if(random == curr){
                                PrimaryMediaPlayer.seek(PrimaryMediaPlayer.getStartTime());
                            }
                            else{
                                curr = random;
                                ChangeMusic();
                                PrimaryMediaPlayer.play();
                                ImageState.setImage(ImagePause);
                            }
                            break;
                    }

                    Status();

                }
            });
        }
    }

    //切換音樂
    void ChangeMusic(){
        PrimaryMediaPlayer.stop();
        PrimaryMediaPlayer.seek(PrimaryMediaPlayer.getStartTime());
        PrimaryMediaPlayer = musicList.get(curr).getMediaPlayer();
        PrimaryMediaPlayer.play();
        SongName.setText(musicList.get(curr).getMusicName());
        progressBarBegin();
        setVolumeBar();
        speedButton.setText("1X");
        PrimaryMediaPlayer.setRate(1.0);
    }

    //隨機撥放
    @FXML
    void Random(MouseEvent event) {
        RepeatCircle.setFill(Color.WHITE);
        if(PlayStatus.equals("random")){
            PlayStatus = "default";
            playMode.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("takeTurn.png"))));
        }
        else{
            PlayStatus = "random";
            playMode.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("random.png"))));
        }

    }

    //重複播放
    @FXML
    void Repeat(MouseEvent event) {

        if(PlayStatus.equals("repeat")){
            PlayStatus = "default";
            RepeatCircle.setFill(Color.WHITE);
        }

        else{
            PlayStatus = "repeat";
            RepeatCircle.setFill(Color.RED);
        }
    }

    //生成音樂列表
    @FXML
    public void setMusicList() {
        Image image1 = image.getImage();
        Scroll.getChildren().clear();
        String cssLayout = """
                -fx-border-color: black;
                -fx-border-radius: 15;
                """;
        for (Music m: musicList) {
            ImageView imageView = new ImageView(image1);
            imageView.setFitHeight(38);
            imageView.setFitWidth(40);
            Label name = new Label(m.getMusicName());
            name.setFont(new Font("Bold Italic", 15));
            CheckBox checkBox = new CheckBox();
            checkBox.setVisible(false);
            checkBox.setDisable(true);
            HBox hBox = new HBox(imageView, checkBox, name);
            hBox.setPrefWidth(318);
            hBox.setPrefHeight(48);
            hBox.setFillHeight(true);
            hBox.setSpacing(5);
            hBox.setStyle(cssLayout);
            hBox.setPadding(new Insets(4, 0, 0, 5));
            hBox.setAlignment(Pos.CENTER_LEFT);
            hBox.setOnMouseClicked((new EventHandler<MouseEvent>() {
                public void handle(MouseEvent event) {
                    if (PrimaryMediaPlayer != null) {
                        PrimaryMediaPlayer.pause();
                    }
                    PrimaryMediaPlayer = m.getMediaPlayer();
                    PrimaryMediaPlayer.setRate(1.0);
                    setVolumeBar();
                    speedButton.setText("1X");
                    PrimaryMediaPlayer.play();
                    SongName.setText(m.getMusicName());
                }
            }));
            Scroll.getChildren().add(hBox);
        }
    }

    //刪除歌曲
    @FXML
    public void three() {
        add.setVisible(false);
        add.setDisable(true);
        delete.setVisible(false);
        delete.setDisable(true);
        deleteLabel.setVisible(true);
        deleteLabel.setDisable(false);
        sureButton.setVisible(true);
        sureButton.setDisable(false);
        cancelButton.setVisible(true);
        cancelButton.setDisable(false);
        for (int a = 0; a < Scroll.getChildren().size(); a ++) {
            HBox delHBox = (HBox) Scroll.getChildren().get(a);
            CheckBox delCheck = (CheckBox) delHBox.getChildren().get(1);
            delCheck.setVisible(true);
            delCheck.setDisable(false);
        }
    }

    //關閉刪除用方框
    @FXML
    public void deleteCancel() {
        setMusicList();
        add.setVisible(true);
        add.setDisable(false);
        delete.setVisible(true);
        delete.setDisable(false);
        deleteLabel.setVisible(false);
        deleteLabel.setDisable(true);
        sureButton.setVisible(false);
        sureButton.setDisable(true);
        cancelButton.setVisible(false);
        cancelButton.setDisable(true);
    }

    //確認刪除方框
    @FXML
    public void deleteSure() {
        ArrayList<String> deleteName = new ArrayList<>();
        for (int a = 0; a < Scroll.getChildren().size(); a ++) {
            HBox delHBox = (HBox) Scroll.getChildren().get(a);
            CheckBox delCheck = (CheckBox) delHBox.getChildren().get(1);
            if (delCheck.isSelected()) {
                assert false;
                Label del = (Label) delHBox.getChildren().get(2);
                deleteName.add(del.getText());
            }
        }
        while (true) {
            assert false;
            for (String del: deleteName) {
                for (Music music: musicList) {
                    if(musicList.get(curr).getMusicName().equals(del)){
                        musicPlayBar.setValue(0.0);
                        PrimaryMediaPlayer.dispose();
                        SongName.setText("目前沒有音樂~~");
                    }
                    if (music.getMusicName().equals(del)) {
                        musicList.remove(music);
                        deleteName.remove(del);
                        break;
                    }
                }
                break;
            }
            if (deleteName.size() == 0) {
                break;
            }
        }
        setMusicList();
        add.setVisible(true);
        add.setDisable(false);
        delete.setVisible(true);
        delete.setDisable(false);
        deleteLabel.setVisible(false);
        deleteLabel.setDisable(true);
        sureButton.setVisible(false);
        sureButton.setDisable(true);
        cancelButton.setVisible(false);
        cancelButton.setDisable(true);
    }


    //時間軸
    public void progressBarBegin(){

        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                double current = PrimaryMediaPlayer.getCurrentTime().toSeconds();
                double end = PrimaryMediaPlayer.getMedia().getDuration().toSeconds();

                PrimaryMediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
                    @Override
                    public void changed(ObservableValue<? extends Duration> observableValue, Duration duration, Duration t1) {
                        musicPlayBar.setValue(current);
                    }
                });

                musicPlayBar.setOnMousePressed(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        PrimaryMediaPlayer.seek(Duration.seconds(musicPlayBar.getValue()));
                    }
                });

                musicPlayBar.setOnMouseDragged(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        PrimaryMediaPlayer.seek(Duration.seconds(musicPlayBar.getValue()));
                    }
                });

                if(PrimaryMediaPlayer != null){
                    musicPlayBar.setMax(PrimaryMediaPlayer.getMedia().getDuration().toSeconds());
                }

                if (current/end == 1){
                    progressBarCancel();
                }
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, 500);
    }

    public void progressBarCancel(){
        timer.cancel();
    }

    //音量軸
    public void setVolumeBar(){
        volumeBar.setMin(0.0);
        volumeBar.setMax(1.0);
        volumeBar.setValue(1.0);
        volumeBar.valueProperty().bindBidirectional(
                PrimaryMediaPlayer.volumeProperty()
        );
    }
    //調速按鈕
    public void setSpeed(MouseEvent event){
        if(PrimaryMediaPlayer != null) {
            if (speedButton.getText().equals("1X")) {
                speedButton.setText("1.5X");
                PrimaryMediaPlayer.setRate(1.5);
            }
            else if (speedButton.getText().equals("1.5X")) {
                speedButton.setText("2X");
                PrimaryMediaPlayer.setRate(2.0);
            }
            else if (speedButton.getText().equals("2X")) {
                speedButton.setText("0.5X");
                PrimaryMediaPlayer.setRate(0.5);
            }
            else if (speedButton.getText().equals("0.5X")) {
                speedButton.setText("1X");
                PrimaryMediaPlayer.setRate(1.0);
            }
        }
    }
}

//要用的Class
class Music{
    private MediaPlayer mediaPlayer;
    private String MusicName = "Default";
    private boolean isChoose = false;
    private File file;

    Music(String road,String MusicName){
        this.file = new File(road);
        Media media = new Media(file.toURI().toString());
        this.MusicName = MusicName;
        this.mediaPlayer = new MediaPlayer(media);
    }


    public void ISChoose(){this.isChoose = true;}

    public boolean getChoose(){return isChoose;}

    public String getMusicName(){return MusicName;}

    public MediaPlayer getMediaPlayer(){return mediaPlayer;}

    public void clearUp(){
        this.file.delete();
        mediaPlayer.dispose();
    }

}