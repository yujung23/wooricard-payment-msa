package com.card.payment.pos;

import com.card.payment.pos.client.VanClient;
import com.card.payment.pos.dto.PaymentRequest;
import com.card.payment.pos.dto.PaymentResponse;
import com.card.payment.pos.entity.PaymentHistory;
import com.card.payment.pos.entity.PaymentHistoryRepository;
import com.card.payment.pos.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * "00" → 승인 성공
 * "51" → 잔액 부족
 * "61" → 한도 초과
 * "54" → 유효기간 만료
 * "14" → 유효하지 않은 카드
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

	@InjectMocks
	private PaymentService paymentService;

	@Mock
	private VanClient vanClient;

	@Mock
	private PaymentHistoryRepository paymentHistoryRepository;

	@Test
	@DisplayName("정상 결제 요청 시 VAN 호출 후 승인 응답 리턴")
	void requestPayment_success() {
		// given
		PaymentRequest request = new PaymentRequest (
				"4689140002870206", "2029-07",
				50000L, "MERCHANT_001", 0
		);
		PaymentResponse mockResponse = PaymentResponse.from(
				"APR20260320001", "00", "승인완료", "HYUNDAI"
		);

		when(vanClient.requestApproval(request)).thenReturn(mockResponse);
		when(paymentHistoryRepository.save(any(PaymentHistory.class))).thenReturn(null);

		// when
		PaymentResponse result = paymentService.requestPayment(request);


		// then
	}

	@Test
	@DisplayName("정상 결제 요청 시 결제 이력 DB 저장")
	void requestPayment_savesHistory() {
		// given
		PaymentRequest request = new PaymentRequest (
				"4689140002870206", "2029-07",
				50000L, "MERCHANT_001", 0
		);
		PaymentResponse mockResponse = PaymentResponse.from(
				"APR20260320001", "00", "승인완료", "HYUNDAI"
		);

		when(vanClient.requestApproval(request)).thenReturn(mockResponse);
		when(paymentHistoryRepository.save(any(PaymentHistory.class))).thenReturn(null);

		// when
		paymentService.requestPayment(request);

		// then
		ArgumentCaptor<PaymentHistory> captor = ArgumentCaptor.forClass(PaymentHistory.class);
		verify(paymentHistoryRepository, times(1)).save(captor.capture());

		PaymentHistory saved = captor.getValue();
		assertThat(saved.getPrimaryAccountNumber()).isEqualTo("4689-****-****-0206");
		assertThat(saved.getTransactionAmount()).isEqualTo(50000L);
		assertThat(saved.getResponseCode()).isEqualTo("00");

	}

	/**
	 * STAN: System Trace Audit Number의 약자
	 *  결제 건마다 부여되는 거래 추적 번호
	 *  ISO 8583에서는 DE11번에 해당
	 *
	 *  GET: /api/v1/approval/{stan}
	 */
	@Test
	@DisplayName("존재하는 STAN으로 조회 시 정상 응답 리턴")
	void getPaymentResult_success() {
//		// given
		PaymentHistory mockHistory = PaymentHistory.builder()
				.systemTraceAuditNumber("APR20260320001")
				.primaryAccountNumber("4689-****-****-0206")
				.transactionAmount(50000L)
				.responseCode("00")
				.cardCompany("HYUNDAI")
				.cardAcceptorId("MERCHANT_001")
				.build();

		when(paymentHistoryRepository.findBySystemTraceAuditNumber("APR20260320001"))
				.thenReturn(Optional.of(mockHistory));

		// when
		PaymentResponse result = paymentService.getPaymentResult("APR20260320001");

		// then
		assertThat(result.systemTraceAuditNumber()).isEqualTo("APR20260320001");
		assertThat(result.responseCode()).isEqualTo("00");
		assertThat(result.cardCompany()).isEqualTo("HYUNDAI");
	}

	@Test
	@DisplayName("존재하지 않는 STAN으로 조회 시 RuntimeException 발생")
	void getPaymentResult_notFound() {
		// given
		when(paymentHistoryRepository.findBySystemTraceAuditNumber("INVALID_STAN"))
				.thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> paymentService.getPaymentResult("INVALID_STAN"))
				.isInstanceOf(RuntimeException.class)
				.hasMessageContaining("결제 내역을 찾을 수 없습니다");

	}
}
