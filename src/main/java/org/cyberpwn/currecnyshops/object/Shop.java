package org.cyberpwn.currecnyshops.object;

import org.phantomapi.lang.GList;

public interface Shop
{
	public Currency getCurrency();
	
	public String getName();
	
	public GList<String> getAliases();
	
	public String getDescription();
	
	public String getDisplayName();
}
