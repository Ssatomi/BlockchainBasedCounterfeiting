package application;
	
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;


public class Main extends Application {
	static Stage primarystage = null;
	private Pane clientpane = null;
	private static Scene clientscene = null;
	ObservableList<String> ob_admin = FXCollections.observableArrayList();
	@Override
	public void start(Stage primaryStage) {
		try {
			primaryStage.setTitle("consumer client");
			primarystage = primaryStage;
			clientpane = FXMLLoader.load(getClass().getResource("view/client.fxml"));
			clientscene = new Scene(clientpane);
			primarystage.setScene(clientscene);
			primarystage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Stage getPrimaryStage() {
		return primarystage;
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
