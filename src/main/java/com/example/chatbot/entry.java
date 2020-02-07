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
        return  "Thread id - " + this.threadid + '\n' +
                "Subthread id - " + this.subthreadid + '\n' +
                "subid - " + this.subid + "\n" +
                "body - " + this.body + "\n" +
                "date - " + this.date + "\n" +
                "qa - " + this.qa + "\n" +
                "Keywords - " + this.keywords;
    }

    public entry add(String threadid, String subthreadid, int subid, String date, String qa, ArrayList<String> keywords, String body) {
        entry ent = new entry();
        ent.body = body;
        ent.threadid = threadid;
        ent.subid = subid;
        ent.date = date;
        ent.subthreadid = subthreadid;
        ent.qa = qa;
        ent.keywords = keywords;
        return ent;
    }
}
