package org.cyberpwn.currencyshops;

import java.io.File;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.cyberpwn.currecnyshops.object.PlayerData;
import org.phantomapi.clust.ConfigurationHandler;
import org.phantomapi.clust.DataController;
import org.phantomapi.clust.PD;
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
			try
			{
				if(ConfigurationHandler.rowExists(pd, getSQL()))
				{
					loadMysql(pd);
					ConfigurationHandler.fromFields(pd);
					PD.get(identifier).getConfiguration().add(pd.getConfiguration(), "currency.");
					
					new TaskLater()
					{
						@Override
						public void run()
						{
							try
							{
								ConfigurationHandler.dropRow(pd, getSQL());
							}
							
							catch(Exception e)
							{
								
							}
						}
					};
				}
			}
			
			catch(Exception e)
			{
				
			}
		}
		
		return pd;
	}
	
	@Override
	public void onSave(Player identifier)
	{
		
	}
	
	@EventHandler
	public void on(PlayerQuitEvent e)
	{
		
	}
	
	@Override
	public void onStart()
	{
		
	}
}
