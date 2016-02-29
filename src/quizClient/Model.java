package quizClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;


/** Klassen innehåller logiken för hela klientprogrammet
 * 
 * @author Mattias Larsson
 *
 */
public class Model {
	private EventHandlers events = new EventHandlers();
	private Socket sock;
	private PrintStream printer;
	private InputStream reader;
	private TextArea chatWindow;
	private Button connect, disConnect;
	private TextField userName, chatMsg;
	private Text questionText;
	private ListView<String> userScoreListView;
	private ObservableList<String> scoreList = FXCollections.observableArrayList();

	
	/** Skapar fönster
	 * 
	 * @param Stage
	 * @return Stage
	 */
	@SuppressWarnings("unchecked")
	public Stage getLayout(Stage stage) {
		BorderPane root = new BorderPane();
		
		Scene scene = new Scene(root, 640, 400);
		stage.setScene(scene);
		stage.setOnCloseRequest(events.stageCloseEvent(this));
		
		connect = new Button("Connect");
		disConnect = new Button("Disconnect");
		
		userName = new TextField();
		userName.setPromptText("Skriv in namn");
		userName.setOnAction(events.connectAction(this));
		
		connect.setOnAction(events.connectAction(this));
		disConnect.setOnAction(events.disConnectAction(this));
		
		HBox toppRad = new HBox();
		toppRad.getChildren().addAll(userName, connect, disConnect);
		
		VBox centerBox = new VBox();
		
		questionText = new Text();
		questionText.setFont(Font.font(25));
		questionText.setFill(Color.CADETBLUE);
		questionText.setWrappingWidth(400);
		
		chatWindow = new TextArea();
		chatWindow.setWrapText(true);
		chatWindow.setEditable(false);
		
		centerBox.getChildren().addAll(toppRad, questionText, chatWindow);
		root.setCenter(centerBox);
		
		chatMsg = new TextField();
		chatMsg.requestFocus();
		chatMsg.setOnAction(events.chatAction(this, chatMsg));
		root.setBottom(chatMsg);
		
		userScoreListView = new ListView<String>();
		userScoreListView.setItems(scoreList);
		root.setLeft(userScoreListView);
		
		return stage;
	}
	
	/** Ansluter till servern om man har 
	 * angett ett användarnamn
	 * 
	 */
	public void connectToServer() {
		if (userName.getText().length() > 0) {
			Thread connect = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						// Lägger till en själv i poänglistan
						scoreList.add(userName.getText() + " " + "0");
						sock = new Socket("localhost", 3000);
						printer = new PrintStream(sock.getOutputStream());
						reader = sock.getInputStream();
						
						// Skickar inloggningsmeddelande till servern
						sendMsg("@userName@"+userName.getText());
						Scanner sc = new Scanner(reader);
						chatWindow.setText("");
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								chatMsg.requestFocus();
							}
						});
						while(sc.hasNextLine()) {
							String input = sc.nextLine();
							chatWindow.appendText(parseMsg(input)+"\n");
						}
					} catch (IOException ioex) {
						chatWindow.appendText("Fel vid anslutning\n");
					}
				}
			});
			
			connect.start();
		} else {
			chatWindow.appendText("Du måste ange ett användarnamn\n");
		}
	}
	
	/** Kopplar ner från servern
	 * 
	 */
	public void disConnectFromServer() {
		sendMsg("/drop");
		sock = null;
	}
	
	/** Parsar meddelande från servern
	 * 
	 * @param String
	 * @return String
	 */
	public String parseMsg(String input) {
		// Inloggningsmeddelande
		if (input.startsWith("@userName@")) {
			System.out.println(input);
			String name = input.substring(input.lastIndexOf("@")+1, input.indexOf("-"));
			String score = input.substring(input.lastIndexOf("-")+1, input.length());
			Platform.runLater(() -> {
				// Lägger till en ny användare i poänglistan
				scoreList.add(name + " " + score);
			});

			return "";
		
		// En fråga tas emot
		} else if (input.startsWith("@question@")) {
			questionText.setText(input.substring(input.lastIndexOf("@")+1, input.length()));
			return "";
			
		// Någon får poäng
		} else if (input.startsWith("@point@")) {
			System.out.println(input);
			String name = input.substring(input.lastIndexOf("@")+1, input.indexOf("-"));
			String score = input.substring(input.indexOf("-")+1, input.length());
			scoreList.forEach(user -> {
				if (user.startsWith(name)) {
					Platform.runLater(() -> {
						// Uppdaterar poänglistan
						int index = scoreList.indexOf(user);
						scoreList.set(index, name + " " + score);
					});
				}
			});
		
			return "";
			
		// Någon loggar ut
		} else if (input.startsWith("@loggedout@")) {
			String name = input.substring(input.lastIndexOf("@")+1, input.length());
			// Tar bort användaren från listan
			scoreList.forEach(user -> {
				if (user.startsWith(name)) {
					Platform.runLater(() -> {
						scoreList.remove(user);
					});
				}
			});
			return "";
		}
		return input;
	}
	
	/** Skickar meddelande till servern
	 * 
	 * @param msg
	 */
	public void sendMsg(String msg) {
		try {
			printer.println(msg);
			printer.flush();
			chatMsg.setText("");
		} catch (NullPointerException npe) {
			chatWindow.appendText("Du är inte ansluten\n");
		}
	}
}
	