package org.graylog2.host;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class HostConfiguration {
	private String facility;
	private String originHost;

	public String getFacility() {
		return facility;
	}

	public void setFacility(String facility) {
		this.facility = facility;
	}

	public String getOriginHost() {
		if (originHost == null) {
			originHost = getLocalHostName();
		}
		return originHost;
	}

	public void setOriginHost(String originHost) {
		this.originHost = originHost;
	}

	private String getLocalHostName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException uhe) {
			throw new IllegalStateException(
					"Origin host could not be resolved automatically. Please set originHost property", uhe);
		}
	}

}
