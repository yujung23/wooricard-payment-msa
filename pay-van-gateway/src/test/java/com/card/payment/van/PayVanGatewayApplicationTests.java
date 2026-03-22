package com.card.payment.van;

import com.card.payment.van.client.CardAuthorizationClient;
import com.card.payment.van.dto.*;
import com.card.payment.van.repository.PaymentHistoryRepository;
import com.card.payment.van.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest
class PayVanGatewayApplicationTests {

	@Autowired
	private PaymentService paymentService;

	@Autowired
	private PaymentHistoryRepository paymentHistoryRepository;

	@MockitoBean // 실제 카드사 서버 대신 가짜 응답을 제공할 Mock 객체
	private CardAuthorizationClient cardAuthorizationClient;

	@Test
	@DisplayName("테스트 1: VISA 카드 결제 승인 성공 시 응답 데이터 매핑 및 DB 저장이 정상적으로 수행되는지 검증")
	void approvePayment_Success_Visa() {
		// given: 가맹점(POS)에서 들어온 결제 요청 데이터
		PosPaymentRequest posRequest = new PosPaymentRequest(
				"4123456789012345", "2028-12", 50000L,
				"MERCHANT_001", "TERM_01", 0, "ORDER_001"
		);

		// 카드사 서버의 정상 승인 응답 모의(Mocking)
		String mockStan = "VAN-STAN-001";
		CardAuthorizationResponse authResponse = new CardAuthorizationResponse(
				mockStan, "APP-123", "00", "승인완료",
				50000L, LocalDateTime.now(), true
		);
		given(cardAuthorizationClient.requestAuthorization(any())).willReturn(authResponse);

		// when: VAN 서비스 로직 실행
		PosPaymentResponse result = paymentService.approvePayment(posRequest);

		// then: 1. 응답 데이터 검증 (카드사 판별 및 데이터 매핑)
		assertThat(result.cardCompany()).isEqualTo("VISA");
		assertThat(result.systemTraceAuditNumber()).isEqualTo(mockStan);
		assertThat(result.responseCode()).isEqualTo("00");

		// then: 2. DB 저장 여부 확인 (영속성 검증)
		boolean existsInDb = paymentHistoryRepository.findAll().stream()
				.anyMatch(h -> h.getApprovalId().equals(mockStan));
		assertThat(existsInDb).isTrue();
	}

	@Test
	@DisplayName("테스트 2: 잔액 부족(51)으로 승인 실패 시 카드사 응답 코드와 메시지가 POS로 정상 전달되는지 검증")
	void approvePayment_Fail_InsufficientFunds() {
		// given: 우리카드('9' 시작) 결제 요청
		PosPaymentRequest posRequest = new PosPaymentRequest(
				"9123456789012345", "2028-12", 1000L,
				"MERCHANT_001", "TERM_01", 0, "ORDER_002"
		);

		// 카드사 서버에서 '51(잔액부족)' 코드를 반환한다고 가정
		CardAuthorizationResponse authResponse = new CardAuthorizationResponse(
				"VAN-STAN-002", null, "51", "잔액부족",
				1000L, LocalDateTime.now(), false
		);
		given(cardAuthorizationClient.requestAuthorization(any())).willReturn(authResponse);

		// when: 서비스 실행
		PosPaymentResponse result = paymentService.approvePayment(posRequest);

		// then: 실패 코드와 메시지가 POS로 정확히 전달되는지 확인
		assertThat(result.cardCompany()).isEqualTo("WOORICARD");
		assertThat(result.responseCode()).isEqualTo("51");
		assertThat(result.responseMessage()).isEqualTo("잔액부족");
	}

	@Test
	@DisplayName("테스트 3: 미정의 카드번호 '1' 입력 시 카드사가 UNKNOWN으로 식별되는지 검증")
	void approvePayment_Unknown_Company() {
		// given: 정의되지 않은 번호로 시작하는 카드 요청
		PosPaymentRequest posRequest = new PosPaymentRequest(
				"1123456789012345", "2028-12", 1000L,
				"MERCHANT_001", "TERM_01", 0, "ORDER_003"
		);

		CardAuthorizationResponse authResponse = new CardAuthorizationResponse(
				"VAN-STAN-003", "APP-456", "00", "승인완료",
				1000L, LocalDateTime.now(), true
		);
		given(cardAuthorizationClient.requestAuthorization(any())).willReturn(authResponse);

		// when: 서비스 실행
		PosPaymentResponse result = paymentService.approvePayment(posRequest);

		// then: 카드사 식별 로직이 UNKNOWN을 반환하는지 확인
		assertThat(result.cardCompany()).isEqualTo("UNKNOWN");
	}
}