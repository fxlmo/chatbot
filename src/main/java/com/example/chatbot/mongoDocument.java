package com.example.chatbot;

import java.util.ArrayList;

public class mongoDocument {
    public String threadid;
    public String subid;
    public String subthreadid;
    public String body;
    public String date;
    public String qa;
    public ArrayList<String> keyWords;

    public String toString() {
        String output = "";
        output = "Thread id - " + this.threadid + '\n' +
                "Subthread id " + this.subthreadid + '\n' +
                "subid - " + this.subid + "\n" +
                "body - " + this.body + "\n" +
                "date - " + this.date + "\n" +
                "qa - " + this.qa + "\n" +
                "Keywords - " + this.keyWords;
        return output;
    }
}
