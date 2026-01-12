package models;

public class ChatMessage {
    public String text;
    public boolean fromBot;

    public ChatMessage(String text, boolean fromBot) {
        this.text = text;
        this.fromBot = fromBot;
    }
}

