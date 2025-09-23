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

    Optional<Card> findByNumber(String number);

    boolean existsByNumber(String number);

    List<Card> findByUser(User user);

    List<Card> findByStatus(CardStatus status);

    List<Card> findByExpirationDateBefore(LocalDate date);

    @Query("SELECT c FROM Card c WHERE c.user.id = :userId AND c.status = :status")
    List<Card> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") CardStatus status);
}
