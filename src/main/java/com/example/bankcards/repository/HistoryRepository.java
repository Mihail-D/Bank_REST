package com.example.bankcards.repository;

import com.example.bankcards.entity.History;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface HistoryRepository extends JpaRepository<History, Long>, JpaSpecificationExecutor<History> {

    List<History> findByUser(User user);

    List<History> findByCard(Card card);

    List<History> findByTransfer(Transfer transfer);

    List<History> findByEventType(String eventType);

    List<History> findByEventDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT h FROM History h WHERE h.user.id = :userId ORDER BY h.eventDate DESC")
    List<History> findByUserIdOrderByEventDateDesc(@Param("userId") Long userId);

    @Query("SELECT h FROM History h WHERE h.card.id = :cardId ORDER BY h.eventDate DESC")
    List<History> findByCardIdOrderByEventDateDesc(@Param("cardId") Long cardId);
}
