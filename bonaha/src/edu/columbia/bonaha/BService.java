package edu.columbia.bonaha;
import com.apple.dnssd.*;

import java.util.*;

/**
 * The BService class allows an application to create, register and update
 * a ZeroConf service record. It also allows the application to set and get
 * metadata records, and listen to events (nodes entering and leaving) corresponding
 * to that service.
 * @author Suman Srinivasan
 *
 */
public class BService implements
	RegisterListener, BrowseListener, ResolveListener {

	static BonAHA bInstance = BonAHA.getInstance();
	static BListener bListener;
	String dnssdName;
	TXTRecord txtRecord;
	HashMap<String,BNode> nodes;
	boolean foundFlag;
	DNSSDRegistration registerRec;
	DNSSDService serviceRec, resolveRec;
	boolean listenSelf = true;

	/**
	 * Register a ZeroConf service, specifying the name of the service and the
	 * packet type. (The mDNS name is generated from this.)
	 * @param name Human-readable name of service offered
	 * @param packetType Type of packet used (TCP, UDP)
	 */
	public BService (String name, String packetType) {
		this ("_" + name + "._" + packetType);
	}

	/**
	 * Register a ZeroConf service, using the raw service type name.
	 * @param dnsName Raw name of service
	 */
	public BService (String dnsName) {
		this.dnssdName = dnsName;
		txtRecord = new TXTRecord();
		nodes = new HashMap<String,BNode>();

        // add shutdown hook
        BServiceShutdownHook shutdownHook = new BServiceShutdownHook();
        Runtime.getRuntime().addShutdownHook(shutdownHook);

		try {
			DNSSD.browse(dnssdName, this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set the metadata for the service
	 * @param key Key for metadata
	 * @param value Value corresponding to key
	 */
	public void set(String key, Object value) {
//		txtRecord.set(key, value.toString());
        TXTRecord txtRec = new TXTRecord();
        txtRec.set(key,value.toString());
        try {
        	registerRec.addRecord(0,16,txtRec.getRawBytes(),0);
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}

	/**
	 * Returns the value of the metadata for corresponding key
	 * @param key
	 * @return Value corresponding to key
	 */
	public String get(String key) {
		return txtRecord.getValueAsString(key);
	}

	/**
	 * Register the service with the default name
	 */
	public void register() {
		register("", 9999);
	}

	/**
	 * Register the service with the name provided
	 * @param name Name of the service
	 */
	public void register(String name) {
		register(name, 9999);
	}

	/**
	 * Register the service with the port.
	 * @param port Port on which service is provided.
	 */
	public void register(int port) {
		register("", port);
	}

	/**
	 * Register the service with the given name and port.
	 * @param name Name of the service.
	 * @param port Port on which service is provided.
	 */
	public void register(String name, int port) {
		try {
			registerRec = DNSSD.register(name, dnssdName,
					port, this);
			// Register the TXT records, if there are any
			if (txtRecord.size() > 0) {
				registerRec.getTXTRecord().update(0, txtRecord.getRawBytes(), 0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Update the service record. This needs to be called, for instance,
	 * if the metadata is updated.
	 */
	public void update() {
		// Register the TXT records, if there are any
		try {
			if (txtRecord.size() > 0)
				registerRec.getTXTRecord().update(0, txtRecord.getRawBytes(), 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Currently not used.
	 * @param listen
	 */
	public void listenToSelf (boolean listen) {
		listenSelf = listen;
	}

	/**
	 * Set the class that listens to events on this service
	 * @param listener
	 */
	public void setListener(BListener listener) {
		bListener = listener;
	}

	/* PRIVATE BONJOUR EVENTS */

	// When a service is found
	public void serviceFound(DNSSDService browser, int flags, int ifIndex,
			String name, String regType, String domain) {
		// TODO: HONORING THE listenToSelf() FUNCTION CALL.
		// We want to eventually replace "true" with a check
		// for whether localhost network address is the same as the
		// address of the node that we just found.
		if (!listenSelf && (true)) {
			return;
		}
		foundFlag = true;
		try {
			resolveRec = DNSSD.resolve(0, DNSSD.ALL_INTERFACES, name,
					dnssdName, domain, this);
		} catch (DNSSDException e) {
			System.err.println("BService :: service: " + dnssdName);
		}
	}

	// When a service is lost or leaves the network
	public void serviceLost(DNSSDService browser, int flags, int ifIndex,
			String name, String regType, String domain) {
		foundFlag = false;
		try {
			resolveRec = DNSSD.resolve(0, DNSSD.ALL_INTERFACES, name,
					dnssdName, domain, this);
		} catch (DNSSDException e) {
			System.err.println("BService :: service: " + dnssdName);
		}
	}

	// When a service is resolved
	public void serviceResolved(DNSSDService resolver, int flags, int ifIndex,
			String fullName, String hostName, int port, TXTRecord txtRecord) {
		BNode n = new BNode(fullName, hostName, port, txtRecord);
		nodes.put(hostName, n);

		if (foundFlag) bListener.serviceUpdated(n);
		else bListener.serviceExited(n);
	}

	// When an operation fails
	public void operationFailed(DNSSDService arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	// When a service is registered
	public void serviceRegistered(DNSSDRegistration arg0, int arg1, String arg2, String arg3, String arg4) {
		// TODO Auto-generated method stub

	}

	// The shutdown hook to stop all Bonjour announcements when this class is
	// terminated.
	private class BServiceShutdownHook extends Thread {
		public void run() {
			if (registerRec != null) registerRec.stop();
			if (serviceRec != null) serviceRec.stop();
			if (resolveRec != null) resolveRec.stop();
		}
	}
}
