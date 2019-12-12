package com.ChatbotController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatbotController {

    @RequestMapping("/")
    public String index() {
        return "<a href='https://www.google.com'>help</a>";
    }

}
