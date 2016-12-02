package org.cyberpwn.currencyshops;

import java.io.File;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.cyberpwn.currecnyshops.object.PlayerData;
import org.phantomapi.clust.DataController;
import org.phantomapi.construct.Controllable;
import org.phantomapi.sync.TaskLater;

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
		File f = new File(new File(getPlugin().getDataFolder(), "playerdata"), identifier.getUniqueId().toString() + ".yml");
		PlayerData pd = new PlayerData(identifier);
		
		if(f.exists())
		{
			loadCluster(pd, "playerdata");
			
			new TaskLater(20)
			{
				@Override
				public void run()
				{
					f.delete();
				}
			};
		}
		
		else
		{
			loadMysql(pd);
		}
		
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
