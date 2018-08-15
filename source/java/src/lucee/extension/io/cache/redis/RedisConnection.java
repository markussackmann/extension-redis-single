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

			Integer setMaxTotal = caster.toInteger(arguments.getOrDefault("setMaxTotal",128),128);
			Integer setMaxIdle = caster.toInteger(arguments.getOrDefault("setMaxIdle",128),128);
			Integer setMinIdle = caster.toInteger(arguments.getOrDefault("setMinIdle",16),16);
			Integer setMinEvictableIdleTimeMillis = caster.toInteger(arguments.getOrDefault("setMinEvictableIdleTimeMillis",60000),60000);
			Integer setTimeBetweenEvictionRunsMillis = caster.toInteger(arguments.getOrDefault("setTimeBetweenEvictionRunsMillis",30000),30000);
			Integer setNumTestsPerEvictionRun = caster.toInteger(arguments.getOrDefault("setNumTestsPerEvictionRun",3),3);

			Boolean setTestOnBorrow = caster.toBoolean(arguments.getOrDefault("setTestOnBorrow",true),true);
			Boolean setTestOnReturn = caster.toBoolean(arguments.getOrDefault("setTestOnReturn",true),true);
			Boolean setTestWhileIdle = caster.toBoolean(arguments.getOrDefault("setTestWhileIdle",true),true);
			Boolean setBlockWhenExhausted = caster.toBoolean(arguments.getOrDefault("setBlockWhenExhausted",true),true);

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
