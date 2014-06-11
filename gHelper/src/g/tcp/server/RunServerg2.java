package g.tcp.server;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class RunServerg2 extends Application implements ServerListener {
	
	private Pane		root;
	private Serverg2	srv	= null;
	
	private static TextField	textField, textField2;
	private Thread				th;
	
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				try {
					stop();
					System.exit(0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		root = new Pane();
		
		Button button1 = new Button("create server");
		button1.setLayoutX(10);
		button1.setLayoutY(5);
		button1.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				System.out.println("ffff");
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						RunServerg2 rr = new RunServerg2();
						rr.startServer();
					}
				});
			}
		});
		root.getChildren().add(button1);
		
		textField = new TextField("5599");
		textField.setLayoutX(120);
		textField.setLayoutY(5);
		root.getChildren().add(textField);
		
		textField2 = new TextField("127.0.0.1");
		textField2.setLayoutX(120);
		textField2.setLayoutY(30);
		root.getChildren().add(textField2);
		
		primaryStage.setScene(new Scene(root, 300, 250));
		primaryStage.show();
	}
	
	private void startServer() {
		System.out.println("ffff");
		srv = new Serverg2();
		srv.setServerName("masterServer");
		srv.setServerId((byte) 1);
		srv.setServerListener(this);
		srv.setServerPort(Integer.valueOf(textField.getText()));
		
		System.out.println("ffff");
		//th = new Thread(srv);
		//th.start();
		System.out.println("4");
	}
	
	@Override
	public void incomingMessage1(String line) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void incomingMessage2(String line) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void incomingMessage3(String line) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void incomingMessage4(String line) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void incomingMessage5(String line) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void incomingMessage6(String line) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void incomingMessage7(String line) {
		// TODO Auto-generated method stub
		
	}
	
}
