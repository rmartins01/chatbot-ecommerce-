package com.ecomart.chatbot.infra.openai;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ecomart.chatbot.domain.FreightCalculatorData;
import com.ecomart.chatbot.domain.service.FreighCalculator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatFunction;
import com.theokanning.openai.completion.chat.ChatFunctionCall;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.messages.Message;
import com.theokanning.openai.messages.MessageRequest;
import com.theokanning.openai.runs.Run;
import com.theokanning.openai.runs.RunCreateRequest;
import com.theokanning.openai.runs.SubmitToolOutputRequestItem;
import com.theokanning.openai.runs.SubmitToolOutputsRequest;
import com.theokanning.openai.service.FunctionExecutor;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.threads.ThreadRequest;


@Component
public class OpenAIClient {

    private final String apiKey;
    private final String assistantId;
    private String threadId;
    private final OpenAiService service;
    private final FreighCalculator freighCalculator;

    public OpenAIClient(
            @Value("${app.openai.api.key}") String apiKey,
            @Value("${app.openai.assistant.id}") String assistantId,
            FreighCalculator freighCalculator) {
        this.apiKey = apiKey;
        this.assistantId = assistantId;
        this.freighCalculator = freighCalculator;
        this.service = new OpenAiService(apiKey, Duration.ofSeconds(60));
    }

    public String sendRequestChatCompletion(DataRequestChatCompletion data) {
        var messageRequest = MessageRequest.builder()
                .role(ChatMessageRole.USER.value())
                .content(data.userPrompt())
                .build();

        if (threadId == null) {
            threadId = createNewThread(messageRequest);
        } else {
            service.createMessage(threadId, messageRequest);
        }

        var run = startRun();
        handleRunCompletion(run);

        return retrieveLatestResponse();
    }

    private String createNewThread(MessageRequest messageRequest) {
        var threadRequest = ThreadRequest.builder()
                .messages(List.of(messageRequest))
                .build();
        return service.createThread(threadRequest).getId();
    }

    private Run startRun() {
        var runRequest = RunCreateRequest.builder()
                .assistantId(assistantId)
                .build();
        return service.createRun(threadId, runRequest);
    }

    private void handleRunCompletion(Run run) {
        boolean completed = false;
        boolean requiresFunctionCall;

        try {
            do {
                Thread.sleep(10_000);
                run = service.retrieveRun(threadId, run.getId());
                completed = "completed".equalsIgnoreCase(run.getStatus());
                requiresFunctionCall = run.getRequiredAction() != null;

                if (requiresFunctionCall) {
                    handleFunctionCall(run);
                }

            } while (!completed);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted while waiting for run completion", e);
        }
    }

    private void handleFunctionCall(Run run) {
        var freightPrice = calculateFreight(run);

        var toolOutput = SubmitToolOutputRequestItem.builder()
                .toolCallId(run.getRequiredAction().getSubmitToolOutputs().getToolCalls().get(0).getId())
                .output(freightPrice)
                .build();

        var submitRequest = SubmitToolOutputsRequest.builder()
                .toolOutputs(List.of(toolOutput))
                .build();

        service.submitToolOutputs(threadId, run.getId(), submitRequest);
    }

    private String calculateFreight(Run run) {
        try {
        	
            var functionCall = run.getRequiredAction().getSubmitToolOutputs().getToolCalls().get(0).getFunction();

            var freightFunction = ChatFunction.builder()
                    .name("freightCalculator")
                    .executor(FreightCalculatorData.class, freighCalculator::calc)
                    .build();

            var executor = new FunctionExecutor(List.of(freightFunction));
            var functionArgs = new ObjectMapper().readTree(functionCall.getArguments());

            return executor.execute(new ChatFunctionCall(functionCall.getName(), functionArgs)).toString();
        } catch (Exception e) {
            throw new RuntimeException("Error executing freight calculation", e);
        }
    }

    private String retrieveLatestResponse() {
        return service.listMessages(threadId).getData().stream()
                .sorted(Comparator.comparingInt(Message::getCreatedAt).reversed())
                .findFirst()
                .map(message -> message.getContent().get(0).getText().getValue().replaceAll("\\\u3010.*?\\\u3011", ""))
                .orElseThrow(() -> new RuntimeException("No response found in thread"));
    }

    public List<String> loadMessageHistory() {
        if (threadId == null) return List.of();

        return service.listMessages(threadId).getData().stream()
                .sorted(Comparator.comparingInt(Message::getCreatedAt))
                .map(message -> message.getContent().get(0).getText().getValue())
                .collect(Collectors.toList());
    }

    public void deleteThread() {
        if (threadId != null) {
            service.deleteThread(threadId);
            threadId = null;
        }
    }
}

