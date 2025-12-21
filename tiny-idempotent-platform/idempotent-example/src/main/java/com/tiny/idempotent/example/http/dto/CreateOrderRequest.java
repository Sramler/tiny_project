package com.tiny.idempotent.example.http.dto;

import lombok.Data;

/**
 * 创建订单请求 DTO
 * 
 * @author Auto Generated
 * @since 1.0.0
 */
@Data
public class CreateOrderRequest {
    
    /**
     * 订单号（用于生成幂等 Key）
     */
    private String orderNo;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 商品ID
     */
    private Long productId;
    
    /**
     * 商品数量
     */
    private Integer quantity;
    
    /**
     * 订单金额
     */
    private Long amount;
}

