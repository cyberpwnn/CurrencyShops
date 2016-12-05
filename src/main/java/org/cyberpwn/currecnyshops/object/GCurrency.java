package org.cyberpwn.currecnyshops.object;

import org.bukkit.entity.Player;
import org.phantomapi.clust.ColdLoad;
import org.phantomapi.clust.Comment;
import org.phantomapi.clust.Configurable;
import org.phantomapi.clust.DataCluster;
import org.phantomapi.clust.Keyed;
import org.phantomapi.lang.GList;
import org.phantomapi.util.F;

@ColdLoad
public class GCurrency implements Currency, Configurable
{
	private DataCluster cc;
	private String name;
	
	@Comment("The Name for a SINGLE amount of this currency. \nor example 1 Coin (instead of 1 Coins)")
	@Keyed("currency.name-single")
	public String single = "&6Coin";
	
	@Comment("Amount to give on vote")
	@Keyed("currency.vote-reward")
	public int onVote = 0;
	
	@Comment("The Name for a PLURAL worded amount of this currency. \nFor example 0 Coins (instead of 0 Coin)")
	@Keyed("currency.name-plural")
	public String plural = "&6Coins";
	
	@Comment("The description of this currency.")
	@Keyed("currency.description")
	public String description = "&6The Currency description";
	
	@Comment("The command aliases.")
	@Keyed("currency.aliases")
	public GList<String> aliases = new GList<String>().qadd("co");
	
	@Comment("Should the name (plural or single) be prefixed with the number or suffixed?\nFALSE = '4 <SINGLE/PLURAL>'\nTRUE = '<SINGLE/PLURAL> 4'")
	@Keyed("currency.prefix-name")
	public Boolean prefixName = false;
	
	@Comment("If set to true, this will allow players to freely use /<currency> pay <player> <amt>")
	@Keyed("currency.allow-pay")
	public Boolean pay = false;
	
	public GCurrency(String name)
	{
		cc = new DataCluster();
		this.name = name;
	}
	
	@Override
	public void onNewConfig()
	{
		// Dynamic
	}
	
	@Override
	public void onReadConfig()
	{
		single = F.color(single);
		plural = F.color(plural);
		description = F.color(description);
	}
	
	@Override
	public DataCluster getConfiguration()
	{
		return cc;
	}
	
	@Override
	public String getCodeName()
	{
		return getName();
	}
	
	@Override
	public String getSingle()
	{
		return single;
	}
	
	@Override
	public String getPlural()
	{
		return plural;
	}
	
	@Override
	public Boolean prefixName()
	{
		return prefixName;
	}
	
	@Override
	public String getName()
	{
		return name;
	}
	
	@Override
	public String getDescription()
	{
		return description;
	}
	
	@Override
	public GList<String> getAliases()
	{
		return aliases;
	}
	
	@Override
	public Boolean allowPay()
	{
		return pay;
	}
	
	public void processVote(Player p)
	{
		if(onVote > 0)
		{
			try
			{
				new Transaction(this, onVote).to(p).commit();
			}
			
			catch(TransactionExeption e)
			{
				e.printStackTrace();
			}
		}
	}
}
