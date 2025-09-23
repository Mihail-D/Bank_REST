package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByUser(User user);

    List<Card> findByStatus(CardStatus status);

    @Query("SELECT c FROM Card c WHERE c.user.id = :userId AND c.status = :status")
    List<Card> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") CardStatus status);

    Optional<Card> findByEncryptedNumber(String encryptedNumber);

    // Дополнительные методы для оптимизации
    @Query("SELECT c FROM Card c WHERE c.user.id = :userId")
    List<Card> findByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM Card c WHERE c.expirationDate < :date")
    List<Card> findExpiredCards(@Param("date") LocalDate date);

    @Query("SELECT c FROM Card c WHERE c.expirationDate < :date AND c.status = :status")
    List<Card> findExpiredCardsByStatus(@Param("date") LocalDate date, @Param("status") CardStatus status);

    @Query("SELECT COUNT(c) FROM Card c WHERE c.user.id = :userId AND c.status = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") CardStatus status);

    @Query("SELECT c FROM Card c WHERE c.user.id = :userId AND c.status IN :statuses")
    List<Card> findByUserIdAndStatusIn(@Param("userId") Long userId, @Param("statuses") List<CardStatus> statuses);
}
