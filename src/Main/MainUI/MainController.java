package Main.MainUI;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSlider;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.jaudiotagger.audio.AudioFileIO;

public class MainController implements Initializable {

    @FXML
    private AnchorPane window;
    @FXML
    private JFXButton shuffle;
    @FXML
    private JFXButton previous;
    @FXML
    private JFXButton play;
    @FXML
    private JFXButton next;
    @FXML
    private JFXButton repeat;
    @FXML
    private JFXButton minimize;
    @FXML
    private JFXButton close;
    @FXML
    private ImageView shuffleIcon;
    @FXML
    private ImageView previousIcon;
    @FXML
    private ImageView playIcon;
    @FXML
    private ImageView nextIcon;
    @FXML
    private ImageView repeatIcon;
    @FXML
    private ImageView dragIcon;
    @FXML
    private ImageView minimizeIcon;
    @FXML
    private ImageView closeIcon;
    @FXML
    private JFXSlider progress;
    @FXML
    private JFXSlider volume;
    @FXML
    private JFXSlider slowed;
    @FXML
    private Text name;
    @FXML
    private Text played;
    @FXML
    private Text remaining;

    private final List<File> songs = new ArrayList<>();
    private List<File> shuffledSongs = new ArrayList<>();

    private MediaPlayer player;
    private boolean playing = false;
    private File currentSong;
    private double songPlayed;
    private double songRemaining;
    private int songDuration;

    private boolean shuffling = false;
    private int repeating = 0;

    private ListIterator<File> list;
    private boolean dragging = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        volume.setValue(100);
        List<File> mp3 = new ArrayList<>();
        getSongs(new File(System.getProperty("user.home") + "\\Music"), mp3);

        if (!songs.isEmpty()) {
            currentSong = songs.get(0);
            name.setText(currentSong.getName().substring(0, currentSong.getName().length() - 4));

            getSongDetails(currentSong);
            player = new MediaPlayer(new Media(songs.get(0).toURI().toString()));
            player.setRate(slowed.getValue() / 100 + 0.50);
            player.setVolume(volume.getValue() / 100);
            setEndOfMedia();
        } else {
            name.setText("No song loaded.");
        }
        list = songs.listIterator();

        shuffleIcon.setImage(new Image(new java.io.File("images\\shuffle_0.png").toURI().toString()));
        previousIcon.setImage(new Image(new java.io.File("images\\previous.png").toURI().toString()));
        playIcon.setImage(new Image(new java.io.File("images\\play.png").toURI().toString()));
        nextIcon.setImage(new Image(new java.io.File("images\\next.png").toURI().toString()));
        repeatIcon.setImage(new Image(new java.io.File("images\\repeat_0.png").toURI().toString()));
        dragIcon.setImage(new Image(new java.io.File("images\\drag.png").toURI().toString()));
        minimizeIcon.setImage(new Image(new java.io.File("images\\minimize.png").toURI().toString()));
        closeIcon.setImage(new Image(new java.io.File("images\\close.png").toURI().toString()));

        Timer ticker = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (playing) {
                    double speed = slowed.getValue() / 200 + 0.25;

                    songPlayed += speed;
                    songRemaining -= speed;

                    played.setText(convertToTime(songPlayed));
                    remaining.setText("-" + convertToTime(songRemaining));
                    double currentProg = 100 - songRemaining / songDuration * 100;

                    if (!dragging) {
                        progress.setValue(currentProg);
                    }
                }
            }
        };
        ticker.schedule(task, 0, 500);

        eventHandlers();
    }

    private String convertToTime(double played) {
        played *= 1000L;

        long minutes = TimeUnit.MILLISECONDS.toMinutes((long) played);
        played -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds((long) played);

        if (minutes < 10 && seconds < 10) {
            return "0" + minutes + ":0" + seconds;
        } else if (minutes < 10) {
            return "0" + minutes + ":" + seconds;
        } else if (seconds < 10) {
            return "00:0" + seconds;
        } else {
            return "00:" + seconds;
        }
    }

    private void eventHandlers() {
        shuffle.setOnAction(e -> {
            shuffling = !shuffling;

            if (!shuffling) {
                shuffleIcon.setImage(new Image(new java.io.File("images\\shuffle_0.png").toURI().toString()));
                list = songs.listIterator();
            } else {
                shuffleIcon.setImage(new Image(new java.io.File("images\\shuffle_1.png").toURI().toString()));
                shuffledSongs = new ArrayList<>(songs);
                Collections.shuffle(shuffledSongs);
                list = shuffledSongs.listIterator();
            }
        });

        previous.setOnAction(e -> {
            if (songs.size() > 1) {
                resetIterator();

                while (list.hasNext()) {
                    File file = list.next();
                    if (file == currentSong && list.hasPrevious()) {
                        list.previous();

                        if (list.hasPrevious()) {
                            File previous = list.previous();
                            loadSong(previous);
                        } else {
                            File last = null;
                            while (list.hasNext())
                                last = list.next();

                            assert last != null;
                            loadSong(last);
                        }
                        break;
                    }
                }
            } else if (songs.size() == 1) {
                loadSong(currentSong);
            }
        });

        play.setOnAction(e -> {
            if (playing) {
                player.pause();
                playIcon.setImage(new Image(new java.io.File("images\\play.png").toURI().toString()));
                playing = false;
            } else {
                player.play();
                playIcon.setImage(new Image(new java.io.File("images\\pause.png").toURI().toString()));
                playing = true;
            }
        });

        next.setOnAction(e -> playNext());

        repeat.setOnAction(e -> {
            if (repeating == 0) {
                repeating++;
                repeatIcon.setImage(new Image(new java.io.File("images\\repeat_1.png").toURI().toString()));
            } else if (repeating == 1) {
                repeating++;
                repeatIcon.setImage(new Image(new java.io.File("images\\repeat_2.png").toURI().toString()));
            } else {
                repeating = 0;
                repeatIcon.setImage(new Image(new java.io.File("images\\repeat_0.png").toURI().toString()));
            }
        });

        progress.setOnMousePressed(e -> dragging = true);

        progress.setOnMouseReleased(e -> {
            dragging = false;
            progress.setLabelFormatter(new StringConverter<>() {
                @Override
                public String toString(Double v) {
                    return convertToTime(songDuration / (100 / v));
                }

                @Override
                public Double fromString(String s) {
                    return null;
                }
            });
            songPlayed = songDuration * progress.getValue() / 100;
            songRemaining = songDuration - songPlayed;
            played.setText(convertToTime(songPlayed));
            remaining.setText("-" + convertToTime(songRemaining));

            player.seek(Duration.millis(songPlayed * 1000));
        });

        volume.valueProperty().addListener((value, aBoolean, t1) -> {
            if (currentSong != null && player != null) {
                player.setVolume(volume.getValue() / 100);
            }
        });

        slowed.valueProperty().addListener((value, aBoolean, t1) -> {
            if (currentSong != null && player != null) {
                player.setRate(slowed.getValue() / 100 + 0.50);
            }
        });

        window.setOnMousePressed(pressEvent -> dragIcon.setOnMouseDragged(dragEvent -> {
            window.getScene().getWindow().setX(dragEvent.getScreenX() - pressEvent.getSceneX());
            window.getScene().getWindow().setY(dragEvent.getScreenY() - pressEvent.getSceneY());
        }));

        minimize.setOnAction(e -> {
            Stage stage = (Stage) window.getScene().getWindow();
            stage.setIconified(true);
        });

        close.setOnAction(e -> System.exit(1));
    }

    private void resetIterator() {
        if (shuffling) {
            list = shuffledSongs.listIterator();
        } else {
            list = songs.listIterator();
        }
    }

    private void loadSong(File song) {
        currentSong = song;
        name.setText(song.getName().substring(0, song.getName().length() - 4));

        if (player != null) {
            player.dispose();
        }
        player = new MediaPlayer(new Media(song.toURI().toString()));
        player.setRate(slowed.getValue() / 100 + 0.50);
        player.setVolume(volume.getValue() / 100);
        getSongDetails(song);
        setEndOfMedia();

        playing = true;
        player.play();
        playIcon.setImage(new Image(new java.io.File("images\\pause.png").toURI().toString()));
    }

    private void setEndOfMedia() {
        player.setOnEndOfMedia(() -> {
            if (repeating == 0) {
                playNext();
            } else if (repeating == 1) {
                repeatSong();
                repeating = 0;
                repeatIcon.setImage(new Image(new java.io.File("images\\repeat_0.png").toURI().toString()));
            } else {
                repeatSong();
            }
        });
    }

    private void getSongDetails(File song) {
        try {
            songPlayed = 0;
            songDuration = AudioFileIO.read(song).getAudioHeader().getTrackLength();
            songRemaining = songDuration;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playNext() {
        if (songs.size() > 1) {
            resetIterator();

            while (list.hasNext()) {
                File file = list.next();
                if (file == currentSong) {
                    if (list.hasNext()) {
                        File next = list.next();
                        loadSong(next);
                    } else {
                        File first = null;
                        while (list.hasPrevious())
                            first = list.previous();

                        assert first != null;
                        loadSong(first);
                    }
                    break;
                }
            }
        } else if (songs.size() == 1) {
            loadSong(currentSong);
        }
    }

    private void repeatSong() {
        player.stop();
        player.seek(Duration.seconds(0));
        songPlayed = 0;
        songRemaining = songDuration;
        player.play();
    }

    private void getSongs(File root, List<File> files) {
        File[] fList = root.listFiles();

        if (fList != null) {
            for (File file : fList) {
                if (file.isFile()) {
                    files.add(file);
                } else if (file.isDirectory()) {
                    getSongs(file, files);
                }
            }
        }

        for (File file : files) {
            if (file.getName().endsWith(".mp3")) {
                try {
                    songDuration = AudioFileIO.read(file).getAudioHeader().getTrackLength();
                    songs.add(file);
                } catch (Exception e) {
                    //ignore
                }
            }
        }

        Set<File> set = new HashSet<>(songs);
        songs.clear();
        songs.addAll(set);
    }

}
