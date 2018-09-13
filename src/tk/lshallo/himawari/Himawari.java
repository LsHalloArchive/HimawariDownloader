package tk.lshallo.himawari;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
 
public class Himawari extends Application {

	static UI controller;

    @Override
    public void start(Stage stage) {
		controller = new UI();
		
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("UI.fxml"));
			loader.setController(controller);
			VBox root = loader.load();
			Scene scene = new Scene(root);
			
			stage.setTitle("Himawari Downloader");
			stage.setScene(scene);
			stage.getIcons().add(new Image(this.getClass().getResourceAsStream("icon.png")));
			stage.show();
			
			controller.setup();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public static void main(String[] args) {
        launch(args);
    }
}
