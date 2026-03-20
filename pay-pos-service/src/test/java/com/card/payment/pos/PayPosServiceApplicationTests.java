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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
		// when
		// then
	}

	@Test
	@DisplayName("정상 결제 요청 시 결제 이력 DB 저장")
	void requestPayment_savesHistory() {
		// given
		// when
		// then
	}

	@Test
	@DisplayName("존재하는 STAN으로 조회 시 정상 응답 리턴")
	void getPaymentResult_success() {
		// given
		// when
		// then
	}

	@Test
	@DisplayName("존재하지 않는 STAN으로 조회 시 RuntimeException 발생")
	void getPaymentResult_notFound() {
		// given
		// when & then
	}
}
