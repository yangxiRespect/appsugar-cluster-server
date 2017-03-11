package org.appsugar.cluster.service.akka.serialization;

import org.nustaq.serialization.FSTConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.serialization.JSerializer;

/**
 * fast serialization 
 * @author NewYoung
 * 2016年5月28日下午7:27:31
 */
public class FstSerialization extends JSerializer {

	private static final Logger logger = LoggerFactory.getLogger(FstSerialization.class);

	public static final int identifier = 10000;

	private FSTConfiguration config = FSTConfiguration.getDefaultConfiguration();

	public FstSerialization() {
		super();
	}

	@Override
	public int identifier() {
		return 10000;
	}

	@Override
	public boolean includeManifest() {
		return false;
	}

	@Override
	public byte[] toBinary(Object msg) {
		return config.asByteArray(msg);
	}

	@Override
	public Object fromBinaryJava(byte[] data, Class<?> clazz) {
		try {
			return config.asObject(data);
		} catch (Exception ex) {
			logger.error("deserializable message error ", ex);
			return ex;
		}
	}

}
