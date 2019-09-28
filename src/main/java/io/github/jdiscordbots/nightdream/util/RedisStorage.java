package io.github.jdiscordbots.nightdream.util;

import net.dv8tion.jda.api.entities.Guild;
import redis.clients.jedis.Jedis;

public class RedisStorage implements Storage{

	private final Jedis jedis;
	
	public RedisStorage() {
		jedis = new Jedis();
		init();
	}
	public RedisStorage(String url) {
		jedis = new Jedis(url);
		init();
	}

	private void init() {
		String pass=BotData.getDatabasePassword();
		if(pass!=null&&!"".equals(pass)) {
			jedis.auth(pass);
		}
	}
	
	private String read(String key,String defaultValue) {
		String value=jedis.get(key);
		if(value==null) {
			value=defaultValue;
		}
		return value;
	}
	private String getKey(String... keyParams) {
		return String.join(".", keyParams);
	}
	@Override
	public String read(String unit, String subUnit, String key, String defaultValue, String... defaultSubUnits) {
		return read(getKey(unit,subUnit,key),defaultValue);
	}

	@Override
	public String read(String unit, String key, String defaultValue) {
		return read(getKey(unit,key),defaultValue);
	}

	private void write(String key,String value) {
		jedis.set(key, value);
	}
	@Override
	public void write(String unit, String subUnit, String key, String value, String... defaultSubUnits) {
		write(getKey(unit,subUnit,key),value);
	}

	@Override
	public void write(String unit, String key, String value) {
		write(getKey(unit,key),value);
	}

	private void remove(String key) {
		jedis.del(key);
	}
	
	@Override
	public void remove(String unit, String subUnit, String key) {
		remove(getKey(unit,subUnit,key));
	}

	@Override
	public void remove(String unit, String key) {
		remove(getKey(unit,key));
	}

	@Override
	public String getGuildDefault(String key) {
		return read("guild_default", key, BotData.GUILD_DEFAULTS.get(key));
	}

	@Override
	public void setGuildDefault(String key, String value) {
		write("guild_default", key, value);
	}

	@Override
	public String getForGuild(Guild guild, String key) {
		return read("guild_" + guild.getId(), key, getGuildDefault(key));
	}

	@Override
	public void setForGuild(Guild guild, String key, String value) {
		write("guild_" + guild.getId(), key, value);
	}

}
