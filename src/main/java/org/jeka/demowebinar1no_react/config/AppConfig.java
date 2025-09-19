package org.jeka.demowebinar1no_react.config;

import lombok.extern.slf4j.Slf4j;
import org.jeka.demowebinar1no_react.repo.ChatRepository;
import org.jeka.demowebinar1no_react.services.PostgresChatMemory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class AppConfig {

    private static final PromptTemplate MY_PROMPT_TEMPLATE = new PromptTemplate(
            "{query}\n\n" +
                    "Контекст:\n" +
                    "---------------------\n" +
                    "{question_answer_context}\n" +
                    "---------------------\n\n" +
                    "Отвечай только на основе контекста выше. Если информации нет в контексте, сообщи, что не можешь ответить."
    );


    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private VectorStore vectorStore;


    @Bean
    @ConditionalOnProperty(name = "use.rag", havingValue = "true")
    public ChatClient chatClient(ChatClient.Builder builder) {
        log.info("Starting chatClient with RAG");
        return builder.defaultAdvisors(getHistoryAdvisor(), getRagAdviser()).build();
    }


    @Bean
    @ConditionalOnProperty(name = "use.rag", havingValue = "false", matchIfMissing = true)
    public ChatClient chatClientNoRag(ChatClient.Builder builder) {
        log.info("Starting chatClientNoRag");
        return builder.defaultAdvisors(
                SimpleLoggerAdvisor.builder().build(),
                getHistoryAdvisor()).build();
    }

    private Advisor getRagAdviser() {
        return QuestionAnswerAdvisor.builder(vectorStore).promptTemplate(MY_PROMPT_TEMPLATE).searchRequest(
                SearchRequest.builder().topK(4).build()
        ).build();
    }


    private Advisor getHistoryAdvisor() {
        return MessageChatMemoryAdvisor.builder(getChatMemory()).order(-10).build();
    }

    private ChatMemory getChatMemory() {
        return PostgresChatMemory.builder()
                .maxMessages(12)
                .chatMemoryRepository(chatRepository)
                .build();
    }

}
