package com.unwiredappeal.tivo.config;

public class ConfigEntry {
	public String value;
	public String name;
	public String helpMsg;
	public String defaultValue;
	private String match = null;
	private ConfigurationManager.propertyHandler handler;

	public ConfigEntry(String cfgName, String def, String helpMsg) {
		//this.value = ConfigurationManager.inst.getStringProperty(cfgName, def);
		this.name = cfgName;
		this.helpMsg = helpMsg;
		this.defaultValue = def;
		ConfigurationManager.inst.addConfigEntry(this);
	}
	public ConfigEntry(String cfgName, String def) {
		this(cfgName, def, null);
	}
	
	public ConfigEntry(String cfgName) {
		this(cfgName, "false", "true/false");
	}
	
	public ConfigEntry(String baseName, ConfigurationManager.propertyHandler handler, String helpMsg) {
		this(baseName + ".x", "", helpMsg);
		this.handler = handler;
		match = baseName.replaceAll("\\.", "\\.") + "\\.\\d+";
		
	}
	
	public String getValue() {
		return value;
	}
	public void process(ConfigurableObject te) {
		if (this.match == null)
			this.value = ConfigurationManager.inst.getStringProperty(this.name, this.defaultValue);
		else {
			ConfigurationManager.inst.matchProperties(te, match, handler);

		}
	}
	
	public boolean getBool() {
		return ConfigurationManager.parseBool(value);
	}
	
	public int getInt() {
		return ConfigurationManager.parseInt(value);
	}
	
	
}
