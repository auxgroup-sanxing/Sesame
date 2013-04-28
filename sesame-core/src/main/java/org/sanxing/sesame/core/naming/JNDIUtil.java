package org.sanxing.sesame.core.naming;

import org.sanxing.sesame.core.Detector;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class JNDIUtil {
	private static InitialContext ic;

	public static InitialContext getInitialContext() {
		if (ic == null) {
			try {
				if ((!(Detector.isInContainer().booleanValue()))
						&& (((System.getProperty("java.naming.factory.initial") == null) || (System
								.getProperty("org.osjava.sj.jndi.shared") == null)))) {
					System.setProperty("java.naming.factory.initial",
							"org.osjava.sj.memory.MemoryContextFactory");
					System.setProperty("org.osjava.sj.jndi.shared", "true");
				}

				ic = new InitialContext();
			} catch (NamingException e) {
				throw new RuntimeException("can not init naming context", e);
			}
		}
		return ic;
	}
}