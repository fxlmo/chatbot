package com.ChatbotController;

import java.util.ArrayList;

import com.example.chatbot.ChatbotApplication;
import com.mongodb.DBCollection;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class ChatbotController {

    @Autowired
    ChatbotApplication ca;

    ChatbotApplication app = new ChatbotApplication();
    DBCollection collection = app.getCollection();
    ArrayList<ArrayList<String>> documents = app.getDocs(collection);

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String questionForm() {
        return "index";
    }



    @RequestMapping(value = "/index", method = RequestMethod.POST)
    public ResponseEntity<JSONresponse> getSearchResultViaAjax(@RequestBody String msg1) throws JSONException {


        /*
        TODO:  - send data to chatbot application
               - Make sure message is processed correctly
               - send back json
               - Process and format string in js
         */

        System.out.println("message received: " + msg1);

        JSONObject response = app.normalIO(documents, collection, msg1);
        return ResponseEntity.ok(new JSONresponse(response));


    }

    private class JSONresponse {
        public String type;
        public ArrayList<String> content;

        public JSONresponse(JSONObject s) throws JSONException {
            this.type = (String) s.get("type");
            this.content = new ArrayList<>();
            if (type.equals("answer")) {
                int i = 0;
                JSONObject JSONcontent = (JSONObject) s.get("content");
                while(i < JSONcontent.length()) {
                    this.content.add((String) JSONcontent.get(String.valueOf(i)));
                    i++;
                }
            } else {
                this.content.add((String) s.get("content"));
            }
        }
    }

    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    public String adminPage() {
        return "admin";
    }

    @RequestMapping(value = "/admin", method = RequestMethod.POST)
    public ResponseEntity<JSONresponse> addThreadViaAjax(@RequestBody String thread) throws JSONException {

        JSONObject jsonThread = new JSONObject(thread);
        System.out.println(jsonThread);

        //app.adminAdd(collection, documents, jsonThread.getString("ID"), jsonThread.getString("SubID"), jsonThread.getString("body"), jsonThread.getString("date"), jsonThread.getString("qa") .toLowerCase());

        JSONObject response = new JSONObject();
        response.put("type","success");
        response.put("content","null");
        return ResponseEntity.ok(new JSONresponse(response));


    }


}