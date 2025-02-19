package org.scoula.backend.order.domain;

import org.scoula.backend.global.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Slf4j
public class TradeHistory extends BaseEntity {

	//TODO : Entitiy에 companycode 추가
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "trade_history_id")
	private Long id;

	@Column(nullable = false)
	private Long sellOrderId;

	@Column(nullable = false)
	private Long buyOrderId;

	@Column(nullable = false)
	private Integer quantity;

	@Column(nullable = false)
	private Integer price;

}
