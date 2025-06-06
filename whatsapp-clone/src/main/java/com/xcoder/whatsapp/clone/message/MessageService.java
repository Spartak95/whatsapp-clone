package com.xcoder.whatsapp.clone.message;

import java.util.List;

import com.xcoder.whatsapp.clone.chat.Chat;
import com.xcoder.whatsapp.clone.chat.ChatRepository;
import com.xcoder.whatsapp.clone.file.FileService;
import com.xcoder.whatsapp.clone.file.FileUtils;
import com.xcoder.whatsapp.clone.notification.Notification;
import com.xcoder.whatsapp.clone.notification.NotificationService;
import com.xcoder.whatsapp.clone.notification.NotificationType;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MessageService {
    private static final String CHAT_WITH_ID_S_NOT_FOUND = "Chat with id %s not found";

    private final MessageMapper mapper;
    private final FileService fileService;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final NotificationService notificationService;

    public void saveMessage(MessageRequest messageRequest) {
        Chat chat = chatRepository.findById(messageRequest.getChatId())
            .orElseThrow(
                () -> new EntityNotFoundException(CHAT_WITH_ID_S_NOT_FOUND.formatted(messageRequest.getChatId())));

        Message message = new Message();
        message.setContent(messageRequest.getContent());
        message.setChat(chat);
        message.setSenderId(messageRequest.getSenderId());
        message.setReceiverId(messageRequest.getReceiverId());
        message.setType(messageRequest.getType());
        message.setState(MessageState.SENT);

        messageRepository.save(message);

        Notification notification = Notification.builder()
            .chatId(chat.getId())
            .messageType(messageRequest.getType())
            .content(messageRequest.getContent())
            .senderId(messageRequest.getSenderId())
            .receiverId(messageRequest.getReceiverId())
            .type(NotificationType.MESSAGE)
            .chatName(chat.getTargetChatName(message.getSenderId()))
            .build();

        notificationService.sendNotification(message.getReceiverId(), notification);
    }

    public List<MessageResponse> findChatMessages(String chatId) {
        return messageRepository.findMessagesByChatId(chatId)
            .stream()
            .map(mapper::toMessageResponse)
            .toList();
    }

    @Transactional
    public void setMessagesToSeen(String chatId, Authentication authentication) {
        Chat chat = chatRepository.findById(chatId)
            .orElseThrow(() -> new EntityNotFoundException(CHAT_WITH_ID_S_NOT_FOUND.formatted(chatId)));

        final String recipientId = getRecipientId(chat, authentication);

        messageRepository.setMessagesToSeenByChatId(chatId, MessageState.SEEN);

        Notification notification = Notification.builder()
            .chatId(chat.getId())
            .senderId(getSenderId(chat, authentication))
            .receiverId(recipientId)
            .type(NotificationType.SEEN)
            .build();

        notificationService.sendNotification(recipientId, notification);
    }

    public void uploadMediaMessage(String chatId, MultipartFile file, Authentication authentication) {
        Chat chat = chatRepository.findById(chatId)
            .orElseThrow(() -> new EntityNotFoundException(CHAT_WITH_ID_S_NOT_FOUND.formatted(chatId)));

        final String senderId = getSenderId(chat, authentication);
        final String recipientId = getRecipientId(chat, authentication);

        final String filePath = fileService.saveFile(file, senderId);
        Message message = new Message();
        message.setChat(chat);
        message.setSenderId(senderId);
        message.setReceiverId(recipientId);
        message.setType(MessageType.IMAGE);
        message.setState(MessageState.SENT);
        message.setMediaFilePath(filePath);

        messageRepository.save(message);

        Notification notification = Notification.builder()
            .chatId(chat.getId())
            .type(NotificationType.MESSAGE)
            .messageType(MessageType.IMAGE)
            .senderId(senderId)
            .receiverId(recipientId)
            .media(FileUtils.readFileFromLocation(filePath))
            .build();

        notificationService.sendNotification(recipientId, notification);
    }

    private String getSenderId(Chat chat, Authentication authentication) {
        if (chat.getSender().getId().equals(authentication.getName())) {
            return chat.getSender().getId();
        }
        return chat.getRecipient().getId();
    }

    private String getRecipientId(Chat chat, Authentication authentication) {
        if (chat.getSender().getId().equals(authentication.getName())) {
            return chat.getRecipient().getId();
        }
        return chat.getSender().getId();
    }
}
