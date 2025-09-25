package org.jeka.demowebinar1no_react.advisors;

import lombok.Builder;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.ollama.api.OllamaOptions;

import java.util.Map;

@Builder
public class ExpansionQueryAdvisor implements BaseAdvisor {

    public static final String ENRICHED_QUESTION = "ENRICHED_QUESTION";
    public static final String ORIGINAL_QUESTION = "ORIGINAL_QUESTION";
    public static final String EXPANSION_RATION = "EXPANSION_RATION";
    private int order;
    private ChatClient chatClient;
    private static final PromptTemplate template = PromptTemplate.builder()
            .template("""
                    if the question contains any second-person references in Russian (such as "ты", "тебе", "у тебя", "тобой" etc.), reformulate them by replacing references to "Андрей Зайцев".
                    If there are no such references return question unchanged.
                    
                    Question: {question},
                    Reformulated:
                    """

            )
            .build();

    public static ExpansionQueryAdvisorBuilder builder(ChatModel chatModel) {
        return new ExpansionQueryAdvisorBuilder().chatClient(ChatClient.builder(chatModel)
                        .defaultOptions(OllamaOptions.builder()
                                .temperature(0.0)
                                .topK(1)
                                .topP(0.1)
                                .repeatPenalty(1.0)
                                .build())
                .build());
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        String userMessage = chatClientRequest.prompt().getUserMessage().getText();
        String enrichedQuestion = chatClient.prompt().user(template.render(Map.of("question", userMessage))).call().content();
        return chatClientRequest.mutate()
                .context(ORIGINAL_QUESTION, userMessage)
                .context(ENRICHED_QUESTION, enrichedQuestion)
                .context(EXPANSION_RATION, enrichedQuestion.length()/ (double) userMessage.length())
                .build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse;
    }

    @Override
    public int getOrder() {
        return order;
    }
}
