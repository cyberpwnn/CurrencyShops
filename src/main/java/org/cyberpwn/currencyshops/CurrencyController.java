package org.cyberpwn.currencyshops;

import java.io.File;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.cyberpwn.currecnyshops.object.GCurrency;
import org.phantomapi.clust.DataController;
import org.phantomapi.command.PhantomSender;
import org.phantomapi.construct.Controllable;
import org.phantomapi.text.MessageBuilder;
import org.phantomapi.util.C;
import org.phantomapi.util.F;
import com.earth2me.essentials.craftbukkit.SetExpFix;
import net.milkbowl.vault.economy.Economy;

public class CurrencyController extends DataController<GCurrency, String> implements CommandExecutor
{
	private File currency;
	private Economy econ = null;
	
	public CurrencyController(Controllable parentController)
	{
		super(parentController);
		
		currency = new File(getPlugin().getDataFolder(), "currencies");
		
		if(!currency.exists())
		{
			currency.mkdirs();
		}
		
		getPlugin().getCommand("compatcusp").setExecutor(this);
	}
	
	private boolean setupEconomy()
	{
		if(getPlugin().getServer().getPluginManager().getPlugin("Vault") == null)
		{
			return false;
		}
		
		RegisteredServiceProvider<Economy> rsp = getPlugin().getServer().getServicesManager().getRegistration(Economy.class);
		
		if(rsp == null)
		{
			return false;
		}
		
		econ = rsp.getProvider();
		
		return econ != null;
	}
	
	@Override
	public void onStart()
	{
		setupEconomy();
	}
	
	@Override
	public void onStop()
	{
		
	}
	
	private boolean hasCurrency(String identifier)
	{
		return new File(currency, identifier + ".yml").exists();
	}
	
	@Override
	public GCurrency onLoad(String identifier)
	{
		if(hasCurrency(identifier))
		{
			GCurrency c = new GCurrency(identifier);
			loadCluster(c, "currencies");
			
			return c;
		}
		
		return null;
	}
	
	@Override
	public void onSave(String identifier)
	{
		saveCluster(get(identifier), "currencies");
	}
	
	public File getCurrencyFile()
	{
		return currency;
	}
	
	public File getCurrency()
	{
		return currency;
	}
	
	public Economy getEcon()
	{
		return econ;
	}
	
	@Override
	public GCurrency get(String t)
	{
		try
		{
			if(t.equalsIgnoreCase("vault"))
			{
				return new GCurrency("vault");
			}
			
			return super.get(t);
		}
		
		catch(Exception e)
		{
			
		}
		
		return null;
	}
	
	@EventHandler
	public void on(PlayerCommandPreprocessEvent e)
	{
		if(e.getMessage().equalsIgnoreCase("/xp"))
		{
			MessageBuilder mb = new MessageBuilder().setTag(C.DARK_GRAY + "[" + C.GREEN + "XP" + C.DARK_GRAY + "]" + C.GRAY + ": ", C.GREEN + "Your Experience");
			Player p = e.getPlayer();
			PhantomSender sender = new PhantomSender(p);
			sender.setMessageBuilder(mb);
			sender.sendMessage(C.GREEN + F.f(SetExpFix.getTotalExperience(p)) + "xp");
			e.setCancelled(true);
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String name, String[] args)
	{
		if(!sender.hasPermission("cshv.god"))
		{
			return false;
		}
		
		if(args.length != 1)
		{
			return true;
		}
		
		Player p = Bukkit.getPlayer(args[0]);
		
		if(p != null)
		{
			for(String i : cache.k())
			{
				cache.get(i).processVote(p);
			}
		}
		
		return false;
	}
}
