package com.gamesup.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Purchase {

	public enum OrderStatus {
		BASKET,
		PAID,
		SHIPPED,
		DELIVERED,
		CANCELED
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private OrderStatus status = OrderStatus.BASKET;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column
	private LocalDateTime paidAt;

	@Column(nullable = true)
	private float totalAmount;

	@Column(length = 3)
	private String currency;

	@ManyToOne
	@JoinColumn(nullable = false)
	private User user;

	@OneToMany(mappedBy = "purchase")
	private List<PurchaseLine> purchaseLines;

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
	}
}