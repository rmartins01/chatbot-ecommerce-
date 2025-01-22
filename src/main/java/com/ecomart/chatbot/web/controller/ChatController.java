package com.ecomart.chatbot.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import com.ecomart.chatbot.domain.service.ChatbotService;
import com.ecomart.chatbot.web.dto.QuestionDto;

@Controller
@RequestMapping({"/", "chat"})
public class ChatController {

    private static final String CHAT_PAGE = "chat";

    private ChatbotService service;

    public ChatController(ChatbotService service) {
            this.service = service;
    }
    
    @GetMapping
    public String loadChatbotPage() {
        return CHAT_PAGE;
    }

    @PostMapping
    @ResponseBody
    public ResponseBodyEmitter responderPergunta(@RequestBody QuestionDto dto) {
        var flowAnswer = service.answerQuestion(dto.question());
        var emitter = new ResponseBodyEmitter();

        flowAnswer.subscribe(chunk -> {
                var token = chunk.getChoices().get(0).getMessage().getContent();
                if (token != null) {
                        emitter.send(token);
                }
        }, emitter::completeWithError,
                        emitter::complete);

        return emitter;
}
    @GetMapping("clean")
    public String cleanConversation() {
        return CHAT_PAGE;
    }

}
