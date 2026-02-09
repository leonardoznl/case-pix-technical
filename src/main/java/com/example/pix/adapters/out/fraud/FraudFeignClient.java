package com.example.pix.adapters.out.fraud;

import com.example.pix.application.port.out.model.FraudResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "fraudClient",
        url = "${pix.fraud.base-url}",
        configuration = FraudFeignConfig.class
)
public interface FraudFeignClient {
    @PostMapping
    FraudResponse check(@RequestBody FraudCheckRequest request);
}
