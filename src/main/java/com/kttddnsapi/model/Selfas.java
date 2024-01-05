package com.kttddnsapi.model;

import java.sql.Timestamp;

public class Selfas
{
	private int idx;
	private String mac;
	private String certnum;
	private Timestamp certnum_create_time;
	private String token;
	private Timestamp token_create_time;
	private int user_level;

	public Selfas() {
		super();
	}

	public int getIdx() {
		return idx;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public String getCertnum() {
		return certnum;
	}

	public void setCertnum(String certnum) {
		this.certnum = certnum;
	}

	public Timestamp getCertnum_create_time() {
		return certnum_create_time;
	}

	public void setCertnum_create_time(Timestamp certnum_create_time) {
		this.certnum_create_time = certnum_create_time;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Timestamp getToken_create_time() {
		return token_create_time;
	}

	public void setToken_create_time(Timestamp token_create_time) {
		this.token_create_time = token_create_time;
	}

	public int getUser_level() {
		return user_level;
	}

	public void setUser_level(int user_level) {
		this.user_level = user_level;
	}
}
