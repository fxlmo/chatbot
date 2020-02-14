package com.ChatbotController;

import com.example.chatbot.ChatbotApplication;
import com.mongodb.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;

@Controller
public class ChatbotController {

    @Autowired
    ChatbotApplication ca;

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String questionForm() {
        return "index";
    }

    @RequestMapping(value = "/index", method = RequestMethod.POST)
    public String index(HttpServletRequest request, Model model) throws JSONException {
        String questionAsked = request.getParameter("question");


        JSONObject newOB = new JSONObject();
        ArrayList<ArrayList<String>> documents = new ArrayList<>();
        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://admin:chatbot123@ec2-54-198-1-3.compute-1.amazonaws.com:27017/?authSource=admin&authMechanism=SCRAM-SHA-1"));
        ;
        DB database = database = mongoClient.getDB("chatbot");
        ;
        DBCollection coll = database.getCollection("threads");
        //This will flag as deprecated but it's fine I promise


        //GET ALL FILES FROM MONGO
        DBCursor cursor = coll.find(new BasicDBObject());
        //Iterate through documents
        for (int i = 0; i < cursor.size(); i++) {
            DBObject currentDoc = cursor.next();
            String body = (String) currentDoc.get("body");
            if (body != null) {
                ArrayList<String> newDoc = new ArrayList<>();
                for (String word : body.split(" ")) {
                    newDoc.add(word);
                }
                documents.add(newDoc);
            }
        }

        newOB = ca.normalIO(documents, coll, questionAsked);
        String content = newOB.get("content").toString();
        System.out.println(content);
        model.addAttribute("Question",content);

        return "index";
    }
}