package com.ecomart.chatbot.domain.service;

import org.springframework.stereotype.Service;

import com.ecomart.chatbot.infra.openai.DataRequestChatCompletion;
import com.ecomart.chatbot.infra.openai.OpenAIClient;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;

import io.reactivex.Flowable;

@Service
public class ChatbotService {

	private OpenAIClient client;
	
	public ChatbotService(OpenAIClient client) {
		this.client = client;
	}
	
	public Flowable<ChatCompletionChunk> answerQuestion(String question) {
        var sistemPrompt = "You are a customer service chatbot for an e-commerce platform and should only respond to questions related to the e-commerce";
        var data = new DataRequestChatCompletion(sistemPrompt, question);
        return client.sendRequestChatCompletion(data);
	}
}
