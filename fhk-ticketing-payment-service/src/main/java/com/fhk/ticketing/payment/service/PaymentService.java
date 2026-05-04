package com.fhk.ticketing.payment.service;

import com.fhk.ticketing.payment.dto.PaymentDtos.*;

public interface PaymentService {

    PaymentRes createPayment(Long accountId, String authorization, CreatePaymentReq request);

    PaymentRes getPayment(String paymentNo);

    CallbackResultRes processCallback(PgCallbackReq request);

    CallbackResultRes mockSuccess(String paymentNo);

    CallbackResultRes mockFail(String paymentNo);

    CallbackLogListRes getCallbacks(String paymentNo);
}
