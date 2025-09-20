package org.jeka.demowebinar1no_react.services;

import lombok.SneakyThrows;
import org.jeka.demowebinar1no_react.model.LoadedDocument;
import org.jeka.demowebinar1no_react.repo.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DocumentLoaderService implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(DocumentLoaderService.class);
    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private ResourcePatternResolver resolver;

    @Autowired
    private VectorStore vectorStore;

    @Scheduled(initialDelay = 1,fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    public void loadDocuments() throws IOException {
        List<Resource> resources = Arrays.stream(resolver.getResources("classpath:/knowledgebase/**/*.*")).toList();
        log.info("Found {} resources", resources.size());
        resources.stream()
                .map(resource -> Pair.of(resource, calcContentHash(resource)))
                .filter(pair -> !documentRepository.existsByFilenameAndContentHash(pair.getFirst().getFilename(), pair.getSecond()))
                .forEach(pair -> {
                    log.info("Loading document {}", pair.getFirst().getFilename());
                    Resource resource = pair.getFirst();
                    List<Document> documents = new TextReader(resource).get();
                    TokenTextSplitter textSplitter = TokenTextSplitter.builder().withChunkSize(200).build();
                    List<Document> chunks = textSplitter.apply(documents);
                    vectorStore.accept(chunks);
                    log.info("Loaded document {} with {} chunks", resource.getFilename(), chunks.size());
                    LoadedDocument loadedDocument = LoadedDocument.builder()
                            .documentType("txt")
                            .chunkCount(chunks.size())
                            .filename(resource.getFilename())
                            .contentHash(pair.getSecond())
                            .build();
                    documentRepository.save(loadedDocument);
                });


    }

    @SneakyThrows
    private String calcContentHash(Resource resource) {
        return DigestUtils.md5DigestAsHex(resource.getInputStream());
    }


    @Override
    public void afterPropertiesSet() throws IOException {
        loadDocuments();
    }
}
