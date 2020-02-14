package com.ChatbotController;

import com.example.chatbot.ChatbotApplication;
import com.mongodb.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<StringObj> getSearchResultViaAjax(@RequestBody String msg1) {


        /*
        TODO:  - send data to chatbot application
               - Make sure message is processed correctly
               - send back json
               - Process and format string in js
         */
        System.out.println("message received");

        return ResponseEntity.ok(new StringObj("received"));


    }

    private class StringObj {
        public String field;

        public StringObj(String s) {
            field = s;
        }
    }
}