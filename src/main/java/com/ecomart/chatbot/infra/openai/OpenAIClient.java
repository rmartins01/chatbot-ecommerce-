package com.ecomart.chatbot.infra.openai;

import java.time.Duration;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.theokanning.openai.OpenAiHttpException;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

import io.reactivex.Flowable;

@Component
public class OpenAIClient {

    private final String apiKey;
    private final OpenAiService service;

    public OpenAIClient(@Value("${app.openai.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.service = new OpenAiService(apiKey, Duration.ofSeconds(60));
    }

    public Flowable<ChatCompletionChunk> sendRequestChatCompletion(DataRequestChatCompletion data) {
        final int maxAttempts = 5;
        int secNextAttempt = 5;
        int attempts = 0;

        while (attempts++ < maxAttempts) {
            try {
                return service.streamChatCompletion(buildChatCompletionRequest(data));
            } catch (OpenAiHttpException ex) {
                handleOpenAiHttpException(ex, secNextAttempt);
                secNextAttempt *= 2;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        throw new RuntimeException("API down! Attempts completed without success");
    }

    private ChatCompletionRequest buildChatCompletionRequest(DataRequestChatCompletion data) {
        return ChatCompletionRequest.builder()
                .model("gpt-4o-mini")
                .messages(Arrays.asList(
                        new ChatMessage(ChatMessageRole.SYSTEM.value(), data.promptSistema()),
                        new ChatMessage(ChatMessageRole.USER.value(), data.userPrompt())
                ))
                .stream(true)
                .build();
    }

    private void handleOpenAiHttpException(OpenAiHttpException ex, int retryDelay) {
        int errorCode = ex.statusCode;

        switch (errorCode) {
            case 401 -> throw new RuntimeException("Error with API key!", ex);
            case 429, 500, 503 -> {
                try {
                    Thread.sleep(1000L * retryDelay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread interrupted during retry delay", e);
                }
            }
            default -> throw new RuntimeException("Unhandled OpenAiHttpException", ex);
        }
    }


}
