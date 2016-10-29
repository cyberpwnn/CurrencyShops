package org.cyberpwn.currecnyshops.object;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.phantomapi.async.A;
import org.phantomapi.lang.GList;
import org.phantomapi.lang.GMap;
import org.phantomapi.util.C;
import org.phantomapi.util.D;
import org.phantomapi.world.MaterialBlock;

public class DupeScan
{
	private GShop shop;
	
	public DupeScan(GShop shop)
	{
		this.shop = shop;
	}
	
	@SuppressWarnings("deprecation")
	public GMap<MaterialBlock, Double> direct()
	{
		GMap<MaterialBlock, Double> map = new GMap<MaterialBlock, Double>();
		D d = new D("Dupe Direct");
		
		new A()
		{
			@Override
			public void async()
			{
				for(Material i : Material.values())
				{
					for(int j = 0; j < 127; j++)
					{
						MaterialBlock mb = new MaterialBlock(i, (byte) j);
						
						if(shop.hasItem(mb))
						{
							try
							{
								if(shop.getBuy(mb) < shop.getSell(mb))
								{
									if(shop.getBuy(mb) > 0 && shop.getSell(mb) > 0)
									{
										map.put(mb, shop.getBuy(mb));
										d.f(mb.getMaterial().toString() + ":" + mb.getData() + C.YELLOW + "(buy $" + shop.getBuy(mb) + ") " + C.RED + " < " + mb.getMaterial().toString() + ":" + mb.getData() + C.YELLOW + "(sell $" + shop.getSell(mb) + ")");
									}
								}
							}
							
							catch(Exception e)
							{
								
							}
							
							for(Recipe k : Bukkit.getServer().getRecipesFor(new ItemStack(mb.getMaterial(), 1, (short) 0, mb.getData())))
							{
								try
								{
									GList<ItemStack> isx = new GList<ItemStack>();
									
									if(k instanceof ShapedRecipe)
									{
										ShapedRecipe s = (ShapedRecipe) k;
										
										for(Character l : s.getIngredientMap().keySet())
										{
											isx.add(s.getIngredientMap().get(l));
										}
									}
									
									if(k instanceof ShapelessRecipe)
									{
										ShapelessRecipe s = (ShapelessRecipe) k;
										
										for(ItemStack is : s.getIngredientList())
										{
											isx.add(is);
										}
									}
									
									if(isx.isEmpty())
									{
										continue;
									}
									
									double buy = 0;
									
									for(ItemStack is : isx)
									{
										MaterialBlock mbx = new MaterialBlock(is.getType(), is.getData().getData());
										
										if(shop.hasItem(mbx))
										{
											buy += shop.getBuy(mbx);
										}
									}
									
									if(buy == 0.0)
									{
										continue;
									}
									
									if(shop.hasItem(mb) && buy < shop.getSell(mb))
									{
										GList<String> v = new GList<String>();
										map.put(mb, buy);
										
										for(ItemStack is : isx)
										{
											MaterialBlock mbx = new MaterialBlock(is.getType(), is.getData().getData());
											
											if(mbx.getData() == -1)
											{
												mbx.setData((byte) 0);
											}
											
											if(mbx.getData() != 0)
											{
												v.add(mbx.getMaterial().toString() + ":" + mbx.getData());
											}
											
											else
											{
												v.add(mbx.getMaterial().toString());
											}
										}
										
										d.f(v.toString(", ") + C.YELLOW + "(buy $" + buy + ") " + C.RED + " < " + mb.getMaterial().toString() + ":" + mb.getData() + C.YELLOW + "(sell $" + shop.getSell(mb) + ")");
									}
								}
								
								catch(Exception e)
								{
									
								}
							}
						}
					}
				}
				
				for(MaterialBlock i : map.k())
				{
					d.v(i.getMaterial() + " (sell $" + shop.getSell(i) + ")" + C.AQUA + " to sell for $" + map.get(i));
				}
				
				d.s("Done");
			}
		};
		
		return map;
	}
}
