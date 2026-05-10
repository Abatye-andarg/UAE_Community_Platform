package com.abatye.family_help_uae.service.serviceImpl;

import com.abatye.family_help_uae.model.Sec103_1093910_Message;
import com.abatye.family_help_uae.repository.Sec103_1093910_MessageRepository;
import com.abatye.family_help_uae.service.Sec103_1093910_MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for inter-family messaging within a help task context.
 *
 * <h3>Design decisions:</h3>
 * <ul>
 *   <li>Messages are <b>immutable</b> once sent — no update operation is provided,
 *       ensuring a trustworthy interaction history.</li>
 *   <li>Messages are scoped to a specific {@code HelpTask}, guaranteeing that all
 *       communication is traceable and contextually linked to a support interaction.</li>
 *   <li>The chronologically-ordered retrieval method supports building conversation
 *       views in the frontend without additional sorting.</li>
 * </ul>
 *
 */
@Service
@RequiredArgsConstructor
@Transactional
public class Sec103_1093910_MessageServiceImpl implements Sec103_1093910_MessageService {

    private final Sec103_1093910_MessageRepository messageRepository;

    @Override
    public Sec103_1093910_Message save(Sec103_1093910_Message message) {
        validateMessage(message);
        // Prevent a family from messaging itself
        if (message.getSenderFamily().getId().equals(message.getReceiverFamily().getId())) {
            throw new IllegalArgumentException("Sender and receiver must be different families.");
        }
        return messageRepository.save(message);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Sec103_1093910_Message> findById(Long id) {
        return messageRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sec103_1093910_Message> findAll() {
        return messageRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sec103_1093910_Message> findByHelpTaskId(Long helpTaskId) {
        return messageRepository.findByHelpTaskId(helpTaskId);
    }

    /**
     * Returns all messages for a given task ordered by {@code createdAt} ascending,
     * suitable for rendering a chronological conversation thread.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Sec103_1093910_Message> findByHelpTaskIdOrderByCreatedAtAsc(Long helpTaskId) {
        return messageRepository.findByHelpTaskIdOrderByCreatedAtAsc(helpTaskId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sec103_1093910_Message> findBySenderFamilyId(Long senderFamilyId) {
        return messageRepository.findBySenderFamilyId(senderFamilyId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sec103_1093910_Message> findByConversation(Long familyA, Long familyB) {
        return messageRepository.findConversation(familyA, familyB);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sec103_1093910_Message> findByReceiverFamilyId(Long receiverFamilyId) {
        return messageRepository.findByReceiverFamilyId(receiverFamilyId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sec103_1093910_Message> getRecentConversations(Long familyId) {
        return messageRepository.findRecentConversations(familyId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long familyId) {
        return messageRepository.countByReceiverFamilyIdAndIsReadFalse(familyId);
    }

    @Override
    @Transactional
    public void markAsRead(Long receiverId, Long senderId) {
        List<Sec103_1093910_Message> unread = messageRepository.findByReceiverFamilyIdAndSenderFamilyIdAndIsReadFalse(receiverId, senderId);
        unread.forEach(m -> m.setRead(true));
        messageRepository.saveAllAndFlush(unread);
    }

    @Override
    public void deleteById(Long id) {
        if (!messageRepository.existsById(id)) {
            throw new IllegalArgumentException("Message not found with id: " + id);
        }
        messageRepository.deleteById(id);
    }

    // Private helpers

    private void validateMessage(Sec103_1093910_Message message) {
        if (message.getMessageText() == null || message.getMessageText().isBlank()) {
            throw new IllegalArgumentException("Message text must not be blank.");
        }
        // Tasks are now optional for general outreach
        // if (message.getHelpTask() == null || message.getHelpTask().getId() == null) { ... }
        if (message.getSenderFamily() == null || message.getSenderFamily().getId() == null) {
            throw new IllegalArgumentException("Message must have a sender family.");
        }
        if (message.getReceiverFamily() == null || message.getReceiverFamily().getId() == null) {
            throw new IllegalArgumentException("Message must have a receiver family.");
        }
    }
}
