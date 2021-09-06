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
	private Pane clientpane = null,loginpane=null;
	private static Scene clientscene = null,loginscene=null;
	ObservableList<String> ob_admin = FXCollections.observableArrayList();
	@Override
	public void start(Stage primaryStage) {
		try {
			primaryStage.setTitle("producer client");
			primarystage = primaryStage;
			clientpane = FXMLLoader.load(getClass().getResource("view/client.fxml"));
			loginpane = FXMLLoader.load(getClass().getResource("view/login.fxml"));
			clientscene = new Scene(clientpane);
			loginscene = new Scene(loginpane);
			primarystage.setScene(loginscene);
			primarystage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void  setLoginScene() {
		primarystage.setScene(loginscene);
	}
	
	public static void setClientScene() {
		primarystage.setScene(clientscene);
	}
	
	public static Stage getPrimaryStage() {
		return primarystage;
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
