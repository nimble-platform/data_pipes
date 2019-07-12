package eu.nimble.service.datapipes.rest.datachannel;

import eu.nimble.service.datapipes.rest.check.*;
import java.util.Vector;

public class ResponseConsumeNextMessages {

    Vector messages = new Vector();

    public Vector getMessages() {
        return messages;
    }

    public void setMessages(Vector messages) {
        this.messages = messages;
    }

    public ResponseConsumeNextMessages () {
    }

    public void addMessage(String message) {
        messages.add(message);
    }


}
