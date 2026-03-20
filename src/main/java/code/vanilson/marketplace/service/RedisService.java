package code.vanilson.marketplace.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    private static final Logger logger = LogManager.getLogger(RedisService.class);
    private static final String PRODUCT_VIEW_COUNT_KEY = "product:views";
    private static final String PRODUCT_RATING_KEY = "product:ratings";
    private static final String SEARCH_INDEX_KEY = "search:index";
    private static final String CART_CACHE_KEY = "cart:cache";
    private static final String PRODUCT_CACHE_KEY = "product:cache";

    private final RedisTemplate<String, String> redisTemplate;

    public RedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void incrementProductViewCount(Long productId) {
        String key = PRODUCT_VIEW_COUNT_KEY + ":" + productId;
        redisTemplate.opsForValue().increment(key);
        logger.info("Incremented view count for product: {}", productId);
    }

    public Long getProductViewCount(Long productId) {
        String key = PRODUCT_VIEW_COUNT_KEY + ":" + productId;
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value) : 0L;
    }

    public void addProductRating(Long productId, Double rating) {
        String key = PRODUCT_RATING_KEY + ":" + productId;
        redisTemplate.opsForZSet().add(key, "user", rating);
        logger.info("Added rating for product: {}", productId);
    }

    public Double getProductAverageRating(Long productId) {
        String key = PRODUCT_RATING_KEY + ":" + productId;
        return redisTemplate.opsForZSet().score(key, "user");
    }

    public void indexProductForSearch(Long productId, String name, String category) {
        String key = SEARCH_INDEX_KEY;
        redisTemplate.opsForZSet().add(key, productId.toString(), 0);
        redisTemplate.opsForHash().put("search:product:" + productId, "name", name);
        redisTemplate.opsForHash().put("search:product:" + productId, "category", category);
        logger.info("Indexed product for search: {}", productId);
    }

    public Set<String> searchProducts(String query) {
        String key = SEARCH_INDEX_KEY;
        return redisTemplate.opsForZSet().range(key, 0, -1);
    }

    public void cacheProduct(Long productId, String productJson, long ttlMinutes) {
        String key = PRODUCT_CACHE_KEY + ":" + productId;
        redisTemplate.opsForValue().set(key, productJson, ttlMinutes, TimeUnit.MINUTES);
        logger.info("Cached product: {}", productId);
    }

    public String getCachedProduct(Long productId) {
        String key = PRODUCT_CACHE_KEY + ":" + productId;
        return redisTemplate.opsForValue().get(key);
    }

    public void invalidateProductCache(Long productId) {
        String key = PRODUCT_CACHE_KEY + ":" + productId;
        redisTemplate.delete(key);
        logger.info("Invalidated cache for product: {}", productId);
    }

    public void cacheCart(Long customerId, String cartJson, long ttlMinutes) {
        String key = CART_CACHE_KEY + ":" + customerId;
        redisTemplate.opsForValue().set(key, cartJson, ttlMinutes, TimeUnit.MINUTES);
        logger.info("Cached cart for customer: {}", customerId);
    }

    public String getCachedCart(Long customerId) {
        String key = CART_CACHE_KEY + ":" + customerId;
        return redisTemplate.opsForValue().get(key);
    }

    public void invalidateCartCache(Long customerId) {
        String key = CART_CACHE_KEY + ":" + customerId;
        redisTemplate.delete(key);
        logger.info("Invalidated cart cache for customer: {}", customerId);
    }

    public void trackProductSearch(String query, Long customerId) {
        String key = "search:history:" + customerId;
        redisTemplate.opsForList().leftPush(key, query);
        redisTemplate.opsForList().trim(key, 0, 9);
        logger.info("Tracked search query: {} for customer: {}", query, customerId);
    }

    public java.util.List<String> getSearchHistory(Long customerId) {
        String key = "search:history:" + customerId;
        return redisTemplate.opsForList().range(key, 0, 9);
    }

    public void addToRecentlyViewed(Long customerId, Long productId) {
        String key = "recently_viewed:" + customerId;
        redisTemplate.opsForList().remove(key, 0, productId.toString());
        redisTemplate.opsForList().leftPush(key, productId.toString());
        redisTemplate.opsForList().trim(key, 0, 19);
    }

    public java.util.List<Long> getRecentlyViewed(Long customerId) {
        String key = "recently_viewed:" + customerId;
        return redisTemplate.opsForList().range(key, 0, 19).stream()
                .map(Long::parseLong)
                .collect(java.util.stream.Collectors.toList());
    }
}