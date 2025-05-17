package io.hhplus.concert.domain.support;

public interface CacheCleaner {
	void cleanByPrefix(String prefix);
	void cleanAll();
}
