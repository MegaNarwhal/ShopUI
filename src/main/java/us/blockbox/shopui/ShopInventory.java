package us.blockbox.shopui;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

import static us.blockbox.shopui.Utils.fmt;
import static us.blockbox.shopui.listener.ShopInteractListener.getShopByTitle;

//Created 11/20/2016 4:32 AM
public class ShopInventory{

	private static final String shopSuffix = "§2§9§2";
	private static final String menuSuffix = "§2§9§3";
	private static final String menuTitleFormat = "Shop (Money: %s)" + menuSuffix;
	private static final Economy econ = ShopUI.getInstance().getEcon();
	private static final CachedObject<ItemStack[]> menuCache = new CachedObject<ItemStack[]>(){
		@Override
		protected void validate(){
			final ItemStack[] inv = new ItemStack[Utils.nearestMultiple(config.shopCategories.size(),9)];
			int pos = 0;
			for(final Map.Entry<String,ShopCategory> i : config.shopCategories.entrySet()){
				final ItemStack catItem = i.getValue().getItemStack().clone();
				final ItemMeta meta = catItem.getItemMeta();
				meta.setDisplayName(i.getValue().getShopNameColored());
				catItem.setItemMeta(meta);
				inv[pos] = catItem;
				pos++;
			}
			setValue(inv);
		}
	};
	private static Map<String,Inventory> shopInvCache = new HashMap<>();
	private static ShopConfig config = ShopConfig.getInstance();

	public static Inventory getShopInventory(String s){
		final List<ShopItem> list = getShopByTitle(s);
		final Inventory inv;
		if(shopInvCache.containsKey(s)){
			inv = (shopInvCache.get(s));
		}else{
			inv = Bukkit.createInventory(null,Utils.nearestMultiple(list.size(),9),s + shopSuffix);
			int pos = 0;
			for(final ShopItem i : list){
				final ItemStack item = i.getItemStack().clone();
				final ItemMeta meta = item.getItemMeta();
				final List<String> loreList = new ArrayList<>(Arrays.asList(ChatColor.GREEN + "Buy: " + ChatColor.WHITE + i.getPriceBuy() + ChatColor.GRAY + " (Left click)",ChatColor.GREEN + "Sell: " + ChatColor.WHITE + i.getPriceSell() + ChatColor.GRAY + " (Right click)"));
				meta.setLore(loreList);
				item.setItemMeta(meta);
				inv.setItem(pos,item);
				pos++;
			}
			shopInvCache.put(s,copyInventory(inv));
		}
		return inv;
	}

	public static Inventory getShopMenu(final OfflinePlayer player){
		return getShopMenu(String.format(menuTitleFormat,fmt(econ.getBalance(player))));
	}

	private static Inventory getShopMenu(String title){
		final ItemStack[] cache = menuCache.getValue();
		final Inventory inv = Bukkit.createInventory(null,cache.length,title);
		inv.setContents(cache);
		//todo is stacking of identical items still an issue?
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
		final Inventory invCopy = Bukkit.createInventory(null,inv.getSize(),inv.getTitle());
		invCopy.setContents(inv.getContents());
		return invCopy;
	}

/*	private static Inventory copyInventory(Inventory inv){
		Inventory invCopy = Bukkit.createInventory(null,inv.getSize(),inv.getTitle());
		for(int i = 0; i < inv.getSize(); i++){
			ItemStack stack = inv.getItem(i);
			if(stack == null){
				continue;
			}
			invCopy.setItem(i,stack.clone());
		}
		return invCopy;
	}*/
}