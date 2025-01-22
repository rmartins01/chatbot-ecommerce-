package com.ecomart.chatbot.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ecomart.chatbot.domain.service.ChatbotService;
import com.ecomart.chatbot.web.dto.QuestionDto;

@Controller
@RequestMapping({ "/", "chat" })
public class ChatController {

	private static final String CHAT_PAGE = "chat";

	private ChatbotService service;

	public ChatController(ChatbotService service) {

	}

	@GetMapping
	public String loadChatbotPage(Model model) {
		var messages = service.loadMessageHistory();
		model.addAttribute("history", messages);
		return CHAT_PAGE;
	}

	@PostMapping
	@ResponseBody
	public String responderPergunta(@RequestBody QuestionDto dto) {
		return service.answerQuestion(dto.question());
	}

	@GetMapping("clean")
	public String cleanConversation() {
		service.cleanConversation();
		return "redirect:/chat";
	}

}
