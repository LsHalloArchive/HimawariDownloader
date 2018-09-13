package tk.lshallo.himawari;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

public class UI {

    @FXML
    private MenuItem MenuItemSaveAs;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private DatePicker DatePicker;

    @FXML
    private ChoiceBox<ResolutionChoice> ChoiceBoxResolution;

    @FXML
    private CheckBox CheckBoxMultithreadedDownloads;

    @FXML
    private Button ButtonDownload;

    @FXML
    private Button ButtonShowPreview;
    
    @FXML
    private Button ButtonSaveImage;

    @FXML
    private TextField TextFieldTime;
    
    @FXML
    private AnchorPane imageViewParent;
    
    @FXML
    private ImageView imageView;

    @FXML
    private ProgressIndicator Spinner;
    
    BufferedImage result;
    Downloader dl = new Downloader();
    protected DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");
    
    void setup() {
    	ChoiceBoxResolution.getItems().add(new ResolutionChoice("Thumbnail (550px)", 1));
    	ChoiceBoxResolution.getItems().add(new ResolutionChoice("Very Low (1100px)", 2));
    	ChoiceBoxResolution.getItems().add(new ResolutionChoice("Low (2200px)", 4));
    	ChoiceBoxResolution.getItems().add(new ResolutionChoice("Medium (4400px)", 8));
    	ChoiceBoxResolution.getItems().add(new ResolutionChoice("High (8800px)", 16));
    	ChoiceBoxResolution.getItems().add(new ResolutionChoice("Very High (11000px)", 20));
    	
    	imageView.setImage(new Image(this.getClass().getResourceAsStream("HimawariThumb.png")));
    	imageView.setFitWidth(imageViewParent.getWidth());
    	Spinner.setVisible(false);
    	ButtonSaveImage.setDisable(true);
    }

    @FXML
    void download(ActionEvent event) {
    	setButtons(false);
    	ButtonSaveImage.setDisable(true);
    	progressBar.setProgress(0d);
    	ResolutionChoice resolution = ChoiceBoxResolution.getValue();

		new Thread(() -> {
				imageView.setFitWidth(imageViewParent.getWidth());

				if(!CheckBoxMultithreadedDownloads.isSelected()) {
					result = dl.single(resolution.getValue(), LocalDateTime.of(DatePicker.getValue(), LocalTime.parse(TextFieldTime.getText()).withSecond(0)));
				} else {
					result = dl.multi(resolution.getValue(), LocalDateTime.of(DatePicker.getValue(), LocalTime.parse(TextFieldTime.getText()).withSecond(0)));
				}
				imageView.setImage(SwingFXUtils.toFXImage(result, null));

				progressBar.setProgress(1d);
				ButtonSaveImage.setDisable(false);
				setButtons(true);
		}).start();
    	
    }

    @FXML
    void saveImage(ActionEvent event) {
    	ButtonSaveImage.setDisable(true);
    	setButtons(false);
    	File f = new File(System.getProperty("user.home") + "/" + df.format(LocalDateTime.of(DatePicker.getValue(), LocalTime.parse(TextFieldTime.getText()).withSecond(0))) + ".png");
    	Thread th =	new Thread(() -> {
    			if(result != null) {
    	    		try {
    					ImageIO.write(result, "png", f);
    				} catch (IOException e) {
    					e.printStackTrace();
    				}
    	    	}
    	    	setButtons(true);
    	    	ButtonSaveImage.setDisable(false);
    	});
    	th.start();
    	try {
			th.join();
			Alert al = new Alert(AlertType.INFORMATION);
	    	al.setTitle("Image saved!");
	    	al.setHeaderText("Image saved successfully!");
	    	al.setContentText("Path: " + f.getPath());
	    	al.showAndWait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }

    @FXML
    void showPreview(ActionEvent event) {
    	setButtons(false);
    	ButtonSaveImage.setDisable(true);
    	
    	new Thread(() -> {
    			imageView.setFitWidth(imageViewParent.getWidth());
    	    	BufferedImage preview = dl.preview(LocalDateTime.of(DatePicker.getValue(), LocalTime.parse(TextFieldTime.getText()).withSecond(0)));
    	    	imageView.setImage(SwingFXUtils.toFXImage(preview, null));
    	    	
    	    	setButtons(true);
    	}).start();
    	
    }
    
    void setButtons(boolean state) {
    	Spinner.setVisible(!state);
    	ButtonDownload.setDisable(!state);
    	ButtonShowPreview.setDisable(!state);
    }

    @FXML
    void updateAdvanced(ActionEvent event) {

    }

    @FXML
    void updateDate(ActionEvent event) {

    }

    @FXML
    void updateTime(ActionEvent event) {

    }
}
