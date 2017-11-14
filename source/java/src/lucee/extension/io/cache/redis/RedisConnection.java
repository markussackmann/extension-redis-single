package lucee.extension.io.cache.redis;

import java.util.*;

import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.type.Struct;
import lucee.runtime.util.Cast;
import lucee.runtime.exp.PageException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisConnection {

	private static final Hashtable<String, JedisPool> instance = new Hashtable<String, JedisPool>();
	private static final Hashtable<String, String> namespace = new Hashtable<String, String>();

	private RedisConnection() {}

	public static JedisPool init(String cacheName, Struct arguments){

		CFMLEngine engine = CFMLEngineFactory.getInstance();
		Cast caster = engine.getCastUtil();

		if(instance != null && instance.contains(cacheName)){
			return getInstance(cacheName);
		}

		try{
			namespace.put(cacheName, caster.toString(arguments.get("namespace")));

			String hosts = caster.toString(arguments.get("hosts"));
			String host = hosts.split(":")[0];

			Integer port = caster.toInteger(hosts.split(":")[1]);

			Integer setMaxTotal = caster.toInteger(arguments.get("setMaxTotal"),128);
			Integer setMaxIdle = caster.toInteger(arguments.get("namespace"),128);
			Integer setMinIdle = caster.toInteger(arguments.get("namespace"),16);
			Integer setMinEvictableIdleTimeMillis = caster.toInteger(arguments.get("setMinEvictableIdleTimeMillis"),60000);
			Integer setTimeBetweenEvictionRunsMillis = caster.toInteger(arguments.get("setTimeBetweenEvictionRunsMillis"),30000);
			Integer setNumTestsPerEvictionRun = caster.toInteger(arguments.get("setNumTestsPerEvictionRun"),3);

			Boolean setTestOnBorrow = caster.toBoolean(arguments.get("setTestOnBorrow"),true);
			Boolean setTestOnReturn = caster.toBoolean(arguments.get("setTestOnReturn"),true);
			Boolean setTestWhileIdle = caster.toBoolean(arguments.get("setTestWhileIdle"),true);
			Boolean setBlockWhenExhausted = caster.toBoolean(arguments.get("setBlockWhenExhausted"),true);

			final JedisPoolConfig poolConfig = new JedisPoolConfig();
			poolConfig.setMaxTotal(setMaxTotal);
			poolConfig.setMaxIdle(setMaxIdle);
			poolConfig.setMinIdle(setMinIdle);
			poolConfig.setTestOnBorrow(setTestOnBorrow);
			poolConfig.setTestOnReturn(setTestOnReturn);
			poolConfig.setTestWhileIdle(setTestWhileIdle);
			poolConfig.setMinEvictableIdleTimeMillis(setMinEvictableIdleTimeMillis);
			poolConfig.setTimeBetweenEvictionRunsMillis(setTimeBetweenEvictionRunsMillis);
			poolConfig.setNumTestsPerEvictionRun(setNumTestsPerEvictionRun);
			poolConfig.setBlockWhenExhausted(setBlockWhenExhausted);

			instance.put(cacheName, new JedisPool(poolConfig, host, port));

		} catch (PageException e) {
			e.printStackTrace();
		}

		return instance.get(cacheName);
	}

	public static JedisPool getInstance(String cacheName){
		return instance.get(cacheName);
	}

	public static String getNamespace(String cacheName){
		return namespace.get(cacheName);
	}

}
