package clientAPI;

import com.google.gson.Gson;
import ui.TerminalGamePlay;
import webSocketMessages.serverMessages.ErrorMessage;
import webSocketMessages.serverMessages.LoadGame;
import webSocketMessages.serverMessages.Notification;
import webSocketMessages.serverMessages.ServerMessage;

public class NotificationHandler {
    private TerminalGamePlay terminal;
    public NotificationHandler (){
    }

    public void addTerminal(TerminalGamePlay terminal){
        this.terminal = terminal;
    }
    void notify(String message){
        ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);
        switch(serverMessage.getServerMessageType()){
            case NOTIFICATION -> terminal.displayNotification(new Gson().fromJson(message, Notification.class).getMessage());
            case ERROR -> terminal.displayError(new Gson().fromJson(message, ErrorMessage.class).getErrorMessage());
            case LOAD_GAME -> terminal.loadGame(new Gson().fromJson(message, LoadGame.class).getGame());
        }
    }
}
