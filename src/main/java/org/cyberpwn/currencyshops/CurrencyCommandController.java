package org.cyberpwn.currencyshops;

import java.io.File;
import org.bukkit.entity.Player;
import org.cyberpwn.currecnyshops.object.GCurrency;
import org.cyberpwn.currecnyshops.object.Transaction;
import org.cyberpwn.currecnyshops.object.TransactionExeption;
import org.phantomapi.Phantom;
import org.phantomapi.command.CommandController;
import org.phantomapi.command.CommandFilter;
import org.phantomapi.command.CommandListener;
import org.phantomapi.command.PhantomCommand;
import org.phantomapi.command.PhantomCommandSender;
import org.phantomapi.construct.Controllable;
import org.phantomapi.lang.GList;
import org.phantomapi.util.C;
import org.phantomapi.util.F;

public class CurrencyCommandController extends CommandController
{
	private GList<String> names;
	private GList<String> aliases;
	
	public CurrencyCommandController(Controllable parentController)
	{
		super(parentController, "currency");
		
		names = new GList<String>();
		aliases = new GList<String>();
	}
	
	@Override
	public void onStart()
	{
		for(File i : CurrencyShops.instance().getCurrencyController().getCurrencyFile().listFiles())
		{
			names.add(i.getName().replaceAll(".yml", ""));
			s("Registered Currency: " + i.getName().replaceAll(".yml", ""));
			
			for(String j : CurrencyShops.instance().getCurrencyController().get(i.getName().replaceAll(".yml", "")).getAliases())
			{
				s(" >> Registered ALIAS: " + i.getName().replaceAll(".yml", "") + " <> " + j);
				aliases.add(j);
			}
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
				GCurrency c = CurrencyShops.instance().getCurrencyController().get(i.toLowerCase());
				
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
			GCurrency c = CurrencyShops.instance().getCurrencyController().get(command.getName().toLowerCase());
			sender.setMessageBuilder(sender.getMessageBuilder().setTag(C.DARK_GRAY + "[" + c.getPlural() + C.DARK_GRAY + "]" + C.GRAY + ": ", c.getDescription()));
			
			if(command.getArgs().length == 0 && sender.isPlayer())
			{
				sender.sendMessage(C.getLastColors(c.getSingle()) + F.f(CurrencyShops.instance().getPlayerDataController().get(sender.getPlayer()).getCurrency(c.getName()), 2));
				
				return true;
			}
			
			else if(command.getArgs().length >= 2)
			{
				String sub = command.getArgs()[0];
				String mod = command.getArgs()[1];
				
				if(command.getArgs().length == 2 && sub.equalsIgnoreCase("get"))
				{
					if(sender.hasPermission("currencyshops.god"))
					{
						if(Phantom.instance().canFindPlayer(mod))
						{
							sender.sendMessage(C.getLastColors(c.getSingle()) + Phantom.instance().findPlayer(mod).getName() + " <> " + F.f(CurrencyShops.instance().getPlayerDataController().get(Phantom.instance().findPlayer(mod)).getCurrency(c.getName())));
						}
						
						else
						{
							sender.sendMessage(C.RED + "Cannot find any player matching '" + mod + "'.");
						}
					}
					
					else
					{
						sender.sendMessage(getMessageNoPermission());
					}
					
					return true;
				}
				
				if(command.getArgs().length == 3)
				{
					String subMod = command.getArgs()[2];
					
					if(sub.equalsIgnoreCase("pay") && sender.isPlayer() && c.allowPay())
					{
						if(Phantom.instance().canFindPlayer(mod))
						{
							try
							{
								double a = Double.valueOf(subMod);
								Player p = Phantom.instance().findPlayer(mod);
								
								if(a < 0)
								{
									return true;
								}
								
								if(p.equals(sender.getPlayer()))
								{
									p.sendMessage(C.RED + "You cant pay yourself.");
									
									return true;
								}
								
								else
								{
									new Transaction(c, a).to(p).from(sender.getPlayer()).commit();
								}
							}
							
							catch(NumberFormatException e)
							{
								sender.sendMessage(getMessageInvalidArgument(subMod, "INTEGER"));
							}
							
							catch(TransactionExeption e)
							{
								sender.sendMessage(C.RED + e.getMessage());
							}
						}
						
						else
						{
							sender.sendMessage(C.RED + "Cannot find any player matching '" + mod + "'.");
						}
					}
					
					if(sender.hasPermission("currencyshops.god"))
					{
						if(sub.equalsIgnoreCase("give"))
						{
							if(Phantom.instance().canFindPlayer(mod))
							{
								Player p = Phantom.instance().findPlayer(mod);
								
								try
								{
									double a = Double.valueOf(subMod);
									new Transaction(c, a).to(p).commit();
									sender.sendMessage(C.GREEN + "Gave " + p.getName() + " " + a);
								}
								
								catch(NumberFormatException e)
								{
									sender.sendMessage(getMessageInvalidArgument(subMod, "INTEGER"));
								}
								
								catch(TransactionExeption e)
								{
									sender.sendMessage(C.RED + e.getMessage());
								}
							}
							
							else
							{
								sender.sendMessage(C.RED + "Cannot find any player matching '" + mod + "'.");
							}
						}
						
						if(sub.equalsIgnoreCase("take"))
						{
							if(Phantom.instance().canFindPlayer(mod))
							{
								try
								{
									double a = Double.valueOf(subMod);
									Player p = Phantom.instance().findPlayer(mod);
									new Transaction(c, a).from(p).commit();
									sender.sendMessage(C.GREEN + "Took " + a + " from " + p.getName());
								}
								
								catch(NumberFormatException e)
								{
									sender.sendMessage(getMessageInvalidArgument(subMod, "INTEGER"));
								}
								
								catch(TransactionExeption e)
								{
									sender.sendMessage(C.RED + e.getMessage());
								}
							}
							
							else
							{
								sender.sendMessage(C.RED + "Cannot find any player matching '" + mod + "'.");
							}
						}
					}
					
					else
					{
						sender.sendMessage(getMessageNoPermission());
					}
					
					return true;
				}
				
				return true;
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
