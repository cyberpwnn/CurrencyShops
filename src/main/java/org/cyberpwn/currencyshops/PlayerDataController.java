package org.cyberpwn.currencyshops;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.cyberpwn.currecnyshops.object.PlayerData;
import org.phantomapi.clust.PlayerDataHandler;
import org.phantomapi.construct.Controllable;

public class PlayerDataController extends PlayerDataHandler<PlayerData>
{
	public PlayerDataController(Controllable parentController)
	{
		super(parentController);
	}
	
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
		loadMysql(pd);
		return pd;
	}

	@Override
	public void onSave(Player identifier)
	{
		saveMysql(get(identifier));
	}
	
	@EventHandler
	public void on(PlayerQuitEvent e)
	{
		save(e.getPlayer());
	}

	@Override
	public void onStart()
	{
		
	}
}
