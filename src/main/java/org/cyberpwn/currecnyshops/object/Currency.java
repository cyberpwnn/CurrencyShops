package org.cyberpwn.currecnyshops.object;

import org.phantomapi.lang.GList;

public interface Currency
{
	public String getSingle();
	
	public String getPlural();
	
	public String getName();
	
	public String getDescription();
	
	public GList<String> getAliases();
	
	public Boolean prefixName();
	
	public Boolean allowPay();
}
