package com.smart.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smart.entities.PaymentOrder;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long>{

	public PaymentOrder findByOrderId(String orderId);
}
