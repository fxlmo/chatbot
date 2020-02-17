package com.ChatbotController;

import com.example.chatbot.ChatbotApplication;
import com.mongodb.DBCollection;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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
    public ResponseEntity<StringObj> getSearchResultViaAjax(@RequestBody String msg1) throws JSONException {


        /*
        TODO:  - send data to chatbot application
               - Make sure message is processed correctly
               - send back json
               - Process and format string in js
         */
        ChatbotApplication app = new ChatbotApplication();
        DBCollection collection = app.getCollection();
        ArrayList<ArrayList<String>> documents = app.getDocs(collection);
        System.out.println("message received");
        System.out.println("Response:");
        System.out.println(app.normalIO(documents, collection, msg1));

        return ResponseEntity.ok(new StringObj("received"));


    }

    private class StringObj {
        public String field;

        public StringObj(String s) {
            field = s;
        }
    }
}