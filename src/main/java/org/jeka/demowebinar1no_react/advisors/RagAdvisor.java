package org.jeka.demowebinar1no_react.advisors;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jeka.demowebinar1no_react.advisors.ExpansionQueryAdvisor.ENRICHED_QUESTION;

@Slf4j
@Builder
public class RagAdvisor implements BaseAdvisor {

    private VectorStore vectorStore;
    private int order;
    @Builder.Default
    private SearchRequest searchRequest = SearchRequest.builder()
            .topK(1)
            .similarityThreshold(0.62).build();

    @Builder.Default
    private BM25RerankEngine rerankEngine = BM25RerankEngine.builder().build();

    private static final PromptTemplate template = PromptTemplate.builder()
            .template("""
                    
                    CONTEXT: {context}
                    ---
                    Question: {question}
                    """)
            .build();

    public static RagAdvisorBuilder builder(VectorStore vectorStore) {
        return new RagAdvisorBuilder().vectorStore(vectorStore);
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        String originalUserMessage = chatClientRequest.prompt().getUserMessage().getText();
        String queryToRag = chatClientRequest.context().getOrDefault(ENRICHED_QUESTION, originalUserMessage).toString();

        int topK = searchRequest.getTopK();
        List<Document> documents = vectorStore.similaritySearch(SearchRequest.from(searchRequest).query(queryToRag).topK(topK *2).build());
        if(documents == null || documents.isEmpty()) {
            log.warn("Ни один документ в базе знаний по запросу:{}, не найден!", originalUserMessage);
            return chatClientRequest;
        }
        documents = rerankEngine.rerank(documents, queryToRag, topK);
        String llmContext = documents.stream().map(Document::getText).collect(Collectors.joining(System.lineSeparator()));
        String finalUserMessage = template.render(Map.of("context", llmContext, "question", originalUserMessage));
        return chatClientRequest.mutate().prompt(chatClientRequest.prompt().augmentUserMessage(finalUserMessage)).build();
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
