package com.tiny.idempotent.example.http.service;

import com.tiny.idempotent.example.http.controller.OrderController;
import com.tiny.idempotent.example.http.dto.CreateOrderRequest;
import com.tiny.idempotent.example.http.dto.OrderResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 订单服务示例
 * 
 * <p>演示业务服务的实现，配合 @Idempotent 注解使用</p>
 * 
 * @author Auto Generated
 * @since 1.0.0
 */
@Slf4j
@Service
public class OrderService {

    // 模拟数据存储（实际应该使用数据库）
    private final ConcurrentHashMap<Long, OrderResponse> orderStore = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> orderNoToIdMap = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * 创建订单
     * 
     * <p>注意：此方法会被 @Idempotent 注解保护，重复请求会返回相同的订单</p>
     * 
     * @param request 订单创建请求
     * @return 订单信息
     */
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("创建订单开始: orderNo={}, userId={}, amount={}", 
                request.getOrderNo(), request.getUserId(), request.getAmount());
        
        // 检查订单号是否已存在（模拟业务逻辑）
        Long existingOrderId = orderNoToIdMap.get(request.getOrderNo());
        if (existingOrderId != null) {
            log.info("订单已存在，返回已有订单: orderNo={}, orderId={}", 
                    request.getOrderNo(), existingOrderId);
            return orderStore.get(existingOrderId);
        }
        
        // 创建新订单
        Long orderId = idGenerator.getAndIncrement();
        OrderResponse order = OrderResponse.builder()
                .orderId(orderId)
                .orderNo(request.getOrderNo())
                .userId(request.getUserId())
                .status("CREATED")
                .amount(request.getAmount())
                .createTime(LocalDateTime.now())
                .build();
        
        // 保存订单（模拟数据库保存）
        orderStore.put(orderId, order);
        orderNoToIdMap.put(request.getOrderNo(), orderId);
        
        log.info("订单创建成功: orderId={}, orderNo={}", orderId, request.getOrderNo());
        
        return order;
    }

    /**
     * 支付订单
     * 
     * @param orderId 订单ID
     * @param paymentRequest 支付请求
     * @return 支付结果
     */
    public String payOrder(Long orderId, OrderController.PaymentRequest paymentRequest) {
        log.info("支付订单开始: orderId={}, paymentId={}, amount={}", 
                orderId, paymentRequest.getPaymentId(), paymentRequest.getAmount());
        
        OrderResponse order = orderStore.get(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在: " + orderId);
        }
        
        if (!"CREATED".equals(order.getStatus())) {
            throw new IllegalStateException("订单状态不允许支付: " + order.getStatus());
        }
        
        // 模拟支付处理
        order.setStatus("PAID");
        orderStore.put(orderId, order);
        
        log.info("订单支付成功: orderId={}, paymentId={}", orderId, paymentRequest.getPaymentId());
        
        return "支付成功: " + paymentRequest.getPaymentId();
    }

    /**
     * 取消订单
     * 
     * @param orderId 订单ID
     * @return 取消结果
     */
    public String cancelOrder(Long orderId) {
        log.info("取消订单开始: orderId={}", orderId);
        
        OrderResponse order = orderStore.get(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在: " + orderId);
        }
        
        if ("PAID".equals(order.getStatus())) {
            throw new IllegalStateException("已支付订单不能取消");
        }
        
        // 模拟取消处理
        order.setStatus("CANCELLED");
        orderStore.put(orderId, order);
        
        log.info("订单取消成功: orderId={}", orderId);
        
        return "订单已取消: " + orderId;
    }

    /**
     * 查询订单
     * 
     * @param orderId 订单ID
     * @return 订单信息
     */
    public OrderResponse getOrder(Long orderId) {
        OrderResponse order = orderStore.get(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在: " + orderId);
        }
        return order;
    }
}

