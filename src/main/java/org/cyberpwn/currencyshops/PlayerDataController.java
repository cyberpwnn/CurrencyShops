package org.cyberpwn.currencyshops;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.cyberpwn.currecnyshops.object.PlayerData;
import org.phantomapi.clust.DataController;
import org.phantomapi.construct.Controllable;

public class PlayerDataController extends DataController<PlayerData, Player>
{
	public PlayerDataController(Controllable parentController)
	{
		super(parentController);
	}
	
	@Override
	public void onStop()
	{
		for(Player i : cache.k())
		{
			save(i);
		}
	}
	
	@Override
	public PlayerData onLoad(Player identifier)
	{
		PlayerData pd = new PlayerData(identifier);
		loadCluster(pd, "playerdata");
		
		return pd;
	}
	
	@Override
	public void onSave(Player identifier)
	{
		saveCluster(get(identifier), "playerdata");
	}
	
	@EventHandler
	public void on(PlayerQuitEvent e)
	{
		save(e.getPlayer());
	}
	
	@EventHandler
	public void on(PlayerJoinEvent e)
	{
		load(e.getPlayer());
	}
	
	@Override
	public void onStart()
	{
		
	}
}
