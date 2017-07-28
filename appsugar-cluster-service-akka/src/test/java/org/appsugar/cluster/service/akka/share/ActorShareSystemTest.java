package org.appsugar.cluster.service.akka.share;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import junit.framework.TestCase;

public class ActorShareSystemTest extends TestCase {

	private static final Logger logger = LoggerFactory.getLogger(ActorShareSystemTest.class);

	@Test
	public void testOneSystem() throws Exception {
		ActorSystem system = ActorSystem.create("singleSystem");
		ActorShareSystem shareSystem = ActorShareSystem.getSystem(system, (x, y) -> {
		}, (x, y) -> {
		});
		ActorRef ref = system.actorOf(Props.create(TestActor.class), "xx");
		System.out.println(ref.path().toStringWithoutAddress());
		CompletableFuture<Void> result = shareSystem.share(ref, "xx");
		result.get();
		system.terminate();
	}

	@Test
	public void testOnMultipleSystem() throws Exception {
		int[] prots = { 2551, 0, 0 };
		String systemName = "ClusterSystem";
		Set<ActorSystem> systemSets = new HashSet<>();
		for (int port : prots) {
			Config config = ConfigFactory.parseString("akka.remote.artery.canonical.port=" + port)
					.withFallback(ConfigFactory.load());
			ActorSystem system = ActorSystem.create(systemName, config);
			ActorShareSystem shareSystem = ActorShareSystem.getSystem(system, (x, y) -> {
			}, (x, y) -> {
			});
			ActorRef ref = system.actorOf(Props.create(TestActor.class), "xx");
			ActorRef ref1 = system.actorOf(Props.create(TestActor.class), "xx1");
			shareSystem.share(ref, "bbq").get();
			shareSystem.share(ref1, "bbq").get();
			System.out.println("===================share===================");
			systemSets.add(system);
		}
		Thread.sleep(6000);
		systemSets.stream().forEach(s -> s.terminate());
	}

}
