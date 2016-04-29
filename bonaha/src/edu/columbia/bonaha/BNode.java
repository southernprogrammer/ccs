package edu.columbia.bonaha;

import com.apple.dnssd.*;
import java.net.*;
import java.util.*;

/**
 * A class corresponding to a node that provides the service that it is
 * registered for. This does not relate to a physical node which may provide more
 * than one service, rather, a BNode always corresponds to an entity that provides
 * only the service for which information is requested.
 * @author Suman Srinivasan
 *
 */
public class BNode {
	String ipAddr;
	TXTRecord txtRecord;
	String fullName;
	String hostName, ipAddress;
	int port;
	Vector<String> txtKeys, txtValues;

	/**
	 *
	 * @param fullName	The full service name of the node
	 * @param hostName	The host name of the node
	 * @param port		The primary port that the node is offering service on
	 * @param txtRecord	TXTRecords for the node
	 */
	public BNode(String fullName, String hostName, int port, TXTRecord txtRecord) {
		txtKeys = new Vector<String>();
		txtValues = new Vector<String>();
		this.fullName = fullName;
		this.hostName = hostName;
		this.port = port;
		this.txtRecord = txtRecord;
		_localTxtRec(txtRecord);
		try {
			ipAddress = InetAddress.getByName(hostName).getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Return all the keys associated with this node.
	 * @return String array containing the keys
	 */
	public String[] getKeys() {
		String[] keys = new String[txtKeys.size()];
		txtKeys.toArray(keys);
		return keys;
	}

	/**
	 * Return all the values associated with this node.
	 * @return String array containing all the values
	 */
	public String[] getValues() {
		String[] values = new String[txtValues.size()];
		txtValues.toArray(values);
		return values;
	}

	/**
	 * Return the host name of the node.
	 * @return Host name of the node
	 */
	public String getHostName() {
		return hostName;
	}

	/**
	 * Return IP address of the node
	 * @return IP address of the node
	 */
	public String getHostAddress() {
		return ipAddress;
	}

	/**
	 * Return service name of the node
	 * @return Service name of the node
	 */
	public String getServiceName() {
		return fullName;
	}

	/**
	 * Return the value corresponding to the key for the node
	 * @param key The key name
	 * @return The value corresponding to the key
	 */
	public String get(String key) {
		return txtRecord.getValueAsString(key);
	}

	/**
	 * Return the port number for this service.
	 * @return port Port on which service is announced as running.
	 */
	public int getPort() {
		return port;
	}

	/*
	 * Local method to update the key and value data structures
	 */
	protected void _localTxtRec(TXTRecord txtRecord) {
		int size = txtRecord.size();
		txtKeys.removeAllElements();
		txtValues.removeAllElements();
		for (int i=0; i<size; i++) {
			txtKeys.add(txtRecord.getKey(i));
			txtValues.add(txtRecord.getValueAsString(i));
		}
	}
}
