package org.and.demo.repo;

import org.and.demo.model.Chat;
import org.and.demo.model.ChatEntry;
import org.and.demo.model.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ChatRepositoryTest {

    @Autowired
    private ChatRepository chatRepository;


    @BeforeEach
    public void setUp() {
        Chat chat = new Chat();
        chat.setTitle("test");
        ChatEntry chatEntry = new ChatEntry();
        chatEntry.setContent("test content");
        chatEntry.setRole(Role.USER);
        chat.setHistory(List.of(chatEntry));
        chatRepository.save(chat);
    }

    @AfterEach
    public void tearDown() {
        // Release test data after each test method
        chatRepository.deleteAll();
    }

    @Test
    void findAll() {
        List<Chat> chats = chatRepository.findAll();
        assertNotNull(chats);
        assertEquals(1, chats.size());
        assertEquals( "test", chats.getFirst().getTitle());
        assertEquals(1, chats.getFirst().getHistory().size());
    }
}