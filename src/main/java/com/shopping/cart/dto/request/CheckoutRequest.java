package com.shopping.cart.dto.request;

import com.shopping.cart.model.Address;
import com.shopping.cart.model.PaymentInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {

    @NotNull(message = "Shipping address is required")
    @Valid
    private Address shippingAddress;

    @NotNull(message = "Payment information is required")
    @Valid
    private PaymentInfo paymentInfo;
}
