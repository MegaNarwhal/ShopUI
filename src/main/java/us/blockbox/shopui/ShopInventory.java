package us.blockbox.shopui;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

import static us.blockbox.shopui.listener.ShopInteractListener.fmt;
import static us.blockbox.shopui.listener.ShopInteractListener.getShopByTitle;

//Created 11/20/2016 4:32 AM
public class ShopInventory{

	private static final String shopSuffix = "§2§9§2";
	private static final String menuSuffix = "§2§9§3";
	private static final Economy econ = ShopUI.getEcon();
//	private static ItemStack[] shopMenu;
	private static Map<String,Inventory> shopInvCache = new HashMap<>();
	private static ShopConfig config = ShopConfig.getInstance();

	public static Inventory getShopInventory(String s){
		List<ShopItem> list = getShopByTitle(s);
		Inventory inv = Bukkit.createInventory(null,(int)(Math.ceil((float)list.size()/9)*9),s + shopSuffix);
		if(shopInvCache.containsKey(s)){
//			Bukkit.getLogger().info("loading from cache: " + s);
			inv = (shopInvCache.get(s));
		}else{
			int pos = 0;
			for(final ShopItem i : list){
				final ItemStack item = i.getItemStack().clone();
				final ItemMeta meta = item.getItemMeta();
				List<String> loreList = new ArrayList<>(Arrays.asList(ChatColor.GREEN + "Buy: " + ChatColor.WHITE + i.getPriceBuy() + ChatColor.GRAY + " (Left click)",ChatColor.GREEN + "Sell: " + ChatColor.WHITE + i.getPriceSell() + ChatColor.GRAY + " (Right click)"));
				meta.setLore(loreList);
				item.setItemMeta(meta);
				inv.setItem(pos,item);
				pos++;
			}
/*			int i = 0;
			final ItemStack[] temp = new ItemStack[inv.getSize()];
			for(final ItemStack itemStack : inv.getContents()){
				if(itemStack == null){
					break;
				}else{
					temp[i] = itemStack;
					i++;
				}
			}*/
			//shopInvCache.put(s,Arrays.copyOf(temp,i));

			shopInvCache.put(s,copyInventory(inv));
		}
		return inv;
	}

	public static Inventory getShopMenu(final OfflinePlayer player){
		Inventory inv = Bukkit.createInventory(null,(int)Math.ceil((float)config.shopCategories.size()/9)*9,"Shop (Money: " + fmt(econ.getBalance(player)) + ")" + menuSuffix);
/*		if(shopMenu != null){
			inv.addItem(shopMenu);
		}else{*/
			int pos = 0;
			for(final Map.Entry<String,ShopCategory> i : config.shopCategories.entrySet()){
				final ItemStack catItem = i.getValue().getItemStack().clone();
				final ItemMeta meta = catItem.getItemMeta();
				meta.setDisplayName(i.getValue().getShopNameColored());
				catItem.setItemMeta(meta);
				inv.setItem(pos,catItem);
				pos++;
			}
			//todo rework caching to prevent stacking of identical items
/*			int i = 0;
			final ItemStack[] temp = new ItemStack[inv.getSize()];
			for(final ItemStack itemStack : inv.getContents()){
				if(itemStack == null){
					break;
				}else{
					temp[i] = itemStack;
					i++;
				}
			}
			shopMenu = Arrays.copyOf(temp,i);*/
//		}
		return inv;
	}

	public static String getShopSuffix(){
		return shopSuffix;
	}

	public static String getMenuSuffix(){
		return menuSuffix;
	}

	public static boolean isShopInventory(final String title){
		return (title.endsWith(shopSuffix) || title.endsWith(menuSuffix));
	}

	private static Inventory copyInventory(Inventory inv){
		Inventory invCopy = Bukkit.createInventory(null,inv.getSize(),inv.getTitle());
		for(int i = 0; i < inv.getSize(); i++){
			ItemStack stack = inv.getItem(i);
			if(stack == null){
				continue;
			}
			invCopy.setItem(i,stack.clone());
		}
		return invCopy;
	}
}
