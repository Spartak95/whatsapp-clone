package com.xcoder.whatsapp.clone.chat;

import java.util.List;
import java.util.Optional;

import com.xcoder.whatsapp.clone.user.User;
import com.xcoder.whatsapp.clone.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatMapper mapper;
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;

    @Transactional(readOnly = true)
    public List<ChatResponse> getChatsByReceiverId(Authentication currentUser) {
        final String userId = currentUser.getName();
        return chatRepository.findChatsBySenderId(userId)
            .stream()
            .map(c -> mapper.toChatResponse(c, userId))
            .toList();
    }

    public String createChat(String senderId, String receiverId) {
        Optional<Chat> exitingChat = chatRepository.findChatByReceiverAndSender(senderId, receiverId);
        if (exitingChat.isPresent()) {
            return exitingChat.get().getId();
        }

        User sender = userRepository.findByPublicId(senderId)
            .orElseThrow(() -> new EntityNotFoundException("User with id %s not found".formatted(senderId)));

        User receiver = userRepository.findByPublicId(receiverId)
            .orElseThrow(() -> new EntityNotFoundException("User with id %s not found".formatted(receiverId)));

        Chat chat = new Chat();
        chat.setSender(sender);
        chat.setRecipient(receiver);

        Chat savedChat = chatRepository.save(chat);
        return savedChat.getId();
    }
}
