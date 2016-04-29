package edu.columbia.bonaha;

public class BonAHA {

	public BService getService(String dnsName) {
		BService service = new BService(dnsName);
		return service;
	}

	public BService getService(String serviceName, String packetType) {
		BService service = new BService("_" + serviceName +
				"._" + packetType);
		return service;
	}

	private BonAHA() {}

	/**
	 * BonAHAHolder is loaded on the first execution of BonAHA.getInstance()
	 * or the first access to BonAHAHolder.INSTANCE, not before.
	 */
	private static class BonAHAHolder
	{
		private final static BonAHA INSTANCE = new BonAHA();
	}

	public static BonAHA getInstance()
	{
		return BonAHAHolder.INSTANCE;
	}
}
