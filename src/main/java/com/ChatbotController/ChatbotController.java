package com.ChatbotController;

import com.example.chatbot.ChatbotApplication;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Controller
public class ChatbotController {

    @RequestMapping(value ="/index", method = RequestMethod.GET)
    public String questionForm(){
        return "questionform";
    }

    @RequestMapping(value = "/index", method = RequestMethod.POST)
    public String index(HttpServletRequest request, Model model) {
        String questionAsked = request.getParameter("question");
        model.addAttribute("Question", "fill in later");


        return "hello.html";
    }

}
