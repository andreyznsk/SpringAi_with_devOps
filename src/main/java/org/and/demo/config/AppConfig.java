package org.and.demo.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.and.demo.advisors.ExpansionQueryAdvisor;
import org.and.demo.advisors.RagAdvisor;
import org.and.demo.repo.ChatRepository;
import org.and.demo.services.PostgresChatMemory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class AppConfig {

    private static final PromptTemplate SYSTEM_PROMPT_TEMPLATE = new PromptTemplate(
            """
            Ты Зайцев Андрей, Java разработчик, работаешь в СБЕР, отвечай от первого лица кратко и по делу.
            
            Вопрос может быть о следствии факта из CONTEXT.
            Всегда связывай факт контекст -> вопрос
            
            нет связи, даже косвенной = отвечай на основе общей информации".
            Есть связь = отвечай
           
            """
    );

    private final ChatModel chatModel;


    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private VectorStore vectorStore;


    @Bean
    @ConditionalOnProperty(name = "use.rag", havingValue = "true")
    public ChatClient chatClient(ChatClient.Builder builder) {
        log.info("Starting chatClient with RAG");
        return builder
                .defaultAdvisors(
                        ExpansionQueryAdvisor.builder(chatModel).order(0).build(),
                        getHistoryAdvisor(1, 10),
//                        SimpleLoggerAdvisor.builder().order(2).build(),
                        RagAdvisor.builder(vectorStore).order(3).build(),
                        SimpleLoggerAdvisor.builder().order(4).build())
                .defaultOptions(OllamaOptions.builder()
                        .temperature(0.3)
                        .topP(0.7)
                        .topK(20)
                        .repeatPenalty(1.1)
                        .build())
                .defaultSystem(SYSTEM_PROMPT_TEMPLATE.render())
                .build();
    }


    @Bean
    @ConditionalOnProperty(name = "use.rag", havingValue = "false", matchIfMissing = true)
    public ChatClient chatClientNoRag(ChatClient.Builder builder) {
        log.info("Starting chatClientNoRag");
        return builder
                .defaultAdvisors(
                        SimpleLoggerAdvisor.builder().build(),
                        getHistoryAdvisor(1, 10))
                .build();
    }

    private Advisor getHistoryAdvisor(int order, int maxMessages) {
        return MessageChatMemoryAdvisor.builder(getChatMemory(maxMessages)).order(order).build();
    }

    private ChatMemory getChatMemory(int maxMessages) {
        return PostgresChatMemory.builder()
                .maxMessages(maxMessages)
                .chatMemoryRepository(chatRepository)
                .build();
    }

}
