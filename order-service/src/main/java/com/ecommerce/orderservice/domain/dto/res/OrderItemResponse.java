package com.ecommerce.orderservice.domain.dto.res;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private Long orderItemId;
    private Long productId;
    private String productName;
    private int quantity;
    private Long itemPrice;
    private Long totalPrice;
}
