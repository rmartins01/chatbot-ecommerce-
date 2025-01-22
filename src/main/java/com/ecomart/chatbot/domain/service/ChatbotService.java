package com.ecomart.chatbot.domain.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ecomart.chatbot.infra.openai.DataRequestChatCompletion;
import com.ecomart.chatbot.infra.openai.OpenAIClient;

@Service
public class ChatbotService {

	private OpenAIClient client;
	
	public ChatbotService(OpenAIClient client) {
		this.client = client;
	}
	
	public String answerQuestion(String question) {
        var sistemPrompt = "You are a customer service chatbot for an e-commerce platform and should only respond to questions related to the e-commerce";
        var data = new DataRequestChatCompletion(sistemPrompt, question);
        return client.sendRequestChatCompletion(data);
	}
	
    public List<String> loadMessageHistory() {
        return client.loadMessageHistory();
    }

    public void cleanConversation() {
        client.deleteThread();
    }
}
