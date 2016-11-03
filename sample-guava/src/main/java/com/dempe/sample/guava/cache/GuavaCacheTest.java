package com.dempe.sample.guava.cache;

import com.google.common.base.Optional;
import com.google.common.cache.*;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

/**
 * guava cache 使用示例
 * User: Dempe
 * Date: 2016/11/2
 * Time: 18:25
 * To change this template use File | Settings | File Templates.
 */
public class GuavaCacheTest {
    /**
     * 2. How to Use Guava Cache
     * <p>
     * Let’s start with a simple example – let’s cache the uppercase form of String instances.
     * First, we’ll create the CacheLoader – used to compute the value stored in the cache. From this, we’ll use the handy CacheBuilder to build our cache using the given specifications:
     */
    @Test
    public void whenCacheMiss_thenValueIsComputed() throws InterruptedException {
        CacheLoader<String, String> loader;
        loader = new CacheLoader<String, String>() {
            // 当guava cache中不存在，则会调用load方法
            @Override
            public String load(String key) {
                return key.toUpperCase();
            }
        };

        LoadingCache<String, String> cache;
        cache = CacheBuilder
                .newBuilder()
                // 写数据1s后重新加载缓存
                .refreshAfterWrite(1L, TimeUnit.SECONDS)
                .build(loader);

        assertEquals(0, cache.size());
        cache.put("test", "test");
        assertEquals("test", cache.getUnchecked("test"));
        assertEquals("HELLO", cache.getUnchecked("hello"));
        assertEquals(2, cache.size());

        TimeUnit.SECONDS.sleep(2);
        assertEquals("TEST", cache.getUnchecked("test"));

    }

    /**
     * 3.1. Eviction by Size
     * <p>
     * We can limit the size of our cache using maximumSize(). If the cache reaches the limit, the oldest items will be evicted.
     * In the following code, we limit the cache size to 3 records:
     */
    @Test
    public void whenCacheReachMaxSize_thenEviction() {
        CacheLoader<String, String> loader;
        loader = new CacheLoader<String, String>() {
            @Override
            public String load(String key) {
                return key.toUpperCase();
            }
        };
        LoadingCache<String, String> cache;
        cache = CacheBuilder.newBuilder().maximumSize(3).build(loader);

        cache.getUnchecked("first");
        cache.getUnchecked("second");
        cache.getUnchecked("third");
        cache.getUnchecked("forth");
        assertEquals(3, cache.size());
        assertNull(cache.getIfPresent("first"));
        assertEquals("FORTH", cache.getIfPresent("forth"));
    }

    /**
     * 3.2. Eviction by Weight
     * <p>
     * We can also limit the cache size using a custom weight function. In the following code, we use the length as our custom weight function:
     */
    @Test
    public void whenCacheReachMaxWeight_thenEviction() {
        CacheLoader<String, String> loader;
        loader = new CacheLoader<String, String>() {
            @Override
            public String load(String key) {
                return key.toUpperCase();
            }
        };

        Weigher<String, String> weighByLength;
        weighByLength = new Weigher<String, String>() {
            @Override
            public int weigh(String key, String value) {
                return value.length();
            }
        };

        LoadingCache<String, String> cache;
        cache = CacheBuilder.newBuilder()
                .maximumWeight(16)
                .weigher(weighByLength)
                .build(loader);

        cache.getUnchecked("first");
        cache.getUnchecked("second");
        cache.getUnchecked("third");
        cache.getUnchecked("last");
        assertEquals(3, cache.size());
        assertNull(cache.getIfPresent("first"));
        assertEquals("LAST", cache.getIfPresent("last"));
    }

    /**
     * 3.3. Eviction by Time
     * <p>
     * Beside using size to evict old records, we can use time. In the following example, we customize our cache to remove records that have been idle for 2ms:
     *
     * @throws InterruptedException
     */
    @Test
    public void whenEntryIdle_thenEviction() throws InterruptedException {
        CacheLoader<String, String> loader;
        loader = new CacheLoader<String, String>() {
            @Override
            public String load(String key) {
                return key.toUpperCase();
            }
        };

        LoadingCache<String, String> cache;
        cache = CacheBuilder.newBuilder()
                .expireAfterAccess(2, TimeUnit.MILLISECONDS)
                .build(loader);

        cache.getUnchecked("hello");
        assertEquals(1, cache.size());

        cache.getUnchecked("hello");
        Thread.sleep(300);

        cache.getUnchecked("test");
        assertEquals(1, cache.size());
        assertNull(cache.getIfPresent("hello"));
    }

    /**
     * We can also evict records based on their total live time. In the following example, the cache will remove the records after 2ms of being stored:
     *
     * @throws InterruptedException
     */
    @Test
    public void whenEntryLiveTimeExpire_thenEviction() throws InterruptedException {
        CacheLoader<String, String> loader;
        loader = new CacheLoader<String, String>() {
            @Override
            public String load(String key) {
                return key.toUpperCase();
            }
        };

        LoadingCache<String, String> cache;
        cache = CacheBuilder.newBuilder()
                .expireAfterWrite(2, TimeUnit.MILLISECONDS)
                .build(loader);

        cache.getUnchecked("hello");
        assertEquals(1, cache.size());
        Thread.sleep(300);
        cache.getUnchecked("test");
        assertEquals(1, cache.size());
        assertNull(cache.getIfPresent("hello"));
    }

    /**
     * 4. Weak Keys
     * <p>
     * Next, let’s see how to make our cache keys have weak references – allowing the garbage collector to collect cache key that are not referenced elsewhere.
     * By default, both cache keys and values have strong references but we can make our cache store the keys using weak references using weakKeys() as in the following example:
     */
    @Test
    public void whenWeakKeyHasNoRef_thenRemoveFromCache() {
        CacheLoader<String, String> loader;
        loader = new CacheLoader<String, String>() {
            @Override
            public String load(String key) {
                return key.toUpperCase();
            }
        };

        LoadingCache<String, String> cache;
        cache = CacheBuilder.newBuilder().weakKeys().build(loader);
    }

    /**
     * 5. Soft Values
     * <p>
     * We can allow garbage collector to collect our cached values by using softValues() as in the following example:
     */
    @Test
    public void whenSoftValue_thenRemoveFromCache() {
        CacheLoader<String, String> loader;
        loader = new CacheLoader<String, String>() {
            @Override
            public String load(String key) {
                return key.toUpperCase();
            }
        };

        LoadingCache<String, String> cache;
        cache = CacheBuilder.newBuilder().softValues().build(loader);
    }

    /**
     * 6. Handle null Values
     * <p>
     * Now, let’s see how to handle cache null values. By default, Guava Cache will throw exceptions if you try to load a null value – as it doesn’t make any sense to cache a null.
     * But if null value means something in your code, then you can make good use of the Optional class as in the following example:
     */
    @Test
    public void whenNullValue_thenOptional() {
        CacheLoader<String, Optional<String>> loader;
        loader = new CacheLoader<String, Optional<String>>() {
            @Override
            public Optional<String> load(String key) {
                return Optional.fromNullable(getSuffix(key));
            }
        };

        LoadingCache<String, Optional<String>> cache;
        cache = CacheBuilder.newBuilder().build(loader);

        assertEquals("txt", cache.getUnchecked("text.txt").get());
        assertFalse(cache.getUnchecked("hello").isPresent());
    }

    private String getSuffix(final String str) {
        int lastIndex = str.lastIndexOf('.');
        if (lastIndex == -1) {
            return null;
        }
        return str.substring(lastIndex + 1);
    }

    /**
     * 7. Refresh the Cache
     * <p>
     * Next, let’s see how to refresh our cache values. We can refresh our cache automatically using refreshAfterWrite().
     * In the following example, the cache is refreshed automatically every 1 minute:
     */
    @Test
    public void whenLiveTimeEnd_thenRefresh() {
        CacheLoader<String, String> loader;
        loader = new CacheLoader<String, String>() {
            @Override
            public String load(String key) {
                return key.toUpperCase();
            }
        };

        LoadingCache<String, String> cache;
        cache = CacheBuilder.newBuilder()
                .refreshAfterWrite(1, TimeUnit.MINUTES)
                .build(loader);
    }

    /**
     * 8. Preload the Cache
     * <p>
     * We can insert multiple records in our cache using putAll() method. In the following example, we add multiple records into our cache using a Map:
     */
    @Test
    public void whenPreloadCache_thenUsePutAll() {
        CacheLoader<String, String> loader;
        loader = new CacheLoader<String, String>() {
            @Override
            public String load(String key) {
                return key.toUpperCase();
            }
        };

        LoadingCache<String, String> cache;
        cache = CacheBuilder.newBuilder().build(loader);

        Map<String, String> map = Maps.newHashMap();
        map.put("first", "FIRST");
        map.put("second", "SECOND");
        cache.putAll(map);

        assertEquals(2, cache.size());
    }

    /**
     * 9. RemovalNotification
     * <p>
     * Sometimes, you need to take some actions when a record is removed from the cache; so, let’s discuss RemovalNotification.
     * We can register a RemovalListener to get notifications of a record being removed. We also have access to the cause of the removal – via the getCause() method.
     * In the following sample, a RemovalNotification is received when the forth element in the cache because of its size:
     */
    @Test
    public void whenEntryRemovedFromCache_thenNotify() {
        CacheLoader<String, String> loader;
        loader = new CacheLoader<String, String>() {
            @Override
            public String load(final String key) {
                return key.toUpperCase();
            }
        };

        RemovalListener<String, String> listener;
        listener = new RemovalListener<String, String>() {
            @Override
            public void onRemoval(RemovalNotification<String, String> n) {
                if (n.wasEvicted()) {
                    String cause = n.getCause().name();
                    assertEquals(RemovalCause.SIZE.toString(), cause);
                }
            }
        };

        LoadingCache<String, String> cache;
        cache = CacheBuilder.newBuilder()
                .maximumSize(3)
                .removalListener(listener)
                .build(loader);

        cache.getUnchecked("first");
        cache.getUnchecked("second");
        cache.getUnchecked("third");
        cache.getUnchecked("last");
        assertEquals(3, cache.size());
    }
}
