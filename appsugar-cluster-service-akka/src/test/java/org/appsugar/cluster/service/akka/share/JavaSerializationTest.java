package org.appsugar.cluster.service.akka.share;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.junit.Test;
import org.nustaq.serialization.FSTConfiguration;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.IdStrategy;
import io.protostuff.runtime.IncrementalIdStrategy;
import io.protostuff.runtime.RuntimeSchema;
import junit.framework.TestCase;

public class JavaSerializationTest extends TestCase implements Serializable {
	public static final String dataFile = "c:/work/a.dat";
	private String name = "1";
	private double b = 2;
	private long c = 3;

	private long dd = 3;

	private TestObject1 tt = new TestObject1("tt");
	private TestObject t = new TestObject("xx");

	@Test
	public void testJavaDefaultSerialization() throws Exception {
		JavaSerializationTest test = new JavaSerializationTest();
		ByteArrayOutputStream a = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(a);
		long s = System.currentTimeMillis();
		for (int i = 0; i < 1000000; i++) {
			out.writeObject(test);
			out.reset();
		}
		System.out.println(System.currentTimeMillis() - s);
	}

	@Test
	public void testFastSerialization() throws Exception {
		JavaSerializationTest test = new JavaSerializationTest();
		FSTConfiguration config = FSTConfiguration.getDefaultConfiguration();
		long s = System.currentTimeMillis();
		for (int i = 0; i < 10000000; i++) {
			byte[] data = config.asByteArray(test);
		}
		System.out.println(System.currentTimeMillis() - s);
	}

	@Test
	public void testProtostuffSerialization() throws Exception {
		JavaSerializationTest test = new JavaSerializationTest();
		IdStrategy strategy = new IncrementalIdStrategy.Factory().create();
		Schema<JavaSerializationTest> schema = RuntimeSchema.getSchema(JavaSerializationTest.class);
		LinkedBuffer buffer = LinkedBuffer.allocate(4096);
		long s = System.currentTimeMillis();
		for (int i = 0; i < 10000000; i++) {
			buffer.clear();
			byte[] data = ProtostuffIOUtil.toByteArray(test, schema, buffer);
		}
		System.out.println(System.currentTimeMillis() - s);
	}

}
