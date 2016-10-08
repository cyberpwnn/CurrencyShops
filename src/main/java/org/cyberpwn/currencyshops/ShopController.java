package org.cyberpwn.currencyshops;

import java.io.File;
import org.bukkit.entity.Player;
import org.cyberpwn.currecnyshops.object.GShop;
import org.phantomapi.Phantom;
import org.phantomapi.clust.AsyncDataController;
import org.phantomapi.clust.ClustAsyncAlreadyLoadingException;
import org.phantomapi.construct.Controllable;

public class ShopController extends AsyncDataController<GShop, String>
{
	private File shops;
	
	public ShopController(Controllable parentController)
	{
		super(parentController);
		
		shops = new File(getPlugin().getDataFolder(), "shops");
		
		if(!shops.exists())
		{
			shops.mkdirs();
		}
	}
	
	public void onStart()
	{
		
	}
	
	public void onStop()
	{
		for(Player i : Phantom.instance().onlinePlayers())
		{
			i.closeInventory();
		}
	}
	
	private boolean hasCurrency(String identifier)
	{
		return new File(shops, identifier + ".yml").exists();
	}
	
	@Override
	public GShop onLoad(String identifier)
	{
		if(hasCurrency(identifier))
		{
			GShop c = new GShop(identifier);
			loadCluster(c, "shops");
			
			return c;
		}
		
		return null;
	}
	
	@Override
	public void onSave(String identifier)
	{
		try
		{
			saveCluster(get(identifier), "shops");
		}
		
		catch(ClustAsyncAlreadyLoadingException e)
		{
			f("CANNOT SAVE, STILL LOADING SHOP: " + identifier);
		}
	}

	public File getShopFile()
	{
		return shops;
	}
}
