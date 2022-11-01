package com.contact.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.contact.entities.PaymentOrder;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long>{

	public PaymentOrder findByOrderId(String orderId);
}
