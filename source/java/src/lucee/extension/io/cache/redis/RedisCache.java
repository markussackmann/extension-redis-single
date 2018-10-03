package lucee.extension.io.cache.redis;

import lucee.commons.io.cache.Cache;
import lucee.commons.io.cache.CacheEntry;
import lucee.commons.io.cache.CacheEntryFilter;
import lucee.commons.io.cache.CacheKeyFilter;
import lucee.extension.util.Functions;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.config.Config;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Struct;
import lucee.runtime.util.Cast;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class RedisCache implements Cache {

	public Functions func = new Functions();
	CFMLEngine engine = CFMLEngineFactory.getInstance();
	Cast caster = engine.getCastUtil();
	//String namespace;
	private String cacheName;


	public void init(String cacheName, Struct arguments) throws IOException {
		this.cacheName = cacheName;
		RedisConnection.init(cacheName, arguments);
	}

	public void init(Config config, String[] cacheName, Struct[] arguments) {
		//Not used at the moment
	}

	public void init(Config config, String cacheName, Struct arguments) {
		try {
			init(cacheName, arguments);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public CacheEntry getCacheEntry(String key) throws IOException {

		JedisPool pool = RedisConnection.getInstance(cacheName);
		Jedis conn = null;
		RedisCacheItem item;

		try {
			conn = pool.getResource();
			String k = RedisCacheUtils.formatKey(cacheName, key);
			List<String> val = conn.hmget(k, "value", "hitCount");
			if (val.get(0) == null) {
				throw (new IOException("Cache key [" + k + "] does not exists"));
			}
			Integer count = caster.toInteger(conn.hincrBy(k, "hitCount", 1));
			item = new RedisCacheItem(k, val.get(0), count, cacheName);

			return new RedisCacheEntry(item);
		} catch (JedisConnectionException e) {
			e.printStackTrace();
			if (null != conn) {
				pool.returnBrokenResource(conn);
				conn = null;
			}
		} catch (PageException e) {
			e.printStackTrace();
		} finally {
			if (null != conn)
				pool.returnResource(conn);
		}
		return null;
	}

	public Object getValue(String key) throws IOException {
		try {
			return getCacheEntry(key).getValue();
		} catch (IOException e) {
			return null;
		}
	}

	public CacheEntry getCacheEntry(String key, CacheEntry cacheEntry) {
		try {
			return getCacheEntry(key);
		} catch (IOException e) {
			return cacheEntry;
		}
	}

	public Object getValue(String key, Object o) {
		try {
			return getValue(key);
		} catch (Exception e) {
			return o;
		}
	}

	public void put(String key, Object val, Long idle, Long expire) {
		JedisPool pool = RedisConnection.getInstance(cacheName);
		Jedis conn = null;
		try {
			conn = pool.getResource();
			Integer exp = 0;

			String k = RedisCacheUtils.formatKey(cacheName, key);
			if (expire != null) {
				exp = caster.toInteger(expire / 1000);
			// If expire==null AND this is for SESSION scope storage AND idle is not null, use idle as the expire value
			} else if (k.contains(":lucee-storage:session:") && idle != null) {
				exp = caster.toInteger(idle / 1000);
			}
			String value = func.serialize(val);

			HashMap<String, String> fields = new HashMap<String, String>();
			fields.put("value", value);
			fields.put("hitCount", "0");
			conn.hmset(k, fields);

			if (exp > 0) {
				conn.expire(k, exp);
			}
		} catch (JedisConnectionException e) {
			e.printStackTrace();
			if (null != conn) {
				pool.returnBrokenResource(conn);
				conn = null;
			}
		} catch (PageException e) {
			e.printStackTrace();
		} finally {
			if (null != conn)
				pool.returnResource(conn);
		}
	}

	public boolean contains(String key) {
		JedisPool pool = RedisConnection.getInstance(cacheName);
		Jedis conn = null;
		try {
			conn = pool.getResource();
			return conn.exists(RedisCacheUtils.formatKey(cacheName, key));
		} catch (JedisConnectionException e) {
			e.printStackTrace();
			if (null != conn) {
				pool.returnBrokenResource(conn);
			}
			return false;
		} finally {
			pool.returnResource(conn);
		}
	}

	public boolean remove(String key) {
		JedisPool pool = RedisConnection.getInstance(cacheName);
		Jedis conn = null;
		try {
			conn = pool.getResource();
			Long res = conn.del(RedisCacheUtils.formatKey(cacheName, key));
			return res == 1;
		} catch (JedisConnectionException e) {
			e.printStackTrace();
			if (null != conn) {
				pool.returnBrokenResource(conn);
			}
			return false;
		} finally {
			pool.returnResource(conn);
		}
	}

	public int remove(CacheKeyFilter cacheKeyFilter) {
		int removed = 0;
		List keys = keys(cacheKeyFilter);
		Iterator<String> it = keys.iterator();

		while (it.hasNext()) {
			boolean res = remove(it.next());
			if (res) {
				removed++;
			}
		}
		return removed;
	}

	public int remove(CacheEntryFilter cacheEntryFilter) {
		return 0;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public List keys() {
		JedisPool pool = RedisConnection.getInstance(cacheName);
		Jedis conn = null;
		ArrayList res = null;

		try {
			conn = pool.getResource();
			res = new ArrayList(conn.keys(RedisConnection.getNamespace(cacheName) + '*'));
		} catch (JedisConnectionException e) {
			e.printStackTrace();
			if (null != conn) {
				pool.returnBrokenResource(conn);
			}
		} finally {
			pool.returnResource(conn);
		}

		return sanitizeKeys(res);
	}

	public List keys(CacheKeyFilter cacheKeyFilter) {
		List keys = keys();
		ArrayList res = new ArrayList();
		Iterator<String> it = keys.iterator();
		CacheKeyFilter filter = null;

		while (it.hasNext()) {
			String key = it.next();
			if (cacheKeyFilter.accept(key)) {
				res.add(key);
			}
		}

		return res;
	}

	public List keys(CacheEntryFilter cacheEntryFilter) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public List values() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public List values(CacheKeyFilter cacheKeyFilter) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public List values(CacheEntryFilter cacheEntryFilter) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public List entries() {
		return entriesList(keys());
	}

	public List entries(CacheKeyFilter cacheKeyFilter) {
		return entriesList(keys(cacheKeyFilter));
	}

	public List entries(CacheEntryFilter cacheEntryFilter) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public long hitCount() {
		return 0;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public long missCount() {
		return 0;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public Struct getCustomInfo() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	private List entriesList(List keys) {
		JedisPool pool = RedisConnection.getInstance(cacheName);
		Jedis conn = null;
		ArrayList<RedisCacheEntry> res = null;

		try {
			conn = pool.getResource();
			res = new ArrayList<RedisCacheEntry>();
			Iterator<String> it = keys.iterator();
			while (it.hasNext()) {
				String k = it.next();
				res.add(new RedisCacheEntry(new RedisCacheItem(k, conn.get(k), cacheName)));
			}

		} catch (JedisConnectionException e) {
			e.printStackTrace();
			if (null != conn) {
				pool.returnBrokenResource(conn);
			}
		} finally {
			pool.returnResource(conn);
		}
		return res;
	}

	private List sanitizeKeys(List keys) {
		for (int i = 0; i < keys.size(); i++) {
			try {
				keys.set(i, RedisCacheUtils.removeNamespace(cacheName, caster.toString(keys.get(i))));
			} catch (PageException e) {
				e.printStackTrace();
			}
		}
		return keys;
	}

}
