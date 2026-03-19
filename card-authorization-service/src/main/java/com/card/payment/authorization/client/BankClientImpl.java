package com.card.payment.authorization.client;

import com.card.payment.authorization.dto.BalanceRequest;
import com.card.payment.authorization.dto.BalanceResponse;
import com.card.payment.authorization.dto.DebitRequest;
import com.card.payment.authorization.dto.DebitResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * 은행 서비스 클라이언트 구현체 (RestClient 기반)
 * 타임아웃 설정:
 * - 잔액 조회: 10초
 * - 출금: 30초
 * 재전송 로직: 최대 1회
 */
@Component
public class BankClientImpl implements BankClient {
    
    private static final Logger logger = LoggerFactory.getLogger(BankClientImpl.class);
    private static final int MAX_RETRY_COUNT = 1;
    private static final Duration BALANCE_TIMEOUT = Duration.ofSeconds(10); // 잔액 조회 타임아웃: 10초
    private static final Duration DEBIT_TIMEOUT = Duration.ofSeconds(30);   // 출금 타임아웃: 30초
    
    private final RestClient balanceRestClient;
    private final RestClient debitRestClient;
    
    public BankClientImpl(@Value("${bank.service.url:http://localhost:8080}") String bankServiceUrl) {
        // 잔액 조회용 RestClient (타임아웃: 10초)
        JdkClientHttpRequestFactory balanceRequestFactory = new JdkClientHttpRequestFactory();
        balanceRequestFactory.setReadTimeout(BALANCE_TIMEOUT);
        
        this.balanceRestClient = RestClient.builder()
                .baseUrl(bankServiceUrl)
                .requestFactory(balanceRequestFactory)
                .build();
        
        // 출금용 RestClient (타임아웃: 30초)
        JdkClientHttpRequestFactory debitRequestFactory = new JdkClientHttpRequestFactory();
        debitRequestFactory.setReadTimeout(DEBIT_TIMEOUT);
        
        this.debitRestClient = RestClient.builder()
                .baseUrl(bankServiceUrl)
                .requestFactory(debitRequestFactory)
                .build();
    }
    
    @Override
    public BalanceResponse checkBalance(String cardNumber, BigDecimal amount) {
        BalanceRequest request = new BalanceRequest(cardNumber, amount);
        return executeWithRetry(() -> performBalanceCheck(request), "잔액 조회");
    }
    
    @Override
    public DebitResponse requestDebit(String cardNumber, BigDecimal amount, String transactionId) {
        DebitRequest request = new DebitRequest(cardNumber, amount, transactionId);
        return executeWithRetry(() -> performDebit(request), "출금");
    }
    
    /**
     * 잔액 조회 실행 (타임아웃: 10초)
     */
    private BalanceResponse performBalanceCheck(BalanceRequest request) {
        logger.info("은행 잔액 조회 요청: cardNumber={}, amount={}", 
                maskCardNumber(request.getCardNumber()), request.getAmount());
        
        try {
            BalanceResponse response = balanceRestClient.post()
                    .uri("/api/account/balance")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(BalanceResponse.class);
            
            logger.info("은행 잔액 조회 성공: sufficient={}", response.isSufficient());
            return response;
            
        } catch (Exception e) {
            logger.error("은행 잔액 조회 실패: {}", e.getMessage());
            throw new BankClientException("잔액 조회 실패", e);
        }
    }
    
    /**
     * 출금 실행 (타임아웃: 30초)
     */
    private DebitResponse performDebit(DebitRequest request) {
        logger.info("은행 출금 요청: cardNumber={}, amount={}, transactionId={}", 
                maskCardNumber(request.getCardNumber()), request.getAmount(), request.getTransactionId());
        
        try {
            DebitResponse response = debitRestClient.post()
                    .uri("/api/account/debit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(DebitResponse.class);
            
            logger.info("은행 출금 성공: transactionId={}", response.getTransactionId());
            return response;
            
        } catch (Exception e) {
            logger.error("은행 출금 실패: {}", e.getMessage());
            throw new BankClientException("출금 실패", e);
        }
    }
    
    /**
     * 재전송 로직 포함 실행 (최대 1회 재시도)
     */
    private <T> T executeWithRetry(RetryableOperation<T> operation, String operationName) {
        int attemptCount = 0;
        Exception lastException = null;
        
        while (attemptCount <= MAX_RETRY_COUNT) {
            try {
                return operation.execute();
            } catch (Exception e) {
                lastException = e;
                attemptCount++;
                
                if (attemptCount <= MAX_RETRY_COUNT) {
                    logger.warn("{} 실패 (시도 {}/{}), 재시도 중...", 
                            operationName, attemptCount, MAX_RETRY_COUNT + 1);
                    try {
                        Thread.sleep(1000); // 1초 대기 후 재시도
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new BankClientException(operationName + " 재시도 중단", ie);
                    }
                } else {
                    logger.error("{} 최종 실패 (시도 {}회)", operationName, attemptCount);
                }
            }
        }
        
        throw new BankClientException(operationName + " 최종 실패", lastException);
    }
    
    /**
     * 카드 번호 마스킹 (로그용)
     */
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 8) {
            return "****";
        }
        return cardNumber.substring(0, 4) + "****" + cardNumber.substring(cardNumber.length() - 4);
    }
    
    @FunctionalInterface
    private interface RetryableOperation<T> {
        T execute() throws Exception;
    }
    
    /**
     * 은행 클라이언트 예외
     */
    public static class BankClientException extends RuntimeException {
        public BankClientException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
