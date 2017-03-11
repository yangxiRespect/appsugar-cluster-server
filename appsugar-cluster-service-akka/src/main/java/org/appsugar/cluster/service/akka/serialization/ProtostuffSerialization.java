package org.appsugar.cluster.service.akka.serialization;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ExtendedActorSystem;
import akka.serialization.JSerializer;
import io.protostuff.CollectionSchema;
import io.protostuff.GraphIOUtil;
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
	private static final String protostuffPath = "akka.actor.protostuff";
	private static final String mappingKey = protostuffPath + ".mappings";
	private static final String buffSizeKey = protostuffPath + ".buffer-size";
	private static final String serializationType = protostuffPath + ".type";

	private int bufferSize = 4096;
	private String type = "graph";
	private ProtostuffSerializer serializer;

	public ProtostuffSerialization(ExtendedActorSystem system) {
		super();
		Config config = system.settings().config();
		logger.debug("dynamic pre defien schema start");
		registerClass(config);
		logger.debug("dynamic pre defien schema end");
		initConfig(config);
	}

	@Override
	public int identifier() {
		return 10001;
	}

	@Override
	public boolean includeManifest() {
		return false;
	}

	@Override
	public byte[] toBinary(Object msg) {
		return serializer.toBinary(msg);
	}

	@Override
	public Object fromBinaryJava(byte[] data, Class<?> clazz) {
		return serializer.fromBinary(data);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void registerClass(Config config) {
		Registry registry = new ExplicitIdStrategy.Registry();
		idStrategy = registry.strategy;
		Config mappings = config.withFallback(ConfigFactory.parseResources("protostuff.conf")).getConfig(mappingKey);
		if (mappings == null) {
			throw new RuntimeException("config " + mappingKey + " not found");
		}
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

	protected void initConfig(Config config) {
		if (config.hasPath(buffSizeKey)) {
			bufferSize = config.getInt(buffSizeKey);
		}
		if (config.hasPath(serializationType)) {
			type = config.getString(serializationType);
		}
		Schema<ProtostuffObjectWrapper> schema = RuntimeSchema.getSchema(ProtostuffObjectWrapper.class, idStrategy);
		if ("graph".equals(type)) {
			serializer = new GraphProtostuffSerializer(bufferSize, schema);
		} else {
			serializer = new DefautlProtostuffSerializer(bufferSize, schema);
		}
	}

	static abstract class ProtostuffSerializer {
		protected Schema<ProtostuffObjectWrapper> schema;
		protected int bufferSize;
		protected ThreadLocal<LinkedBuffer> localBuffer = new ThreadLocal<>();

		public ProtostuffSerializer(int bufferSize, Schema<ProtostuffObjectWrapper> schema) {
			super();
			this.schema = schema;
			this.bufferSize = bufferSize;
		}

		public abstract byte[] toBinary(Object msg);

		public abstract Object fromBinary(byte[] data);

		protected LinkedBuffer buffer() {
			LinkedBuffer buffer = localBuffer.get();
			if (Objects.isNull(buffer)) {
				buffer = LinkedBuffer.allocate(bufferSize);
				localBuffer.set(buffer);
			}
			return buffer;
		}
	}

	static class DefautlProtostuffSerializer extends ProtostuffSerializer {

		public DefautlProtostuffSerializer(int buffer, Schema<ProtostuffObjectWrapper> schema) {
			super(buffer, schema);
		}

		@Override
		public byte[] toBinary(Object msg) {
			LinkedBuffer buffer = buffer();
			try {
				return ProtostuffIOUtil.toByteArray(new ProtostuffObjectWrapper(msg), schema, buffer);
			} finally {
				buffer.clear();
			}
		}

		@Override
		public Object fromBinary(byte[] data) {
			ProtostuffObjectWrapper wrapper = schema.newMessage();
			ProtostuffIOUtil.mergeFrom(data, wrapper, schema);
			try {
				return wrapper.getObject();
			} catch (Exception ex) {
				logger.error("deserializable message error ", ex);
				return ex;
			}

		}

	}

	static class GraphProtostuffSerializer extends ProtostuffSerializer {

		public GraphProtostuffSerializer(int buffer, Schema<ProtostuffObjectWrapper> schema) {
			super(buffer, schema);
		}

		@Override
		public byte[] toBinary(Object msg) {
			LinkedBuffer buffer = buffer();
			try {
				return GraphIOUtil.toByteArray(new ProtostuffObjectWrapper(msg), schema, buffer);
			} finally {
				buffer.clear();
			}
		}

		@Override
		public Object fromBinary(byte[] data) {
			ProtostuffObjectWrapper wrapper = schema.newMessage();
			GraphIOUtil.mergeFrom(data, wrapper, schema);
			try {
				return wrapper.getObject();
			} catch (Exception ex) {
				logger.error("deserializable message error ", ex);
				return ex;
			}
		}

	}
}
