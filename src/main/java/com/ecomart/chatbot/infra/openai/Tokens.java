package com.ecomart.chatbot.infra.openai;

import org.springframework.stereotype.Component;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.ModelType;

@Component
public class Tokens {

    private final Encoding encoding;

    public Tokens() {
        var registry = Encodings.newDefaultEncodingRegistry();
        this.encoding = registry.getEncodingForModel(ModelType.GPT_4);
    }

    public int get(String mensagem) {
        return encoding.countTokens(mensagem);
    }

}
