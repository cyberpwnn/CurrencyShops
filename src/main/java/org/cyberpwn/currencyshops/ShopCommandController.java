package org.cyberpwn.currencyshops;

import java.io.File;
import org.cyberpwn.currecnyshops.object.DupeScan;
import org.cyberpwn.currecnyshops.object.GShop;
import org.phantomapi.Phantom;
import org.phantomapi.clust.ClustAsyncAlreadyLoadingException;
import org.phantomapi.command.CommandController;
import org.phantomapi.command.CommandFilter;
import org.phantomapi.command.CommandListener;
import org.phantomapi.command.PhantomCommand;
import org.phantomapi.command.PhantomCommandSender;
import org.phantomapi.construct.Controllable;
import org.phantomapi.lang.GList;
import org.phantomapi.util.C;

public class ShopCommandController extends CommandController
{
	private GList<String> names;
	private GList<String> aliases;
	
	public ShopCommandController(Controllable parentController)
	{
		super(parentController, "shops");
		
		names = new GList<String>();
		aliases = new GList<String>();
	}
	
	@Override
	public void onStart()
	{
		for(File i : CurrencyShops.instance().getShopController().getShopFile().listFiles())
		{
			names.add(i.getName().replaceAll(".yml", ""));
			s("Async Loading Shop: " + i.getName().replaceAll(".yml", ""));
			
			try
			{
				CurrencyShops.instance().getShopController().get(i.getName().replaceAll(".yml", ""));
			}
			
			catch(ClustAsyncAlreadyLoadingException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void reregister(String shop)
	{
		try
		{
			for(String j : CurrencyShops.instance().getShopController().get(shop).getAliases())
			{
				s(" >> Registered ALIAS: " + shop + " <> " + j);
				aliases.add(j);
			}
		}
		
		catch(ClustAsyncAlreadyLoadingException e)
		{
			e.printStackTrace();
		}
		
		Phantom.instance().getCommandRegistryController().unregister((CommandListener) this);
		Phantom.instance().getCommandRegistryController().register((CommandListener) this);
	}
	
	@Override
	public String getChatTag()
	{
		return C.DARK_GRAY + "[" + C.GREEN + "CUSP" + C.DARK_GRAY + "]" + C.GRAY + ": ";
	}
	
	@Override
	public String getChatTagHover()
	{
		return C.GREEN + "Currency Shops " + getPlugin().getDescription().getVersion();
	}
	
	@Override
	public GList<String> getCommandAliases()
	{
		GList<String> kc = new GList<String>();
		kc.addAll(names);
		kc.addAll(aliases);
		
		return kc;
	}
	
	@Override
	@CommandFilter.ArgumentRange({0, 3})
	public boolean onCommand(PhantomCommandSender sender, PhantomCommand command)
	{
		if(!names.contains(command.getName().toLowerCase()))
		{
			for(String i : names)
			{
				GShop c = null;
				
				try
				{
					c = CurrencyShops.instance().getShopController().get(i.toLowerCase());
				}
				
				catch(ClustAsyncAlreadyLoadingException e)
				{
					e.printStackTrace();
				}
				
				boolean b = false;
				
				for(String j : c.getAliases())
				{
					if(command.getName().equalsIgnoreCase(j))
					{
						command.setName(i);
						b = true;
						
						break;
					}
				}
				
				if(b)
				{
					break;
				}
			}
		}
		
		if(names.contains(command.getName().toLowerCase()))
		{
			GShop c = null;
			
			try
			{
				c = CurrencyShops.instance().getShopController().get(command.getName().toLowerCase());
			}
			
			catch(ClustAsyncAlreadyLoadingException e)
			{
				e.printStackTrace();
			}
			
			sender.setMessageBuilder(sender.getMessageBuilder().setTag(C.DARK_GRAY + "[" + c.getDisplayName() + C.DARK_GRAY + "]" + C.GRAY + ": ", c.getDescription()));
			
			if(command.getArgs().length == 0 && sender.isPlayer())
			{
				c.launchShop(sender.getPlayer());
				
				return true;
			}
			
			if(command.getArgs().length == 1 && sender.isPlayer())
			{
				if(command.getArgs()[0].equalsIgnoreCase("hand") && sender.hasPermission("cusp.hand"))
				{
					c.trySell(sender.getPlayer());
					
					return true;
				}
				
				if(command.getArgs()[0].equalsIgnoreCase("dupe") && sender.hasPermission("cusp.god"))
				{
					new DupeScan(c).direct();
					
					return true;
				}
				
				if(command.getArgs()[0].equalsIgnoreCase("all") && sender.hasPermission("cusp.all"))
				{
					c.trySellAll(sender.getPlayer());
					
					return true;
				}
			}
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public void onStop()
	{
		
	}
}
