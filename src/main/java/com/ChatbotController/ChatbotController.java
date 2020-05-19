package com.ChatbotController;

import com.example.chatbot.ChatbotApplication;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.Map;

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

    @RequestMapping(value = "/index/reset", method = RequestMethod.POST)
    public ResponseEntity<JSONresponse> resetViaAjax() {
        app.resetAll();
        return ResponseEntity.ok(new JSONresponse());
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

        public JSONresponse() {
            this.type = null;
            this.content = null;
        }

        public JSONresponse(JSONObject s) throws JSONException {
            this.type = (String) s.get("type");
            this.content = new ArrayList<>();
            if (s.get("type").equals("answer") || s.get("type").equals("success") || s.get("type").equals("answers")) {
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

        public JSONresponse(String type, ArrayList<String> content) {
            this.type = type;
            this.content = content;
        }
    }

    private class DBresponse {
        public String type;
        public ArrayList<DBObject> content;
    }


    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    public String adminPage() {
        return "admin";
    }

    @RequestMapping(value = "/admin", method = RequestMethod.POST)
    public ResponseEntity<JSONresponse> addThreadViaAjax(@RequestBody String thread) throws JSONException {

        JSONObject jsonThread = new JSONObject(thread);
        System.out.println("Adding thread " + thread);

        app.adminAdd(collection, documents, jsonThread.getString("ID"), jsonThread.getString("SubID"), jsonThread.getString("body"), jsonThread.getString("date"), jsonThread.getString("qa") .toLowerCase());

        JSONresponse response = new JSONresponse();
        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/admin/delete", method = RequestMethod.POST)
    public ResponseEntity<JSONresponse> getThreadAjax() throws JSONException {

        // JSONObject jsonThread = new JSONObject(thread);
        // System.out.println(jsonThread);

        //app.adminAdd(collection, documents, jsonThread.getString("ID"), jsonThread.getString("SubID"), jsonThread.getString("body"), jsonThread.getString("date"), jsonThread.getString("qa") .toLowerCase());
        ArrayList<String> threads = app.getAllThreads(collection);

        JSONresponse jNresponse = new JSONresponse();
        jNresponse.type = "success";
        jNresponse.content = threads;
        return ResponseEntity.ok(jNresponse);
    }

    @RequestMapping(value = "/admin/threads", method = RequestMethod.POST)
    public ResponseEntity<DBresponse> getSubThreadsAjax(@RequestBody String threadid) throws JSONException {

        // JSONObject jsonThread = new JSONObject(thread);
        // System.out.println(jsonThread);

        //app.adminAdd(collection, documents, jsonThread.getString("ID"), jsonThread.getString("SubID"), jsonThread.getString("body"), jsonThread.getString("date"), jsonThread.getString("qa") .toLowerCase());
		threadid = threadid.substring(1, threadid.length() - 1);
        ArrayList<DBObject> threads = app.getSubThreads(collection, threadid);

        DBresponse jNresponse = new DBresponse();
        jNresponse.type = "success";
        jNresponse.content = threads;
        return ResponseEntity.ok(jNresponse);
    }

    @RequestMapping(value = "/admin/delete", method = RequestMethod.DELETE)
    public ResponseEntity<JSONresponse> deleteThreadViaAjax(@RequestBody String threads) throws JSONException {

        JSONArray jsonThreads = new JSONArray(threads);
        JSONObject testobj = jsonThreads.getJSONObject(0);
        for (int j = 0; j < jsonThreads.length(); j++) {
            JSONObject object = jsonThreads.getJSONObject(j);
            app.adminDelete(collection, object);
        }
        app.updateSubIds(collection, testobj);


        JSONresponse response = new JSONresponse("success", null);
        return ResponseEntity.ok(response);
    }
}