package us.blockbox.shopui.listener;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.item.ItemInfo;
import net.milkbowl.vault.item.Items;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import us.blockbox.shopui.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static us.blockbox.shopui.ShopUI.log;
import static us.blockbox.shopui.ShopUI.prefix;
import static us.blockbox.shopui.locale.ShopMessage.Message.PLAYER_INVENTORY_FULL;
import static us.blockbox.shopui.locale.ShopMessage.getMessage;

public class ShopInteractListener implements Listener{

	private static final String formatBuy = prefix + "You bought %d %s for %s%s."; //todo localization
	private static final String formatSell = prefix + "You sold %d %s for %s%s.";
	private static JavaPlugin plugin;
	private static Economy econ = ShopUI.getEcon();
	private static final EnumSet<ClickType> clicksValid = EnumSet.of(ClickType.LEFT,ClickType.SHIFT_LEFT,ClickType.RIGHT,ClickType.SHIFT_RIGHT);
	private static final String currencyName = econ.currencyNamePlural();
	private static final ShopConfig config = ShopConfig.getInstance();

	public ShopInteractListener(JavaPlugin plugin){
		ShopInteractListener.plugin = plugin;
	}


	//todo change player money all at once when they close shop?

	@EventHandler(ignoreCancelled = true)
	public void onInventoryDrag(InventoryDragEvent e){
		final String title = e.getView().getTitle();
		if(!ShopInventory.isShopInventory(title)){
			return;
		}

		final Set<Integer> clickedSlots = e.getRawSlots();
		final int shopSlots = e.getView().getTopInventory().getSize();
		for(final Integer i : clickedSlots){
			if(i < shopSlots){
				e.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryInteract(InventoryClickEvent e){
//		log.info(e.getAction().toString());
		final String title = e.getView().getTitle();
		if(!ShopInventory.isShopInventory(title)){
			return;
		}

		final Player p = (Player)e.getWhoClicked();
		final int clickedSlot = e.getRawSlot();
		final int shopSlots = e.getView().getTopInventory().getSize();

		if(clickedSlot < 0){
			return;
		}
//		System.out.println(clickedSlot + " " + shopSlots);
		if(ShopUser.shopUsers.contains(p)){
			e.setCancelled(true);
			return;
		}

		final InventoryAction action = e.getAction();

		if(action == InventoryAction.MOVE_TO_OTHER_INVENTORY){
			e.setCancelled(true);
		}else if(clickedSlot < shopSlots){
			e.setCancelled(true);
//			return;
		}else{
			return;
		}

		final ClickType click = e.getClick();
		if(!clicksValid.contains(click)){
			e.setCancelled(true);
			return;
		}

		final ItemStack stack = e.getCurrentItem();
		if(stack == null || stack.getType() == Material.AIR){
			return;
		}

		final String titleStripped = ChatColor.stripColor(title);
		if(title.endsWith(ShopInventory.getShopSuffix())){
			shopInteract(click,clickedSlot,titleStripped,p);
			return;
		}

		menuInteract(p,stack);
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e){
		if(!e.getView().getTitle().endsWith(ShopInventory.getShopSuffix())){
			return;
		}
		final Player p = (Player)e.getPlayer();
		p.playSound(p.getLocation(),Sound.ITEM_ARMOR_EQUIP_GENERIC,2F,1.1F);
		new BukkitRunnable(){
			@Override
			public void run(){
				p.openInventory(ShopInventory.getShopMenu(p));
			}
		}.runTaskLater(plugin,1L);
	}

	private void shopInteract(final ClickType click,final int clickedSlot,final String title,final Player p){
//		final List<String> lore = item.getItemMeta().getLore();
		final ShopItem shopItem = getShopByTitle(title).get(clickedSlot);
		final double priceBuy = shopItem.getPriceBuy();
		final double priceSell = shopItem.getPriceSell();
		final ItemStack shopStack = shopItem.getItemStack().clone();
		final Inventory playerInv = p.getInventory();
		switch(click){
			case LEFT:{
				if(cannotFit(playerInv,shopStack)){
					p.sendMessage(getMessage(PLAYER_INVENTORY_FULL));
					soundDenied(p);
					break;
				}
				if(priceBuy < 0.01){
					soundDenied(p);
					break;
				}
				if(econ.getBalance(p) >= priceBuy){
					//final ShopItem selected = getShopByTitle(title).get(clickedSlot);
					//TODO good idea to use selected.getitemstack instead of shopstack?
					final HashMap<Integer,ItemStack> failed = playerInv.addItem(shopStack);
					if(!failed.isEmpty()){
						p.sendMessage(getMessage(PLAYER_INVENTORY_FULL));
						soundDenied(p);
						break;
					}
					runWithProperSync(new VaultTransactionTask(p,-priceBuy,shopStack.getType()));
					p.sendMessage(String.format(formatBuy,shopStack.getAmount(),getFriendlyName(shopStack),fmt(priceBuy),currencyName));
					soundSuccess(p);
				}else{
					soundDenied(p);
				}
				break;
			}
			case SHIFT_LEFT:{
				if(cannotFit(playerInv,shopStack)){
					p.sendMessage(getMessage(PLAYER_INVENTORY_FULL));
					soundDenied(p);
					break;
				}
				final int maxStack = shopStack.getMaxStackSize();
				final double bal = econ.getBalance(p);
				int amount = (int)Math.floor(bal / priceBuy) * shopItem.getQuantityDefault();
				if(amount == 0){
					soundDenied(p);
					break;
				}
				if(amount > maxStack){
					amount = maxStack;
				}
//				final double price = amount*(priceBuy/shopItem.getQuantityDefault());
				double price = BigDecimal.valueOf(amount)
						.multiply(BigDecimal.valueOf(priceBuy).divide(BigDecimal.valueOf(shopItem.getQuantityDefault()),3,RoundingMode.HALF_UP))
						.setScale(2,RoundingMode.HALF_UP)
						.doubleValue();
				shopStack.setAmount(amount);
				final HashMap<Integer,ItemStack> failed = playerInv.addItem(shopStack);
				if(!failed.isEmpty()){
					final int failedAmount = failed.get(0).getAmount();
					price -= failedAmount * (priceBuy / shopItem.getQuantityDefault());
					amount -= failedAmount;
				}
				if(price > 0){
					runWithProperSync(new VaultTransactionTask(p,-price,shopStack.getType()));
					p.sendMessage(String.format(formatBuy,amount,getFriendlyName(shopStack),fmt(price),currencyName));
					soundSuccess(p);
				}else{
					p.sendMessage(getMessage(PLAYER_INVENTORY_FULL));
					soundDenied(p);
				}
				break;
			}
			case RIGHT:{
				final int quantity = shopItem.getQuantityDefault();
				if(priceSell < 0.01 || !playerInv.containsAtLeast(shopStack,quantity)){
					soundDenied(p);
					break;
				}

/*				final Map<Enchantment,Integer> shopStackEnchantments = shopStack.getEnchantments();
				int amount = 0;
				for(Map.Entry<Integer,? extends ItemStack> iStack : playerInv.all(shopStack.getType()).entrySet()){
					if(iStack.getValue().getDurability() == shopStack.getDurability() && iStack.getValue().getEnchantments().equals(shopStackEnchantments)){
						amount += iStack.getValue().getAmount();
					}
					if(amount >= quantity){
						amount = quantity;
						break;
					}
				}
				if(amount < quantity){
					soundDenied(p);
					break;
				}*/

				if(playerInv.removeItem(shopStack).isEmpty()){
					runWithProperSync(new VaultTransactionTask(p,priceSell,shopStack.getType()));
					p.sendMessage(String.format(formatSell,quantity,getFriendlyName(shopStack),fmt(priceSell),currencyName));
					soundSuccess(p);
				}else{
					soundDenied(p);
				}
				break;
			}
			case SHIFT_RIGHT:{
				if(priceSell <= 0){
					soundDenied(p);
					break;
				}
				final int maxStack = shopStack.getMaxStackSize();
				int amount = 0;
				final Map<Enchantment,Integer> shopStackEnchantments = shopStack.getEnchantments();
				for(Map.Entry<Integer,? extends ItemStack> iStack : playerInv.all(shopStack.getType()).entrySet()){
					if(iStack.getValue().getDurability() == shopStack.getDurability() && iStack.getValue().getEnchantments().equals(shopStackEnchantments)){
						amount += iStack.getValue().getAmount();
					}
					if(amount >= maxStack){
						amount = maxStack;
						break;
					}
				}
				if(amount <= 0){
					soundDenied(p);
					break;
				}

				shopStack.setAmount(amount);
				if(playerInv.removeItem(shopStack).isEmpty()){
					double total = BigDecimal.valueOf(priceSell).multiply(BigDecimal.valueOf(amount)).divide(BigDecimal.valueOf(shopItem.getQuantityDefault()),RoundingMode.FLOOR).doubleValue();
					runWithProperSync(new VaultTransactionTask(p,total,shopStack.getType()));
					p.sendMessage(String.format(formatSell,amount,getFriendlyName(shopStack),fmt(total),currencyName));
					soundSuccess(p);
				}else{
					plugin.getLogger().warning("Failed transaction: " + p.getName() + " " + shopStack.getType().toString());
					soundDenied(p);
				}
				break;
			}
			default:{
			}
		}

		if(ShopUser.shopUsers.add(p)){
			new BukkitRunnable(){
				@Override
				public void run(){
					ShopUser.shopUsers.remove(p);
				}
			}.runTaskLater(plugin,3L);
		}
	}

	private void menuInteract(Player p,ItemStack item){
		final Inventory inv = ShopInventory.getShopInventory(ChatColor.stripColor(item.getItemMeta().getDisplayName()));
		if(inv != null){
			if(config.debugEnabled()){
				log.info("Opening inventory " + item.getItemMeta().getDisplayName() + " for " + p.getName());
			}
			p.openInventory(inv);
			p.playSound(p.getLocation(),Sound.ITEM_ARMOR_EQUIP_GENERIC,2F,1.12F);
		}
	}

	private static void soundDenied(Player p){
		p.playSound(p.getLocation(),Sound.BLOCK_NOTE_HAT,2F,1.88F);
	}

	private static void soundSuccess(Player p){
		p.playSound(p.getLocation(),Sound.ENTITY_EXPERIENCE_ORB_PICKUP,2F,1.18F);
	}

	public static List<ShopItem> getShopByTitle(String title){
		return config.shopItems.get(config.shopCategories.get(title).getShopId());
	}

	private static String prettyName(String item){
		return WordUtils.capitalizeFully(item.replace('_',' '));
	}

	private static String getFriendlyName(ItemStack stack){
		final ItemInfo itemInfo = Items.itemByStack(stack);
		return ((itemInfo == null) ? prettyName(stack.getType().toString()) : itemInfo.getName());
	}

	private boolean cannotFit(final Inventory inventory,final ItemStack itemStack){
		if(inventory.firstEmpty() != -1){
			return false;
		}
		if(!inventory.containsAtLeast(itemStack,1)){
			if(config.debugEnabled()){
				log.info(itemStack.getType().toString() + " cannot fit in inventory, no similar stacks already present.");
			}
			return true;
		}
		//TODO quicker way that wouldn't involve putting items in player's inventory?
		for(ItemStack invStack : inventory.getContents()){
			if(invStack == null || !invStack.isSimilar(itemStack)){
				continue;
			}
			if(invStack.getAmount() + itemStack.getAmount() <= itemStack.getMaxStackSize()){
				if(config.debugEnabled()){
					log.info(itemStack.getType().toString() + " can fit in inventory, similar stack present in inventory has enough space for items.");
				}
				return false;
			}
		}
		if(config.debugEnabled()){
			log.info(itemStack.getType().toString() + " cannot fit in inventory, similar stacks are present but none have enough space.");
		}
		return true;
	}

	private static String prettyNumber(BigDecimal number){
		return (number.remainder(BigDecimal.ONE).equals(BigDecimal.ZERO) ? String.valueOf(number.intValue()) : number.toPlainString());
	}

	public static String fmt(double d){
		return d == (long)d ? String.format("%d",(long)d) : String.format("%s",d);
	}

	public void runWithProperSync(BukkitRunnable runnable){
		if(config.isAsyncEnabled()){
			runnable.runTaskAsynchronously(plugin);
		}else{
			runnable.runTask(plugin);
		}
	}
}
