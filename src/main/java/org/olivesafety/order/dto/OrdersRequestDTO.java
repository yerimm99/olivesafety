package org.olivesafety.order.dto;

import lombok.Getter;
import javax.validation.constraints.NotBlank;

public class OrdersRequestDTO {
    @Getter
    public static class ordersAddDTO {
        @NotBlank
        Long id;

        @NotBlank
        String name;

        @NotBlank
        String phone;

        @NotBlank
        String payType;

        @NotBlank
        Long amount;

        String code;

    }

}
