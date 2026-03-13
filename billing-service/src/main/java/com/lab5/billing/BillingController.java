package com.lab5.billing;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/billing")
public class BillingController {
	private static final Logger log = LoggerFactory.getLogger(BillingController.class);
	private final Map<Long, Invoice> invoices = new ConcurrentHashMap<>();
	private final AtomicLong idGenerator = new AtomicLong(3000);
	private final ObjectMapper objectMapper;

	public BillingController(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@GetMapping
	public List<Invoice> getInvoices() {
		return new ArrayList<>(invoices.values());
	}

	@KafkaListener(topics = "order-topic", groupId = "billing-service")
	public void handleOrderCreated(String message) {
		try {
			OrderEvent event = objectMapper.readValue(message, OrderEvent.class);
			long invoiceId = idGenerator.getAndIncrement();
			Invoice invoice = new Invoice(invoiceId, event.orderId(), event.item(), event.quantity(), "GENERATED");
			invoices.put(invoiceId, invoice);
			log.info("Invoice generated for order {} (invoiceId={})", event.orderId(), invoiceId);
		} catch (Exception ex) {
			log.error("Failed to process order event: {}", message, ex);
		}
	}

	public record OrderEvent(String orderId, String item, int quantity) {
	}

	public record Invoice(Long id, String orderId, String item, int quantity, String status) {
	}
}
