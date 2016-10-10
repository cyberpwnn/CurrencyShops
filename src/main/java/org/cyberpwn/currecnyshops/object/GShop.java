package org.cyberpwn.currecnyshops.object;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.cyberpwn.currencyshops.CurrencyShops;
import org.phantomapi.clust.AsyncConfig;
import org.phantomapi.clust.AsyncConfigurable;
import org.phantomapi.clust.ColdLoad;
import org.phantomapi.clust.Comment;
import org.phantomapi.clust.Configurable;
import org.phantomapi.clust.DataCluster;
import org.phantomapi.clust.DataCluster.ClusterDataType;
import org.phantomapi.clust.Keyed;
import org.phantomapi.gui.Click;
import org.phantomapi.gui.Element;
import org.phantomapi.gui.PhantomElement;
import org.phantomapi.gui.PhantomWindow;
import org.phantomapi.gui.Slot;
import org.phantomapi.gui.Window;
import org.phantomapi.inventory.PhantomPlayerInventory;
import org.phantomapi.lang.GList;
import org.phantomapi.lang.GMap;
import org.phantomapi.lang.GSound;
import org.phantomapi.sync.TaskLater;
import org.phantomapi.util.C;
import org.phantomapi.util.F;
import org.phantomapi.world.MaterialBlock;
import org.phantomapi.world.W;

@AsyncConfig
@ColdLoad
public class GShop implements Shop, Configurable, AsyncConfigurable
{
	private DataCluster cc;
	private String name;
	private Boolean loaded;
	private GMap<Integer, Integer> sizes;
	
	@Comment("This is the Display name of the shop.\nSupports color codes")
	@Keyed("shop.display-name")
	public String displayName = "&aSome Shop";
	
	@Comment("This is the currency file name in the currencies folder. \nDO NOT add .yml to the end of it.")
	@Keyed("shop.currency")
	public String currency = "currency-file-name";
	
	@Comment("These are command aliases to launch the shop")
	@Keyed("shop.aliases")
	public GList<String> aliases = new GList<String>().qadd("shops");
	
	@Comment("This is the description of the shop")
	@Keyed("shop.description")
	public String description = "&aShop description";
	
	@Comment("This title of the window for the shop")
	@Keyed("shop.ui.title")
	public String windowTitle = "&aShop Title";
	
	@Comment("Allow the player to use different amounts.\nuse %amount% for the amount they selected.\nThe cost is calculated as COST x %amount%. Set the price of 1 item.")
	@Keyed("shop.ui.custom-count")
	public boolean customizeCount = false;
	
	@Keyed("shop.viewport")
	public int viewport = 9;
	
	public GShop(String name)
	{
		cc = new DataCluster();
		this.name = name;
		sizes = new GMap<Integer, Integer>();
		sizes.put(0, 1);
		sizes.put(1, 2);
		sizes.put(2, 4);
		sizes.put(3, 8);
		sizes.put(4, 16);
		sizes.put(5, 24);
		sizes.put(6, 32);
		sizes.put(7, 64);
		this.loaded = false;
	}
	
	@Override
	public void onNewConfig()
	{
		
	}
	
	@Override
	public void onReadConfig()
	{
		if(CurrencyShops.instance().getCurrencyController().get(currency) == null)
		{
			CurrencyShops.instance().getShopController().f("Cannot find currency file: " + currency);
		}
		
		loaded = true;
		CurrencyShops.instance().getShopCommandController().reregister(getName());
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
	public Currency getCurrency()
	{
		return CurrencyShops.instance().getCurrencyController().get(currency);
	}
	
	@Override
	public String getName()
	{
		return name;
	}
	
	public GList<String> getAliases()
	{
		return aliases;
	}
	
	@Override
	public String getDescription()
	{
		return description;
	}
	
	@Override
	public String getDisplayName()
	{
		return displayName;
	}
	
	public boolean hasItem(MaterialBlock mb)
	{
		if(!loaded)
		{
			return false;
		}
		
		return getCF(mb) != null;
	}
	
	public Double getBuy(MaterialBlock mb)
	{
		if(!loaded)
		{
			return null;
		}
		
		DataCluster cx = getCF(mb);
		
		if(cx != null)
		{
			return cx.getDouble("cost");
		}
		
		return null;
	}
	
	public Double getSell(MaterialBlock mb)
	{
		if(!loaded)
		{
			return null;
		}
		
		DataCluster cx = getCF(mb);
		
		if(cx != null)
		{
			return cx.getDouble("sell");
		}
		
		return null;
	}
	
	@SuppressWarnings("deprecation")
	public void trySellAll(Player p)
	{
		if(!loaded)
		{
			p.sendMessage(F.color("&8&l(&c&l!&8&l) &cStill Loading. Try again in a few seconds."));
			
			return;
		}
		
		double payment = 0;
		GMap<Material, Integer> amtx = new GMap<Material, Integer>();
		
		for(ItemStack i : p.getInventory().getContents())
		{
			if(i == null)
			{
				continue;
			}
			
			DataCluster cx = getCF(new MaterialBlock(i.getType(), i.getData().getData()));
			
			if(cx != null)
			{
				Double sell = cx.getDouble("sell") != null ? cx.getDouble("sell") : 0;
				
				if(sell > 0)
				{
					double pxp = sell * i.getAmount();
					payment += pxp;
					
					p.getInventory().remove(i.clone());
					p.updateInventory();
					
					if(!amtx.containsKey(i.getType()))
					{
						amtx.put(i.getType(), 0);
					}
					
					amtx.put(i.getType(), amtx.get(i.getType()) + i.getAmount());
				}
			}
		}
		
		if(payment > 0)
		{
			try
			{
				new Transaction(getCurrency(), payment).to(p).commit();
				
				for(Material i : amtx.k())
				{
					p.sendMessage(F.color("&8&l(&6&l!&8&l) &6Sold " + amtx.get(i) + "x " + i.toString().toLowerCase().replaceAll("_", " ")) + " for $" + F.f((int)(amtx.get(i) * getSell(new MaterialBlock(i)))));
				}
			}
			
			catch(TransactionExeption e)
			{
				
			}
		}
		
		p.updateInventory();
	}
	
	@SuppressWarnings("deprecation")
	public void trySell(Player p)
	{
		if(!loaded)
		{
			p.sendMessage(F.color("&8&l(&c&l!&8&l) &cStill Loading. Try again in a few seconds."));
			
			return;
		}
		
		ItemStack is = p.getItemInHand();
		DataCluster cx = getCF(new MaterialBlock(is.getType(), is.getData().getData()));
		
		if(cx != null)
		{
			Double sell = cx.getDouble("sell") != null ? cx.getDouble("sell") : 0;
			
			if(sell > 0)
			{
				Double given = sell * is.getAmount();
				
				try
				{
					p.sendMessage(F.color("&8&l(&6&l!&8&l) &6Sold " + is.getAmount() + "x " + is.getType().toString().toLowerCase().replaceAll("_", " ")) + " for $" + F.f(given));
					p.setItemInHand(new ItemStack(Material.AIR));
					new Transaction(getCurrency(), given).to(p).commit();
				}
				
				catch(TransactionExeption e)
				{
					p.sendMessage(C.RED + e.getMessage());
				}
			}
			
			else
			{
				p.sendMessage(C.RED + "Item cannot be sold.");
			}
		}
		
		else
		{
			p.sendMessage(C.RED + "Item cannot be sold.");
		}
		
		p.updateInventory();
	}
	
	public DataCluster getCF(MaterialBlock mb)
	{
		if(!loaded)
		{
			return null;
		}
		
		for(String i : cc.getData().keySet())
		{
			if(i.endsWith(".material"))
			{
				if(cc.getType(i).equals(ClusterDataType.STRING))
				{
					if(W.getMaterialBlock(cc.getString(i)).equals(mb))
					{
						return cc.crop(i.replaceAll("\\.material", ""));
					}
				}
			}
		}
		
		return null;
	}
	
	public void launchShop(Player p)
	{
		if(!loaded)
		{
			p.sendMessage(F.color("&8&l(&c&l!&8&l) &cStill Loading. Try again in a few seconds."));
			
			return;
		}
		
		Window root = new PhantomWindow(F.color(windowTitle), p);
		root.setViewport(viewport);
		DataCluster ca = cc.crop("shop.ui.categories");
		Set<String> cats = new HashSet<String>();
		
		for(String i : new GList<String>(ca.getData().keySet()).removeDuplicates())
		{
			try
			{
				cats.add(i.split("\\.")[0]);
			}
			
			catch(Exception e)
			{
				
			}
		}
		
		for(String i : cats)
		{
			if(!i.contains("."))
			{
				Slot s = new Slot(0);
				DataCluster cca = ca.crop(i);
				DataCluster cci = cca.crop("items");
				
				s.setX(cca.getInt("x"));
				s.setY(cca.getInt("y"));
				
				Window ww = new PhantomWindow(F.color(cca.getString("name")), p);
				
				ww.addElement(new PhantomElement(Material.BARRIER, new Slot(4, 6), C.RED + "Back")
				{
					@Override
					public void onClick(Player p, Click c, Window w)
					{
						launchShop(p);
					}
				});
				Slot kx = new Slot(-4, 1);
				
				Set<String> catsxz = new HashSet<String>();
				GList<String> catsx = new GList<String>();
				GMap<Integer, String> cax = new GMap<Integer, String>();
				
				for(String j : new GList<String>(cci.getData().keySet()).removeDuplicates())
				{
					try
					{
						catsxz.add(j.split("\\.")[0]);
					}
					
					catch(Exception e)
					{
						
					}
				}
				
				for(String j : catsxz)
				{
					int index = cci.crop(j).getInt("relative");
					cax.put(index, j);
				}
				
				GList<Integer> ix = cax.k();
				ix.sort();
				
				for(Integer j : ix)
				{
					catsx.add(cax.get(j));
				}
				
				for(String j : catsx)
				{
					if(!j.contains("."))
					{
						DataCluster ccie = cci.crop(j);
						
						MaterialBlock mb = W.getMaterialBlock(ccie.getString("material"));
						
						if(mb == null)
						{
							p.sendMessage(C.RED + "What the fuck is " + ccie.getString("material") + "?");
							mb = new MaterialBlock(Material.RECORD_4);
						}
						
						Element e = new PhantomElement(mb.getMaterial(), mb.getData(), kx, F.color(ccie.getString("name")))
						{
							@Override
							public void onClick(Player p, Click c, Window w)
							{
								if(ccie.contains("required-slots"))
								{
									if(!customizeCount)
									{
										int req = ccie.getInt("required-slots");
										PhantomPlayerInventory pp = new PhantomPlayerInventory(p.getInventory());
										
										if(pp.getSlotsLeft() < req)
										{
											p.sendMessage(C.RED + "You need at least " + req + " empty slots to purchase this.");
											return;
										}
									}
								}
								
								try
								{
									if(customizeCount)
									{
										Window wix = new PhantomWindow("How much?", p);
										wix.setViewport(1);
										
										wix.addElement(new PhantomElement(Material.BARRIER, new Slot(4, 1), C.RED + "Back")
										{
											@Override
											public void onClick(Player p, Click c, Window w)
											{
												ww.open();
											}
										});
										
										MaterialBlock mb = W.getMaterialBlock(ccie.getString("material"));
										
										if(mb == null)
										{
											p.sendMessage(C.RED + "What the fuck is " + ccie.getString("material") + "?");
											mb = new MaterialBlock(Material.RECORD_4);
										}
										
										MaterialBlock mbc = mb;
										
										for(int k : sizes.k())
										{
											Element ex = new PhantomElement(mb.getMaterial(), mb.getData(), new Slot(k), F.color(ccie.getString("name")))
											{
												@Override
												public void onClick(Player p, Click c, Window w)
												{
													if(c.equals(Click.LEFT))
													{
														try
														{
															if(ccie.getDouble("cost") == 0)
															{
																p.sendMessage(C.RED + "This item cannot be purchased");
																return;
															}
															
															PhantomPlayerInventory pp = new PhantomPlayerInventory(p.getInventory());
															
															if(pp.getSlotsLeft() < 1)
															{
																p.sendMessage(C.RED + "You need at least 1 empty slot to purchase this.");
																return;
															}
															
															new Transaction(getCurrency(), ccie.getDouble("cost") * sizes.get(k)).from(p).commit();
															
															if(mbc.getMaterial().equals(Material.MOB_SPAWNER))
															{
																Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ss give " + p.getName() + " " + mbc.getData() + " " + sizes.get(k));
															}
															
															else
															{
																new TaskLater()
																{
																	public void run()
																	{
																		for(String i : ccie.getStringList("commands"))
																		{
																			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), i.replaceFirst("/", "").replaceAll("%amount%", "" + sizes.get(k)).replaceAll("%player%", p.getName()));
																		}
																	}
																};
															}
															
															try
															{
																Sound s = Sound.valueOf(ccie.getString("unlock-sound"));
																GSound sx = new GSound(s, 1f, 1f);
																sx.play(p.getLocation());
															}
															
															catch(Exception e)
															{
																
															}
														}
														
														catch(TransactionExeption e)
														{
															p.sendMessage(C.RED + e.getMessage());
														}
													}
													
													else if(c.equals(Click.RIGHT))
													{
														try
														{
															MaterialBlock mb = W.getMaterialBlock(ccie.getString("material"));
															
															if(mb == null)
															{
																p.sendMessage(C.RED + "What the fuck is " + ccie.getString("material") + "?");
																mb = new MaterialBlock(Material.RECORD_4);
															}
															
															if(ccie.getDouble("sell") < 0.1)
															{
																p.sendMessage(C.RED + "This item cannot be sold");
																return;
															}
															
															if(!W.has(p, mb, sizes.get(k)))
															{
																p.sendMessage(C.RED + "You dont have enough to sell " + sizes.get(k) + "x " + F.color(ccie.getString("name")));
															}
															
															else
															{
																new Transaction(getCurrency(), ccie.getDouble("sell") * sizes.get(k)).to(p).commit();
																
																new TaskLater()
																{
																	public void run()
																	{
																		MaterialBlock mb = W.getMaterialBlock(ccie.getString("material"));
																		
																		if(mb == null)
																		{
																			p.sendMessage(C.RED + "What the fuck is " + ccie.getString("material") + "?");
																			mb = new MaterialBlock(Material.RECORD_4);
																		}
																		
																		W.take(p, mb, sizes.get(k));
																	}
																};
																
																try
																{
																	Sound s = Sound.valueOf(ccie.getString("unlock-sound"));
																	GSound sx = new GSound(s, 1f, 1f);
																	sx.play(p.getLocation());
																}
																
																catch(Exception e)
																{
																	
																}
															}
														}
														
														catch(TransactionExeption e)
														{
															p.sendMessage(C.RED + e.getMessage());
														}
													}
												}
											};
											
											GList<String> lore = new GList<String>();
											
											for(String l : F.color(ccie.getStringList("lore")))
											{
												lore.add(l.replaceAll("%player%", p.getName()).replaceAll("%amount%", "" + sizes.get(k)).replaceAll("%cost%", "" + (sizes.get(k) * ccie.getDouble("cost") > 999 ? F.f((int) (sizes.get(k) * ccie.getDouble("cost"))) : sizes.get(k) * ccie.getDouble("cost"))).replaceAll("%sell%", "" + ((sizes.get(k) * ccie.getDouble("sell")) > 999 ? F.f((int) ((sizes.get(k) * ccie.getDouble("sell")))) : (sizes.get(k) * ccie.getDouble("sell")))));
											}
											
											ex.setText(lore);
											
											ex.setCount(sizes.get(k));
											
											wix.addElement(ex);
										}
										
										wix.open();
									}
									
									else
									{
										if(ccie.getDouble("cost") == 0)
										{
											p.sendMessage(C.RED + "This item cannot be purchased");
											return;
										}
										
										new Transaction(getCurrency(), ccie.getDouble("cost")).from(p).commit();
										
										new TaskLater()
										{
											public void run()
											{
												for(String i : ccie.getStringList("commands"))
												{
													Bukkit.dispatchCommand(Bukkit.getConsoleSender(), i.replaceFirst("/", "").replaceAll("%player%", p.getName()));
												}
											}
										};
										
										try
										{
											Sound s = Sound.valueOf(ccie.getString("unlock-sound"));
											GSound sx = new GSound(s, 1f, 1f);
											sx.play(p.getLocation());
										}
										
										catch(Exception e)
										{
											
										}
									}
								}
								
								catch(TransactionExeption e)
								{
									p.sendMessage(C.RED + e.getMessage());
								}
							}
						};
						
						GList<String> lore = new GList<String>();
						
						for(String k : F.color(ccie.getStringList("lore")))
						{
							lore.add(k.replaceAll("%player%", p.getName()).replaceAll("%cost%", "" + (ccie.getDouble("cost") > 999 ? F.f((int) (ccie.getDouble("cost").doubleValue())) : ccie.getDouble("cost"))).replaceAll("%sell%", "" + ((ccie.getDouble("sell")) > 999 ? F.f((int) ((ccie.getDouble("sell")).doubleValue())) : (ccie.getDouble("sell")))));
						}
						
						e.setText(lore);
						
						ww.addElement(e);
						
						kx = new Slot(kx.getSlot() + 1);
					}
				}
				
				MaterialBlock mb = W.getMaterialBlock(cca.getString("material"));
				
				if(mb == null)
				{
					p.sendMessage(C.RED + "What the fuck is " + cca.getString("material") + "?");
					mb = new MaterialBlock(Material.RECORD_4);
				}
				
				Element e = new PhantomElement(mb.getMaterial(), mb.getData(), s, F.color(cca.getString("name")))
				{
					@Override
					public void onClick(Player p, Click c, Window w)
					{
						ww.open();
					}
				};
				
				e.setText(F.color(cca.getStringList("lore")));
				root.addElement(e);
			}
		}
		
		root.open();
	}
	
	@Override
	public boolean isLoaded()
	{
		return loaded;
	}
}
