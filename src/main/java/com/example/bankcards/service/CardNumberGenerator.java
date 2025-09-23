package com.example.bankcards.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.bankcards.repository.CardRepository;
import java.security.SecureRandom;

@Service
public class CardNumberGenerator {
    private static final int CARD_NUMBER_LENGTH = 16;
    private final SecureRandom random = new SecureRandom();
    private final CardRepository cardRepository;
    private final CardEncryptionService cardEncryptionService;

    @Autowired
    public CardNumberGenerator(CardRepository cardRepository, CardEncryptionService cardEncryptionService) {
        this.cardRepository = cardRepository;
        this.cardEncryptionService = cardEncryptionService;
    }

    /**
     * Проверка номера карты по алгоритму Луна
     */
    public static boolean isValidLuhn(String cardNumber) {
        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = cardNumber.charAt(i) - '0';
            if (alternate) {
                n *= 2;
                if (n > 9) n -= 9;
            }
            sum += n;
            alternate = !alternate;
        }
        return sum % 10 == 0;
    }

    /**
     * Генерация уникального номера карты, проходящего Луна
     */
    public String generateUniqueCardNumber() {
        String cardNumber;
        String encrypted;
        do {
            cardNumber = generateCardNumber();
            encrypted = cardEncryptionService.encrypt(cardNumber);
        } while (!isValidLuhn(cardNumber) || cardRepository.findByEncryptedNumber(encrypted).isPresent());
        return cardNumber;
    }

    private String generateCardNumber() {
        StringBuilder sb = new StringBuilder(CARD_NUMBER_LENGTH);
        for (int i = 0; i < CARD_NUMBER_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
