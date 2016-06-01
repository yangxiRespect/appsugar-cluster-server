package org.appsugar.cluster.service.akka.serialization;

import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ExtendedActorSystem;
import akka.serialization.JSerializer;
import io.protostuff.CollectionSchema;
import io.protostuff.LinkedBuffer;
import io.protostuff.MapSchema;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.ExplicitIdStrategy;
import io.protostuff.runtime.ExplicitIdStrategy.Registry;
import io.protostuff.runtime.IdStrategy;
import io.protostuff.runtime.RuntimeSchema;

/**
 * 基于protostuff 消息序列化
 * @author NewYoung
 * 2016年5月28日下午8:47:43
 */
public class ProtostuffSerialization extends JSerializer {

	public static final int identifier = 10001;
	protected IdStrategy idStrategy;
	private static final Logger logger = LoggerFactory.getLogger(ProtostuffSerialization.class);
	private static final String mappingKey = "akka.actor.protostuff.mappings";
	private int bufferSize = 4096;

	public ProtostuffSerialization(ExtendedActorSystem system) {
		super();
		logger.debug("dynamic pre defien schema start");
		registerClass(system.settings().config());
		logger.debug("dynamic pre defien schema end");
	}

	@Override
	public int identifier() {
		return 10001;
	}

	@Override
	public boolean includeManifest() {
		//为了网络传输速度
		return false;
	}

	@Override
	public byte[] toBinary(Object msg) {
		//TODO bad performance
		LinkedBuffer buffer = LinkedBuffer.allocate(bufferSize);
		Schema<ProtostuffObjectWrapper> schema = RuntimeSchema.getSchema(ProtostuffObjectWrapper.class, idStrategy);
		try {
			return ProtostuffIOUtil.toByteArray(new ProtostuffObjectWrapper(msg), schema, buffer);
		} finally {
			buffer.clear();
		}
	}

	@Override
	public Object fromBinaryJava(byte[] data, Class<?> clazz) {
		Schema<ProtostuffObjectWrapper> schema = RuntimeSchema.getSchema(ProtostuffObjectWrapper.class, idStrategy);
		ProtostuffObjectWrapper wrapper = schema.newMessage();
		ProtostuffIOUtil.mergeFrom(data, wrapper, schema);
		return wrapper.getObject();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void registerClass(Config config) {
		Registry registry = new ExplicitIdStrategy.Registry();
		idStrategy = registry.strategy;
		Config mappings = config.withFallback(loadProtobufConfig()).getConfig(mappingKey);
		if (mappings == null) {
			throw new RuntimeException("config " + mappingKey + " not found");
		}
		bufferSize = config.getInt("akka.actor.protostuff.buffer-size");
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		mappings.entrySet().stream().forEach(e -> {
			String key = e.getKey();
			int id = mappings.getInt(key);
			try {
				Class<?> clazz = Class.forName(key, false, loader);
				if (clazz.isEnum()) {
					registry.registerEnum((Class<Enum>) clazz, id);
				} else if (Map.class.isAssignableFrom(clazz)) {
					registry.registerMap(MapSchema.MessageFactories.getFactory(key), id);
				} else if (Collection.class.isAssignableFrom(clazz)) {
					registry.registerCollection(CollectionSchema.MessageFactories.getFactory(key), id);
				} else {
					registry.registerPojo(clazz, id);
				}
			} catch (Exception e1) {
				throw new RuntimeException(e1);
			}
		});
	}

	protected Config loadProtobufConfig() {
		return ConfigFactory.load("protostuff.conf");
	}
}
