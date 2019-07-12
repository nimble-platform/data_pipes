package eu.nimble.service.datapipes.rest.check;

import java.util.Vector;

public class ResponseCheck {

    Vector messages = new Vector();

    public Vector getMessages() {
        return messages;
    }

    public void setMessages(Vector messages) {
        this.messages = messages;
    }

    public ResponseCheck (String message) {
        messages.add(message);
    }

    public void addMessage(String message) {
        messages.add(message);
    }


}
