package com.tiny.idempotent.example.http.controller;

import com.tiny.idempotent.example.http.dto.CreateOrderRequest;
import com.tiny.idempotent.example.http.dto.OrderResponse;
import com.tiny.idempotent.example.http.service.OrderService;
import com.tiny.idempotent.sdk.annotation.Idempotent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 订单服务 Controller 示例
 * 
 * <p>演示如何使用 @Idempotent 注解实现 HTTP 接口幂等性</p>
 * 
 * @author Auto Generated
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单 - 幂等接口示例
     * 
     * <p>使用 @Idempotent 注解，自动处理幂等性</p>
     * <p>Key 生成策略：使用 orderNo 作为幂等 Key</p>
     * <p>超时时间：300 秒（5分钟），在此时间内相同 Key 的请求会被视为重复请求</p>
     * 
     * @param request 订单创建请求
     * @return 订单信息
     */
    @PostMapping
    @Idempotent(
        key = "#request.orderNo",           // 使用订单号作为幂等 Key
        timeout = 300,                      // 超时时间 5 分钟
        failOpen = false                    // 失败时拒绝请求（默认策略）
    )
    public ResponseEntity<OrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        OrderResponse order = orderService.createOrder(request);
        return ResponseEntity.ok(order);
    }

    /**
     * 支付订单 - 幂等接口示例
     * 
     * <p>演示使用多个字段组合生成幂等 Key</p>
     * 
     * @param orderId 订单ID
     * @param paymentRequest 支付请求
     * @return 支付结果
     */
    @PostMapping("/{orderId}/pay")
    @Idempotent(
        key = "#orderId + ':' + #paymentRequest.paymentId",  // 组合 Key
        timeout = 600,                                        // 超时时间 10 分钟
        failOpen = false
    )
    public ResponseEntity<String> payOrder(
            @PathVariable Long orderId,
            @RequestBody PaymentRequest paymentRequest) {
        String result = orderService.payOrder(orderId, paymentRequest);
        return ResponseEntity.ok(result);
    }

    /**
     * 取消订单 - 幂等接口示例
     * 
     * <p>演示使用简单参数作为幂等 Key</p>
     * 
     * @param orderId 订单ID
     * @return 取消结果
     */
    @PostMapping("/{orderId}/cancel")
    @Idempotent(
        key = "#orderId + ':cancel'",
        timeout = 300
    )
    public ResponseEntity<String> cancelOrder(@PathVariable Long orderId) {
        String result = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(result);
    }

    /**
     * 查询订单 - 非幂等接口（GET 请求通常不需要幂等）
     * 
     * @param orderId 订单ID
     * @return 订单信息
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId) {
        OrderResponse order = orderService.getOrder(orderId);
        return ResponseEntity.ok(order);
    }

    /**
     * 支付请求 DTO
     */
    public static class PaymentRequest {
        private String paymentId;
        private Long amount;

        public String getPaymentId() {
            return paymentId;
        }

        public void setPaymentId(String paymentId) {
            this.paymentId = paymentId;
        }

        public Long getAmount() {
            return amount;
        }

        public void setAmount(Long amount) {
            this.amount = amount;
        }
    }
}

