package com.example.bankcards.repository;

import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

    List<Transfer> findBySourceCard(Card sourceCard);

    List<Transfer> findByDestinationCard(Card destinationCard);

    List<Transfer> findByStatus(String status);

    List<Transfer> findByTransferDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT t FROM Transfer t WHERE t.sourceCard.id = :cardId OR t.destinationCard.id = :cardId")
    List<Transfer> findByCardId(@Param("cardId") Long cardId);

    @Query("SELECT t FROM Transfer t WHERE (t.sourceCard.user.id = :userId OR t.destinationCard.user.id = :userId)")
    List<Transfer> findByUserId(@Param("userId") Long userId);
}
