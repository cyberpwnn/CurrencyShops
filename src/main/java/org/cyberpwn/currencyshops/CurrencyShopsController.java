package org.cyberpwn.currencyshops;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.phantomapi.Phantom;
import org.phantomapi.clust.DataCluster;
import org.phantomapi.clust.YAMLDataInput;
import org.phantomapi.clust.YAMLDataOutput;
import org.phantomapi.command.CommandController;
import org.phantomapi.command.PhantomCommand;
import org.phantomapi.command.PhantomCommandSender;
import org.phantomapi.construct.Controllable;
import org.phantomapi.gui.Slot;
import org.phantomapi.lang.GList;
import org.phantomapi.lang.GMap;
import org.phantomapi.lang.GQuadraset;
import org.phantomapi.lang.GTriset;
import org.phantomapi.sync.TaskLater;
import org.phantomapi.util.C;
import org.phantomapi.world.MaterialBlock;
import org.phantomapi.world.W;

public class CurrencyShopsController extends CommandController
{
	private final CurrencyController currencyController;
	private final CurrencyCommandController currencyCommandController;
	private final ShopCommandController shopCommandController;
	private final ShopController shopController;
	private final PlayerDataController playerDataController;
	
	public CurrencyShopsController(Controllable parentController)
	{
		super(parentController, "currencyshops");
		
		currencyController = new CurrencyController(this);
		shopController = new ShopController(this);
		currencyCommandController = new CurrencyCommandController(this);
		shopCommandController = new ShopCommandController(this);
		playerDataController = new PlayerDataController(this);
		
		register(currencyController);
		register(shopController);
		register(currencyCommandController);
		register(shopCommandController);
		register(playerDataController);
	}
	
	@SuppressWarnings("deprecation")
	public void onStart()
	{
		File f = new File(getPlugin().getDataFolder(), "convert");
		File fx = new File(getPlugin().getDataFolder(), "converted");
		
		if(f.exists())
		{
			fx.mkdirs();
			
			for(File j : f.listFiles())
			{
				w("Converting " + j.getName() + "...");
				
				DataCluster i = new DataCluster();
				DataCluster o = new DataCluster();
				
				try
				{
					w("  Reading " + j.getPath() + "...");
					new YAMLDataInput().load(i, j);
					
					DataCluster categories = i.crop("Categories");
					DataCluster itemx = i.crop("Items");
					
					GMap<String, GTriset<MaterialBlock, String, Slot>> cats = new GMap<String, GTriset<MaterialBlock, String, Slot>>();
					GMap<String, GList<GQuadraset<MaterialBlock, Double, Double, Integer>>> items = new GMap<String, GList<GQuadraset<MaterialBlock, Double, Double, Integer>>>();
					
					w("  Processing Categories...");
					
					Set<String> cccx = new HashSet<String>();
					
					for(String c : categories.getData().keySet())
					{
						cccx.add(c.split("\\.")[0]);
					}
					
					for(String c : cccx)
					{
						if(!c.contains("."))
						{
							MaterialBlock mb = W.getMaterialBlock(categories.getString(c + ".Item.Id").split(" ")[0]);
							String name = categories.getString(c + ".Item.Name");
							Slot slot = new Slot(categories.getInt(c + ".Slot"));
							
							w("    Processing " + c + "{mb: " + mb.getMaterial().toString() + ":" + mb.getData() + " name: " + name + " slot: " + slot.getX() + ", " + slot.getY() + "}");
							cats.put(c, new GTriset<MaterialBlock, String, Slot>(mb, name, slot));
							items.put(c, new GList<GQuadraset<MaterialBlock, Double, Double, Integer>>());
							
							Set<String> cccxx = new HashSet<String>();
							
							for(String x : itemx.getData().keySet())
							{
								cccxx.add(x.split("\\.")[0]);
							}
							
							for(String x : cccxx)
							{
								if(!x.contains(".") && itemx.getString(x + ".Category").equals(c))
								{
									MaterialBlock mbz = W.getMaterialBlock(itemx.getString(x + ".Id").split(" ")[0]);
									Double cost = 0.0;
									Double sell = 0.0;
									
									try
									{
										cost = itemx.getDouble(x + ".Buy-Price").doubleValue();
									}
									
									catch(Exception e)
									{
										try
										{
											cost = itemx.getInt(x + ".Buy-Price").doubleValue();
										}
										
										catch(Exception ex)
										{
											f("FAILURE NEAR BUY PRICE AT " + c + "/" + x);
											e.printStackTrace();
											return;
										}
									}
									
									try
									{
										sell = itemx.getDouble(x + ".Sell-Price").doubleValue();
									}
									
									catch(Exception e)
									{
										try
										{
											sell = itemx.getInt(x + ".Sell-Price").doubleValue();
										}
										
										catch(Exception ex)
										{
											f("FAILURE NEAR SELL PRICE AT " + c + "/" + x);
											e.printStackTrace();
											return;
										}
									}
									
									w("      Added: " + mbz.getMaterial() + ":" + mbz.getData() + " @ " + cost + " a piece");
									
									int ixix = 0;
									
									try
									{
										ixix = Integer.valueOf(x);
									}
									
									catch(Exception e)
									{
										
									}
									
									items.get(c).add(new GQuadraset<MaterialBlock, Double, Double, Integer>(mbz, cost, sell, ixix));
								}
							}
						}
					}
					
					w("  Thrashing Data to the Disk...");
					
					for(String c : cats.k())
					{
						GTriset<MaterialBlock, String, Slot> cda = cats.get(c);
						String cate = "shop.ui.categories." + c + ".";
						
						o.set(cate + "material", cda.getA().getMaterial().toString() + ":" + cda.getA().getData().intValue());
						o.set(cate + "name", cda.getB());
						o.set(cate + "x", cda.getC().getX());
						o.set(cate + "y", cda.getC().getY());
						o.set(cate + "lore", new GList<String>());
						
						for(GQuadraset<MaterialBlock, Double, Double, Integer> d : items.get(c))
						{
							String ccc = cate + "items." + UUID.randomUUID() + ".";
							o.set(ccc + "name", "&l&b" + (d.getA().getMaterial().toString().toLowerCase().substring(0, 1).toUpperCase() + d.getA().getMaterial().toString().substring(1).toLowerCase()).replaceAll("_", " "));
							o.set(ccc + "commands", new GList<String>().qadd("give %player% " + d.getA().getMaterial().getId() + ":" + d.getA().getData().intValue() + " %amount%"));
							o.set(ccc + "unlock-sound", "CLICK");
							o.set(ccc + "cost", d.getB());
							o.set(ccc + "sell", d.getC());
							o.set(ccc + "relative", d.getD());
							o.set(ccc + "material", d.getA().getMaterial().toString() + ":" + d.getA().getData().intValue());
							o.set(ccc + "required-slots", 1);
							o.set(ccc + "lore", new GList<String>().qadd("&aBuy: $%cost%&7 Left Click to Buy").qadd(d.getC() < 1 ? "&cItem Cannot be Sold" : "&aSell: $%sell%&7 Right Click to Sell"));
						}
					}
					
					File fz = new File(fx, j.getName());
					fz.delete();
					fz.createNewFile();
					new YAMLDataOutput().save(o, fz);
				}
				
				catch(IOException e)
				{
					f("Failed...");
					e.printStackTrace();
				}
			}
		}
		
		f.delete();
	}
	
	public void onStop()
	{
		
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
		return new GList<String>().qadd("cusp");
	}
	
	@Override
	public boolean onCommand(PhantomCommandSender sender, PhantomCommand cmd)
	{
		sender.sendMessage("Currency Shops v" + getPlugin().getDescription().getVersion());
		
		if(cmd.getArgs().length == 1)
		{
			if(cmd.getArgs()[0].equalsIgnoreCase("reload"))
			{
				new TaskLater()
				{
					@Override
					public void run()
					{
						Phantom.thrash(sender);
					}
				};
			}
		}
		
		return true;
	}
	
	public CurrencyController getCurrencyController()
	{
		return currencyController;
	}
	
	public ShopController getShopController()
	{
		return shopController;
	}
	
	public CurrencyCommandController getCurrencyCommandController()
	{
		return currencyCommandController;
	}
	
	public ShopCommandController getShopCommandController()
	{
		return shopCommandController;
	}
	
	public PlayerDataController getPlayerDataController()
	{
		return playerDataController;
	}
}
