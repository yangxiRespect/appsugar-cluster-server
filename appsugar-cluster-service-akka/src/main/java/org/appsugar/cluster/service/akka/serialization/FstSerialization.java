package org.appsugar.cluster.service.akka.serialization;

import org.nustaq.serialization.FSTConfiguration;

import akka.serialization.JSerializer;

/**
 * fast serialization 
 * @author NewYoung
 * 2016年5月28日下午7:27:31
 */
public class FstSerialization extends JSerializer {

	public static final int identifier = 10000;

	private FSTConfiguration config = FSTConfiguration.getDefaultConfiguration();

	public FstSerialization() {
		super();
		System.out.println("狗日滴,创建了一个");
	}

	@Override
	public int identifier() {
		return 10000;
	}

	@Override
	public boolean includeManifest() {
		//fst serialization 有自描述类型. 无需includeManifest
		return false;
	}

	@Override
	public byte[] toBinary(Object msg) {
		return config.asByteArray(msg);
	}

	@Override
	public Object fromBinaryJava(byte[] data, Class<?> clazz) {
		return config.asObject(data);
	}

}
