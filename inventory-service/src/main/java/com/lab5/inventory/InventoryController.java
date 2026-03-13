package com.lab5.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory")
public class InventoryController {
	private static final Logger log = LoggerFactory.getLogger(InventoryController.class);
	private final Map<String, Integer> stock = new ConcurrentHashMap<>();
	private final ObjectMapper objectMapper;

	public InventoryController(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		stock.put("Laptop", 10);
		stock.put("Notebook", 50);
	}

	@GetMapping
	public Map<String, Integer> getInventory() {
		return stock;
	}

	@KafkaListener(topics = "order-topic", groupId = "inventory-service")
	public void handleOrderCreated(String message) {
		try {
			OrderEvent event = objectMapper.readValue(message, OrderEvent.class);
			stock.compute(event.item(), (key, value) -> {
				int current = value == null ? 0 : value;
				return Math.max(current - event.quantity(), 0);
			});
			log.info("Inventory updated for order {}: item={}, quantity={}", event.orderId(), event.item(), event.quantity());
		} catch (Exception ex) {
			log.error("Failed to process order event: {}", message, ex);
		}
	}

	public record OrderEvent(String orderId, String item, int quantity) {
	}
}
