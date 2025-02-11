package org.scoula.backend.order.service;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.scoula.backend.member.domain.Account;
import org.scoula.backend.member.domain.Member;
import org.scoula.backend.member.domain.MemberStatus;
import org.scoula.backend.order.domain.Order;
import org.scoula.backend.order.domain.OrderStatus;
import org.scoula.backend.order.domain.Type;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MockDataGenerator {
	private static final String[] MAJOR_STOCKS = {
		"005930", // 삼성전자
		"000660", // SK하이닉스
		"035420", // NAVER
		"005490", // POSCO홀딩스
		"035720"  // 카카오
	};

	private static final Map<String, PriceRange> STOCK_PRICE_RANGES = Map.of(
		"005930", new PriceRange(65000, 75000),
		"000660", new PriceRange(110000, 130000),
		"035420", new PriceRange(180000, 200000),
		"005490", new PriceRange(450000, 500000),
		"035720", new PriceRange(45000, 55000)
	);

	@Getter
	@AllArgsConstructor
	private static class PriceRange {
		private final int min;
		private final int max;
	}

	private final Random random = new Random();

	public Member createMockMember() {
		return Member.builder()
			.id(random.nextLong(1, 1000))
			.provider("kakao")
			.email("user" + random.nextInt(1000) + "@example.com")
			.nickname("user" + random.nextInt(1000))
			.status(MemberStatus.ACTIVE)
			.build();
	}

	public Account createMockAccount(Member member) {
		return Account.builder()
			.id(random.nextLong(1, 1000))
			.balance(random.nextLong(10000000, 100000000))
			.member(member)
			.build();
	}

	public Order createMockOrder(Account account) {
		String companyCode = MAJOR_STOCKS[random.nextInt(MAJOR_STOCKS.length)];
		Type orderType = generateOrderType();
		int price = generatePrice(companyCode, orderType);
		int quantity = generateQuantity(price);

		return Order.builder()
			.id(random.nextLong(1, 1000))
			.companyCode(companyCode)
			.type(orderType)
			.totalQuantity(quantity)
			.remainingQuantity(quantity)
			.status(OrderStatus.ACTIVE)
			.price(price)
			.account(account)
			.build();
	}

	private Type generateOrderType() {
		return random.nextBoolean() ? Type.BUY : Type.SELL;
	}

	private int generatePrice(String companyCode, Type orderType) {
		PriceRange range = STOCK_PRICE_RANGES.get(companyCode);
		int basePrice = random.nextInt(range.getMin(), range.getMax());
		return adjustToTickSize(basePrice);
	}

	private int adjustToTickSize(int price) {
		if (price < 1000)
			return roundToNearest(price, 1);
		if (price < 5000)
			return roundToNearest(price, 5);
		if (price < 10000)
			return roundToNearest(price, 10);
		if (price < 50000)
			return roundToNearest(price, 50);
		if (price < 100000)
			return roundToNearest(price, 100);
		if (price < 500000)
			return roundToNearest(price, 500);
		return roundToNearest(price, 1000);
	}

	private int roundToNearest(int price, int tickSize) {
		return (price / tickSize) * tickSize;
	}

	private int generateQuantity(int price) {
		int targetAmount = random.nextInt(10_000_000, 100_000_000);
		int quantity = targetAmount / price;
		return Math.min(Math.max(quantity, 1), 1000);
	}

	// 여러 개의 목 데이터 생성
	public List<Order> createMockOrders(int count) {
		Member member = createMockMember();
		Account account = createMockAccount(member);

		return IntStream.range(0, count)
			.mapToObj(i -> createMockOrder(account))
			.collect(Collectors.toList());
	}

	public Order createMockOrder() {
		Member member = createMockMember();
		Account account = createMockAccount(member);
		Order order = createMockOrder(account);

		return order;
	}
}

