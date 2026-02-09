package com.example.pix.application.port.out.usecase;

import com.example.pix.application.port.in.model.PixPaymentRequest;
import com.example.pix.application.port.out.model.FraudResponse;

public interface FraudCheckPort {
    FraudResponse check(PixPaymentRequest request);
}
