package com.unwiredappeal.tivo.push;

public class Tivo {
	private String tivoName;
	private String username;
	private String password;
	private String tsn;
	private String mind;
	
	public boolean addedAutomatically = false;
	
	public Tivo(Tivo t) {
		this(t.getName(), t.getTsn(), t.getUsername(), t.getPassword(), t.getMind());
		this.addedAutomatically = t.addedAutomatically;
	}
	public Tivo(String s) {
		setName(s);
	}
	
	public Tivo(String tivoName, String tsn, String username, String password, String mind) {
		this.tivoName = tivoName;
		this.tsn = tsn;
		this.username = username;
		this.password = password;
		this.mind = mind;
	}

	public String getMind() {
		return mind;
	}
	
	public void setName(String n) {
		tivoName = n;
	}
	
	public String getName() {
		return tivoName;
	}
	
	public String toString() {
		return getName();
	}
	
	public String getTsn() {
		return tsn;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public boolean getAuto() {
		return addedAutomatically;
	}
	
	public void setAuto(boolean b) {
		addedAutomatically = b;
	}
}
