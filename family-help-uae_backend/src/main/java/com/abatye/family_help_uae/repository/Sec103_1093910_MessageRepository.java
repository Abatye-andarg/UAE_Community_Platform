package com.abatye.family_help_uae.repository;

import com.abatye.family_help_uae.model.Sec103_1093910_Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Sec103_1093910_MessageRepository extends JpaRepository<Sec103_1093910_Message, Long> {

    List<Sec103_1093910_Message> findByHelpTaskId(Long helpTaskId);

    List<Sec103_1093910_Message> findByReceiverFamilyId(Long receiverFamilyId);

    List<Sec103_1093910_Message> findByReceiverFamilyIdAndSenderFamilyIdAndIsReadFalse(Long receiverId, Long senderId);

    long countByReceiverFamilyIdAndIsReadFalse(Long receiverFamilyId);

    List<Sec103_1093910_Message> findBySenderFamilyId(Long senderFamilyId);

    List<Sec103_1093910_Message> findBySenderFamilyIdOrReceiverFamilyId(Long senderId, Long receiverId);

    @org.springframework.data.jpa.repository.Query("SELECT m FROM Sec103_1093910_Message m WHERE (m.senderFamily.id = :f1 AND m.receiverFamily.id = :f2) OR (m.senderFamily.id = :f2 AND m.receiverFamily.id = :f1) ORDER BY m.createdAt ASC")
    List<Sec103_1093910_Message> findConversation(@org.springframework.data.repository.query.Param("f1") Long familyA, @org.springframework.data.repository.query.Param("f2") Long familyB);

    List<Sec103_1093910_Message> findByHelpTaskIdOrderByCreatedAtAsc(Long helpTaskId);

    @org.springframework.data.jpa.repository.Query(value = 
        "SELECT * FROM message m WHERE m.id IN (" +
        "  SELECT MAX(id) FROM message " +
        "  WHERE sender_family_id = :familyId OR receiver_family_id = :familyId " +
        "  GROUP BY LEAST(sender_family_id, receiver_family_id), GREATEST(sender_family_id, receiver_family_id)" +
        ") ORDER BY m.created_at DESC", nativeQuery = true)
    List<Sec103_1093910_Message> findRecentConversations(@org.springframework.data.repository.query.Param("familyId") Long familyId);

    void deleteBySenderFamilyIdOrReceiverFamilyId(Long senderId, Long receiverId);
}
