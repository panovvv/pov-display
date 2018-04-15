package com.shortn0tes.povdisplay;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("scene.fxml"));
		Parent root = loader.load();
		primaryStage.setTitle("POV Display");
		primaryStage.setScene(new Scene(root));
		primaryStage.show();
		Controller controller = loader.getController();
		primaryStage.setOnCloseRequest((we -> controller.close()));
		primaryStage.show();
		controller.refreshSerialPortList();
	}


	public static void main(String[] args) {
		launch(args);
	}
}
