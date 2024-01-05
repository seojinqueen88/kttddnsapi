package com.kttddnsapi.model;

public class P2pregister
{
	private String mac;
	private String p2p_uid;
	private int p2p_priority;

	public P2pregister() {
		super();
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public String getP2p_uid() {
		return p2p_uid;
	}

	public void setP2p_uid(String p2p_uid) {
		this.p2p_uid = p2p_uid;
	}

	public int getP2p_priority() {
		return p2p_priority;
	}

	public void setP2p_priority(int p2p_priority) {
		this.p2p_priority = p2p_priority;
	}
}
