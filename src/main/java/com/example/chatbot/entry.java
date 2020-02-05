package com.example.chatbot;

import java.util.ArrayList;

public class entry {
    public String threadid;
    public int subid;
    public String subthreadid;
    public String date;
    public String body;
    public ArrayList<String> keywords;
    public String qa;

    @Override
    public String toString() {
        String output = "";
        output = "Thread id - " + this.threadid + '\n' +
                "Subthread id " + this.subthreadid + '\n' +
                "subid - " + this.subid + "\n" +
                "body - " + this.body + "\n" +
                "date - " + this.date + "\n" +
                "qa - " + this.qa + "\n" +
                "Keywords - " + this.keywords;
        return output;
    }
}
