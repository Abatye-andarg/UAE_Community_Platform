package com.abatye.family_help_uae.service;

import com.abatye.family_help_uae.model.Sec103_1093910_Message;

import java.util.List;
import java.util.Optional;

public interface Sec103_1093910_MessageService {

    Sec103_1093910_Message save(Sec103_1093910_Message message);

    Optional<Sec103_1093910_Message> findById(Long id);

    List<Sec103_1093910_Message> findAll();

    List<Sec103_1093910_Message> findByHelpTaskId(Long helpTaskId);

    List<Sec103_1093910_Message> findByHelpTaskIdOrderByCreatedAtAsc(Long helpTaskId);

    List<Sec103_1093910_Message> findBySenderFamilyId(Long senderFamilyId);

    List<Sec103_1093910_Message> findByReceiverFamilyId(Long receiverFamilyId);
    
    List<Sec103_1093910_Message> findByConversation(Long familyA, Long familyB);

    List<Sec103_1093910_Message> getRecentConversations(Long familyId);

    long getUnreadCount(Long familyId);

    void markAsRead(Long receiverId, Long senderId);

    void deleteById(Long id);
}
