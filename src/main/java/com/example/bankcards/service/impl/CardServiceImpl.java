package com.example.bankcards.service.impl;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.CardNumberGenerator;
import com.example.bankcards.service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    private final CardNumberGenerator cardNumberGenerator;

    @Autowired
    public CardServiceImpl(CardRepository cardRepository, CardNumberGenerator cardNumberGenerator) {
        this.cardRepository = cardRepository;
        this.cardNumberGenerator = cardNumberGenerator;
    }

    @Override
    public Card createCard(User user) {
        Card card = new Card();
        card.setUser(user);
        card.setNumber(cardNumberGenerator.generateUniqueCardNumber());
        card.setStatus(CardStatus.ACTIVE);
        card.setExpirationDate(LocalDate.now().plusYears(3));
        return cardRepository.save(card);
    }
}
