package br.com.alura.ecomart.chatbot.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import br.com.alura.ecomart.chatbot.domain.service.ChatbotService;
import br.com.alura.ecomart.chatbot.web.dto.PerguntaDto;

@Controller
@RequestMapping({"/", "chat"})
public class ChatController {

    private static final String PAGINA_CHAT = "chat";

    private ChatbotService service;

    public ChatController(ChatbotService service) {
            this.service = service;
    }
    
    @GetMapping
    public String carregarPaginaChatbot() {
        return PAGINA_CHAT;
    }

    @PostMapping
    @ResponseBody
    public ResponseBodyEmitter responderPergunta(@RequestBody PerguntaDto dto) {
        var fluxoResposta = service.responderPergunta(dto.pergunta());
        var emitter = new ResponseBodyEmitter();

        fluxoResposta.subscribe(chunk -> {
                var token = chunk.getChoices().get(0).getMessage().getContent();
                if (token != null) {
                        emitter.send(token);
                }
        }, emitter::completeWithError,
                        emitter::complete);

        return emitter;
}
    @GetMapping("limpar")
    public String limparConversa() {
        return PAGINA_CHAT;
    }

}
