package org.scoula.backend.order.service.multiqueue;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import lombok.Data;

@Data
@Component
public class MarketStateCache {

	private final Cache<String, MarketCondition> stateCache;

	public MarketStateCache() {
		this.stateCache = Caffeine.newBuilder()
			// .maximumSize(10000)
			// .expireAfterWrite(60, TimeUnit.MINUTES)
			.recordStats()
			.build();
	}

	public MarketCondition getCondition(String companyCode) {
		return stateCache.getIfPresent(companyCode);
	}

	public void updateCondition(MarketCondition condition) {
		stateCache.put(condition.getCompanyCode(), condition);
	}

	public Map<String, MarketCondition> getAllConditions() {
		return stateCache.asMap();
	}
}
