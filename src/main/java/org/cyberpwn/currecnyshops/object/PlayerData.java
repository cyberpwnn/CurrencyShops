package org.cyberpwn.currecnyshops.object;

import org.bukkit.entity.Player;
import org.phantomapi.clust.ColdLoad;
import org.phantomapi.clust.Configurable;
import org.phantomapi.clust.DataCluster;
import org.phantomapi.clust.PD;
import org.phantomapi.clust.Tabled;

@ColdLoad
@Tabled("player_currency_data")
public class PlayerData implements Configurable
{
	private Player player;
	
	public DataCluster cc;
	
	public PlayerData(Player player)
	{
		this.player = player;
		cc = new DataCluster();
	}
	
	@Override
	public void onNewConfig()
	{
		// Dynamic
	}
	
	@Override
	public void onReadConfig()
	{
		// Dynamic
	}
	
	@Override
	public DataCluster getConfiguration()
	{
		return cc;
	}
	
	@Override
	public String getCodeName()
	{
		return player.getUniqueId().toString();
	}
	
	public void setCurrency(String currency, double amount)
	{
		PD.get(player).getConfiguration().set("currency." + currency, amount);
	}
	
	public void addCurrency(String currency, double amount)
	{
		setCurrency(currency, hasCurrency(currency) ? getCurrency(currency) + amount : amount);
	}
	
	public boolean hasCurrency(String currency)
	{
		return PD.get(player).getConfiguration().contains("currency." + currency);
	}
	
	public double getCurrency(String currency)
	{
		if(!hasCurrency(currency))
		{
			return 0;
		}
		
		try
		{
			return PD.get(player).getConfiguration().getDouble("currency." + currency);
		}
		
		catch(Exception e)
		{
			return PD.get(player).getConfiguration().getInt("currency." + currency).doubleValue();
		}
	}
}
