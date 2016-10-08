package org.cyberpwn.currecnyshops.object;

import org.bukkit.entity.Player;
import org.cyberpwn.currencyshops.CurrencyShops;
import org.phantomapi.text.MessageBuilder;
import org.phantomapi.util.C;
import org.phantomapi.util.F;
import com.earth2me.essentials.craftbukkit.SetExpFix;

public class Transaction
{
	private Currency c;
	private double a;
	private Player from;
	private Player to;
	private boolean xp;
	
	public Transaction(Currency c, double a)
	{
		xp = false;
		
		if(c == null)
		{
			xp = true;
		}
		
		else if(c.getName().equalsIgnoreCase("vault"))
		{
			c = null;
		}
		
		else if(c.getName().equalsIgnoreCase("xp"))
		{
			c = null;
			xp = true;
		}
		
		this.c = c;
		this.a = a;
		this.from = null;
		this.to = null;
	}
	
	public Transaction from(Player p)
	{
		from = p;
		
		return this;
	}
	
	public Transaction to(Player p)
	{
		to = p;
		
		return this;
	}
	
	public void commit() throws TransactionExeption
	{
		if(from == null)
		{
			if(to != null)
			{
				give(to, a);
			}
		}
		
		else if(to == null)
		{
			if(from != null)
			{
				take(from, a);
			}
		}
		
		else
		{
			pay(from, to, a);
		}
	}
	
	private MessageBuilder gmb()
	{
		return new MessageBuilder().setTag(C.DARK_GRAY + "[" + C.getLastColors(c.getSingle()) + c.getPlural() + C.DARK_GRAY + "]" + C.GRAY + ": ", c.getDescription());
	}
	
	private MessageBuilder gmbx()
	{
		return new MessageBuilder().setTag(C.DARK_GRAY + "[" + C.GOLD + "Money" + C.DARK_GRAY + "]" + C.GRAY + ": ", C.GOLD + "Your money.");
	}
	
	private MessageBuilder gmbxp()
	{
		return new MessageBuilder().setTag(C.DARK_GRAY + "[" + C.GREEN + "XP" + C.DARK_GRAY + "]" + C.GRAY + ": ", C.GREEN + "Your Experience");
	}
	
	public void diff(Player p, double a2)
	{
		if(c == null)
		{
			if(xp)
			{
				String cc = C.GREEN.toString();
				String dd = C.DARK_GRAY.toString();
				String u = C.BOLD + C.GREEN.toString();
				String d = C.BOLD + C.RED.toString();
				gmbxp().message(p, dd + (a2 > 0 ? u + "+ " + u + C.stripColor(F.f((int) a2)) : d + "- " + d + "$" + C.stripColor(F.f((int) -a2))) + dd + "xp (" + cc + C.stripColor(F.f((int)SetExpFix.getTotalExperience(p))) + dd + "xp)");
				return;
			}
			
			String cc = C.YELLOW.toString();
			String dd = C.DARK_GRAY.toString();
			String u = C.BOLD + C.GREEN.toString();
			String d = C.BOLD + C.RED.toString();
			gmbx().message(p, dd + (a2 > 0 ? u + "+ " + u + "$" + C.stripColor(F.f((int) a2)) : d + "- " + d + "$" + C.stripColor(F.f((int) -a2))) + dd + " (" + cc + "$" + C.stripColor(F.f((int)get(p))) + dd + ")");
			return;
		}
		
		String cc = C.getLastColors(c.getPlural());
		String dd = C.DARK_GRAY.toString();
		String u = C.BOLD + C.GREEN.toString();
		String d = C.BOLD + C.RED.toString();
		gmb().message(p, dd + (a2 > 0 ? u + "+ " + u + F.f((int) a2) : d + "- " + d + F.f((int) -a2)) + dd + " (" + cc + F.f((int)get(p)) + dd + ")");
	}
	
	private void give(Player p, double a2)
	{
		if(c == null)
		{
			if(xp)
			{
				SetExpFix.setTotalExperience(p, (int) (SetExpFix.getTotalExperience(p) + a2));
				diff(p, a2);
				return;
			}
			
			CurrencyShops.instance().getCurrencyController().getEcon().depositPlayer(p, a2);
			diff(p, a2);
			return;
		}
		
		CurrencyShops.instance().getPlayerDataController().get(p).addCurrency(c.getName(), a2);
		diff(p, a2);
	}
	
	private boolean canTake(Player p, double a2)
	{
		return get(p) - a2 >= 0;
	}
	
	private void take(Player p, double a2) throws TransactionExeption
	{
		if(canTake(p, a2))
		{
			if(c == null)
			{
				if(xp)
				{
					SetExpFix.setTotalExperience(p, (int) (SetExpFix.getTotalExperience(p) - a2));
					diff(p, -a2);
					return;
				}
				
				CurrencyShops.instance().getCurrencyController().getEcon().withdrawPlayer(p, a2);
				diff(p, -a2);
				return;
			}
			
			CurrencyShops.instance().getPlayerDataController().get(p).addCurrency(c.getName(), -a2);
			diff(p, -a2);
		}
		
		else
		{
			throw new TransactionExeption("Cannot make Transfer. Not enough funds");
		}
	}
	
	private double get(Player p)
	{
		if(c == null)
		{
			if(xp)
			{
				return SetExpFix.getTotalExperience(p);
			}
			
			return (long) CurrencyShops.instance().getCurrencyController().getEcon().getBalance(p);
		}
		
		return CurrencyShops.instance().getPlayerDataController().get(p).getCurrency(c.getName());
	}
	
	private void pay(Player f, Player t, double a2) throws TransactionExeption
	{
		if(canTake(f, a2))
		{
			take(f, a2);
			give(t, a2);
		}
		
		else
		{
			throw new TransactionExeption("Cannot make Transfer. Not enough funds");
		}
	}
}
