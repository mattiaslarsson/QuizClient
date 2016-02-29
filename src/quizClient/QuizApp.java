package quizClient;

import javafx.application.Application;
import javafx.stage.Stage;

/** Startar klientprogrammet
 * 
 * @author Mattias Larsson
 *
 */
public class QuizApp extends Application {
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		Model model = new Model();
		model.getLayout(primaryStage).show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
