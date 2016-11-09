package org.cyberpwn.currencyshops;

import org.bukkit.entity.Player;
import org.phantomapi.Phantom;
import org.phantomapi.construct.ControllerMessage;
import org.phantomapi.construct.PhantomPlugin;
import org.phantomapi.util.DMSRequire;
import org.phantomapi.util.DMSRequirement;
import org.phantomapi.world.MaterialBlock;
import org.phantomapi.world.W;

@DMSRequire(DMSRequirement.SQL)
public class CurrencyShops extends PhantomPlugin
{
	private CurrencyShopsController currencyShopsController;
	public static CurrencyShops instance;
	
	@Override
	public void enable()
	{
		instance = this;
		
		currencyShopsController = new CurrencyShopsController(this);
		
		register(currencyShopsController);
	}
	
	@Override
	public void disable()
	{
		for(Player i : Phantom.instance().onlinePlayers())
		{
			i.closeInventory();
		}
	}
	
	public double value(String shop, MaterialBlock mb)
	{
		try
		{
			return instance.currencyShopsController.getShopController().get(shop).getBuy(mb);
		}
		
		catch(Exception e)
		{
			return 0.0;
		}
	}
	
	public static CurrencyShopsController instance()
	{
		return instance.currencyShopsController;
	}
	
	@Override
	public ControllerMessage onControllerMessageRecieved(ControllerMessage message)
	{
		if(message.contains("value") && message.contains("shop"))
		{
			try
			{
				MaterialBlock mb = W.getMaterialBlock(message.getString("value"));
				String shop = message.getString("shop");
				
				if(instance.currencyShopsController.getShopController().isLoaded(shop))
				{
					message.set("result", value(shop, mb));
				}
			}
			
			catch(Exception e)
			{
				
			}
		}
		
		if(message.contains("check-load"))
		{
			try
			{
				String shop = message.getString("check-load");
				message.set("result", instance.currencyShopsController.getShopController().isLoaded(shop));
			}
			
			catch(Exception e)
			{
				
			}
		}
		
		return message;
	}
}
