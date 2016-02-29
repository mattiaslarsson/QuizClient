package quizClient;

import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;


/** Innehåller alla lyssnare och händelser
 * 
 * @author Mattias Larsson
 *
 */
public class EventHandlers {

	/** När man stänger fönstret
	 * 
	 * @param model
	 * @return EventHandler
	 */
	public EventHandler stageCloseEvent(Model model) {
		EventHandler stageClose = new EventHandler() {
			@Override
			public void handle(Event e) {
				model.disConnectFromServer();
				System.exit(0);
			}
		};
		
		return stageClose;
	}
	
	/** När man ansluter till servern
	 * 
	 * @param BooleanProperty
	 * @param Model
	 * @return EventHandler
	 */
	public EventHandler connectAction(Model model) {
		EventHandler connectAction = new EventHandler() {
			@Override
			public void handle(Event e) {
				model.connectToServer();
			}
		};
		
		return connectAction;
	}
	
	/** När man kopplar ner från servern
	 * 
	 * @param Model
	 * @return EventHandler
	 */
	public EventHandler disConnectAction(Model model) {
		EventHandler disConnectAction = new EventHandler() {
			@Override
			public void handle(Event e) {
				model.disConnectFromServer();
			}
		};
		
		return disConnectAction;
	}
	
	
	/** När man skickar meddelande
	 * 
	 * @param Model
	 * @param TextField
	 * @return EventHandler
	 */
	public EventHandler chatAction(Model model, TextField chatMsg) {
		EventHandler chatAction = new EventHandler() {
			@Override
			public void handle(Event e) {
				if (chatMsg.getText().length()>0) {
					model.sendMsg(chatMsg.getText());
					chatMsg.setText("");
				}
			}
		};
		
		return chatAction;
	}
}
