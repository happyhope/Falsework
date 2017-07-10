package com.sino.scaffold.config.wechat;

import org.nutz.json.Json;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.weixin.at.WxAccessToken;
import org.nutz.weixin.at.WxJsapiTicket;
import org.nutz.weixin.at.impl.CacheableAccessTokenStore;
import org.nutz.weixin.impl.WxApi2Impl;
import org.nutz.weixin.spi.WxJsapiTicketStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author kerbores
 *
 */
@Configuration
@ConditionalOnClass(WxApi2Impl.class)
@EnableConfigurationProperties(WechatConfigProperties.class)
public class WechatAutoConfiguration {
	@Autowired
	private WechatConfigProperties wechatConfigProperties;

	@Autowired
	RedisTemplate<String, String> template;

	Log log = Logs.get();

	@Bean(name = "wxapi")
	public WxApi2Impl api() {
		WxApi2Impl api = new WxApi2Impl(
				wechatConfigProperties.getToken(),
				wechatConfigProperties.getAppid(),
				wechatConfigProperties.getAppsecret(),
				null,
				wechatConfigProperties.getEncodingAesKey());
		if (wechatConfigProperties.isRedisCacheEnable()) {
			api.setAccessTokenStore(new AccessTokenStore(template));
			api.setJsapiTicketStore(new JsapiTicketStore(template));
		}
		return api;
	}

	/**
	 * jstoken存储
	 * 
	 * @author kerbores
	 *
	 */
	public static class JsapiTicketStore implements WxJsapiTicketStore {
		Log log = Logs.get();

		private String tokenKey = "WX_JS_TOKEN";

		private RedisTemplate<String, String> template;

		/**
		 * @param template
		 */
		public JsapiTicketStore(RedisTemplate<String, String> template) {
			super();
			this.template = template;
		}

		/**
		 * @param tokenKey
		 * @param template
		 */
		public JsapiTicketStore(String tokenKey, RedisTemplate<String, String> template) {
			super();
			this.tokenKey = tokenKey;
			this.template = template;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.nutz.weixin.spi.WxJsapiTicketStore#get()
		 */
		@Override
		public WxJsapiTicket get() {
			log.debug("load access ticket...");
			return Json.fromJson(WxJsapiTicket.class, template.opsForValue().get(tokenKey));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.nutz.weixin.spi.WxJsapiTicketStore#save(java.lang.String, int, long)
		 */
		@Override
		public void save(String ticket, int expires, long lastCacheTimeMillis) {
			log.debugf("cache javascript ticket: %s", ticket);
			template.opsForValue().set(tokenKey, Json.toJson(new WxJsapiTicket(ticket, expires, lastCacheTimeMillis)));
		}

	}

	/**
	 * token存储
	 * 
	 * @author kerbores
	 *
	 */
	public static class AccessTokenStore extends CacheableAccessTokenStore {

		private String tokenKey = "WX_AT_TOKEN";

		private RedisTemplate<String, String> template;
		Log log = Logs.get();

		/**
		 * @param template
		 */
		public AccessTokenStore(RedisTemplate<String, String> template) {
			super();
			this.template = template;
		}

		/**
		 * @param tokenKey
		 * @param template
		 */
		public AccessTokenStore(String tokenKey, RedisTemplate<String, String> template) {
			super();
			this.tokenKey = tokenKey;
			this.template = template;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.nutz.weixin.at.impl.CacheableAccessTokenStore#_getAccessToken()
		 */
		@Override
		protected WxAccessToken _getAccessToken() {
			log.debug("load access token...");
			return Json.fromJson(WxAccessToken.class, template.opsForValue().get(tokenKey));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.nutz.weixin.at.impl.CacheableAccessTokenStore#_saveAccessToken(java.lang.
		 * String, int)
		 */
		@Override
		protected void _saveAccessToken(String token, int time) {
			log.debugf("cache access token: %s", token);
			template.opsForValue().set(tokenKey, Json.toJson(new WxAccessToken(token, time, System.currentTimeMillis())));
		}

	}

}
