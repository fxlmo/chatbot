package com.example.chatbot;

import com.mongodb.DBCollection;

import java.util.ArrayList;

public class globals {
    public double averageTF;
    public ArrayList<ArrayList<String>> keyList;
    public ArrayList<String> threadList;
    public ArrayList<entry> entries;
    public context context;
    public ArrayList<String> stoplist;
    public DBCollection collection;
    public ArrayList<ArrayList<String>> documents = new ArrayList<ArrayList<String>>();
}
