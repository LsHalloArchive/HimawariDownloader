package tk.lshallo.himawari;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import javax.imageio.ImageIO;
import javax.swing.*;

import com.googlecode.pngtastic.core.PngImage;
import com.googlecode.pngtastic.core.PngOptimizer;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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


class UI {

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
    private CheckBox checkBoxCompression;

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
    
    private BufferedImage result;
    private Downloader dl = new Downloader();
    private DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");
    private DateTimeFormatter dfText = DateTimeFormatter.ofPattern("HH:mm");
    private boolean compressionLastEnabled = false;
    
    void setup() {
        ResolutionChoice defaultRes = new ResolutionChoice("Medium (4400px)", 8);
    	ChoiceBoxResolution.getItems().add(new ResolutionChoice("Thumbnail (550px)", 1));
    	ChoiceBoxResolution.getItems().add(new ResolutionChoice("Very Low (1100px)", 2));
    	ChoiceBoxResolution.getItems().add(new ResolutionChoice("Low (2200px)", 4));
    	ChoiceBoxResolution.getItems().add(defaultRes);
    	ChoiceBoxResolution.getItems().add(new ResolutionChoice("High (8800px)", 16));
    	ChoiceBoxResolution.getItems().add(new ResolutionChoice("Very High (11000px)", 20));
    	ChoiceBoxResolution.setValue(defaultRes);
    	ChoiceBoxResolution.setOnAction(event -> {
            int resolution = ChoiceBoxResolution.getValue().getValue();
            if(resolution > 8) {
                compressionLastEnabled = checkBoxCompression.isSelected();
                checkBoxCompression.setSelected(false);
                checkBoxCompression.setDisable(true);
            } else {
                checkBoxCompression.setDisable(false);
                checkBoxCompression.setSelected(compressionLastEnabled);
            }
        });

    	LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC")).minusMinutes(30).withSecond(0);
    	DatePicker.setValue(now.toLocalDate());
    	TextFieldTime.setText(dfText.format(now));
    	
    	imageView.setImage(new Image(this.getClass().getResourceAsStream("HimawariThumb.png")));
    	imageView.setFitWidth(imageViewParent.getWidth());
    	Spinner.setVisible(false);
    	ButtonSaveImage.setDisable(true);
    	CheckBoxMultithreadedDownloads.setSelected(true);
    }

    @FXML
    void download(ActionEvent event) {
    	setButtons(false);
    	ButtonSaveImage.setDisable(true);
    	progressBar.setProgress(0d);
    	progress = 0;
    	ResolutionChoice resolution = ChoiceBoxResolution.getValue();

		new Thread(() -> {
				imageView.setFitWidth(imageViewParent.getWidth());

				try {
                    if (!CheckBoxMultithreadedDownloads.isSelected()) {
                        result = dl.single(resolution.getValue(), LocalDateTime.of(DatePicker.getValue(), LocalTime.parse(TextFieldTime.getText()).withSecond(0)));
                    } else {
                        result = dl.multi(resolution.getValue(), LocalDateTime.of(DatePicker.getValue(), LocalTime.parse(TextFieldTime.getText()).withSecond(0)));
                    }
                    imageView.setImage(SwingFXUtils.toFXImage(result, null));
                    progressBar.setProgress(1d);
                    ButtonSaveImage.setDisable(false);
                    setButtons(true);
                } catch (Exception e) {
                    progressBar.setProgress(0d);
                    JOptionPane.showMessageDialog(null, "Download of image failed!\nPlease ensure you have entered a valid date and time. You also must choose a resolution.", "Error downloading image!", JOptionPane.ERROR_MESSAGE);
                    setButtons(true);
                }
		}).start();
    	
    }

    @FXML
    void saveImage(ActionEvent event) {
    	ButtonSaveImage.setDisable(true);
    	setButtons(false);
    	LocalDateTime dt = dl.primeTime(LocalDateTime.of(DatePicker.getValue(), LocalTime.parse(TextFieldTime.getText()).withSecond(0)));
    	File f = new File(System.getProperty("user.home") + "/" + df.format(dt) + ".png");
    	Thread th =	new Thread(() -> {
    			if(result != null) {
    	    		try {
    	    		    boolean formatSupported = false;
    	    		    for(String format : ImageIO.getWriterFileSuffixes()) {
                            if (format.equals("png")) {
                                formatSupported = true;
                            }
                        }

                        if (formatSupported) {
                            if(!checkBoxCompression.isSelected()) {
                                ImageIO.write(result, "png", f);
                            } else {
                                try {
                                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                                    ImageIO.write(result, "png", os);
                                    InputStream is = new ByteArrayInputStream(os.toByteArray());
                                    PngImage img = new PngImage(is);
                                    PngOptimizer optimizer = new PngOptimizer();
                                    PngImage oImage = optimizer.optimize(img);

                                    final ByteArrayOutputStream optimizedBytes = new ByteArrayOutputStream();
                                    oImage.writeDataOutputStream(optimizedBytes);
                                    oImage.export(f.getPath(), optimizedBytes.toByteArray());
                                } catch (OutOfMemoryError e) {
                                    JOptionPane.showMessageDialog(null, "You ran out of memory while compressing the image!\nProgram will now try to save image uncompressed.", "Out of memory!", JOptionPane.ERROR_MESSAGE);
                                    ImageIO.write(result, "png", f);
                                }
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "File format not supported!", "Format not supported!", JOptionPane.ERROR_MESSAGE);
                        }


    					Object[] answers = {"Yes", "No"};
                        if(JOptionPane.showOptionDialog(null, "Path: " + f.getPath() + "\nOpen image?", "Image saved successfully!", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, answers, answers[0]) == 0) {
                            Desktop.getDesktop().open(f);
                        }
    				} catch (IOException e) {
    					e.printStackTrace();
    				}
    	    	}
    	    	setButtons(true);
    	    	ButtonSaveImage.setDisable(false);
    	});
    	th.start();
    }

    @FXML
    void showPreview(ActionEvent event) {
    	setButtons(false);
    	ButtonSaveImage.setDisable(true);
    	
    	new Thread(() -> {
    			imageView.setFitWidth(imageViewParent.getWidth());
    			try {
                    BufferedImage preview = dl.preview(LocalDateTime.of(DatePicker.getValue(), LocalTime.parse(TextFieldTime.getText()).withSecond(0)));
                    imageView.setImage(SwingFXUtils.toFXImage(preview, null));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Download of image failed!\nPlease ensure you have entered a valid date and time.", "Error downloading image!", JOptionPane.ERROR_MESSAGE);
                    setButtons(true);
                }
    	    	
    	    	setButtons(true);
    	}).start();
    	
    }
    
    private void setButtons(boolean state) {
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

    @FXML
    void changeCompression(ActionEvent event) {
        if(checkBoxCompression.isSelected()) {
            JOptionPane.showMessageDialog(null, "Compression is very CPU intensive and may take several minutes, depending on your hardware. \nIt is not available for images >4400px.", "Attention!", JOptionPane.WARNING_MESSAGE);
        }
    }

    private int progress = 0;

    synchronized void increaseProgress() {
        int resolution = ChoiceBoxResolution.getValue().getValue();
        int totalImages = resolution * resolution;
        progress++;

        progressBar.setProgress((double)progress / (double)totalImages);
	}
}
