package us.blockbox.shopui;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import us.blockbox.uilib.component.AbstractItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import static us.blockbox.shopui.ShopConfig.getFormatBuy;
import static us.blockbox.shopui.locale.ShopMessage.Message.PLAYER_INVENTORY_FULL;
import static us.blockbox.shopui.locale.ShopMessage.getMessage;

public class ShopItemNew extends AbstractItem{
	private static Economy econ = ShopUI.getInstance().getEcon();
	public static final ShopConfig config = ShopConfig.getInstance();
	private static final ShopUI plugin = ShopUI.getInstance();
	private double priceBuy;
	private double priceSell;
	private int quantityDefault;

	public ShopItemNew(String name,String id,ItemStack stack,double priceBuy,double priceSell,int quantityDefault){
		super(name,id,stack);
		this.priceBuy = priceBuy;
		this.priceSell = priceSell;
		this.quantityDefault = quantityDefault;
	}

	public ShopItemNew(String name,String id,String description,ItemStack stack,double priceBuy,double priceSell,int quantityDefault){
		super(name,id,description,stack);
		this.priceBuy = priceBuy;
		this.priceSell = priceSell;
		this.quantityDefault = quantityDefault;
	}

	@Override
	public boolean select(Player p,ClickType clickType){
		final double priceBuy = getPriceBuy();
		final double priceSell = getPriceSell();
		final ItemStack shopStack = getItemStack();
		final Inventory playerInv = p.getInventory();
		switch(clickType){
			case LEFT:{
				if(Utils.cannotFit(playerInv,shopStack)){
					p.sendMessage(getMessage(PLAYER_INVENTORY_FULL));
					Utils.soundDenied(p);
					break;
				}
				if(priceBuy < 0.01){
					Utils.soundDenied(p);
					break;
				}
				if(econ.getBalance(p) >= priceBuy){
					//final ShopItem selected = getShopByTitle(title).get(clickedSlot);
					//TODO good idea to use selected.getitemstack instead of shopstack?
					final HashMap<Integer,ItemStack> failed = playerInv.addItem(shopStack);
					if(!failed.isEmpty()){
						p.sendMessage(getMessage(PLAYER_INVENTORY_FULL));
						Utils.soundDenied(p);
						break;
					}
					new VaultTransactionTask(p,-priceBuy,shopStack.getType()).runTaskAsynchronously(plugin);
					p.sendMessage(String.format(getFormatBuy(),shopStack.getAmount(),Utils.getFriendlyName(shopStack),Utils.fmt(priceBuy),ShopConfig.getCurrencyName()));
					Utils.soundSuccess(p);
				}else{
					Utils.soundDenied(p);
				}
				break;
			}
			case SHIFT_LEFT:{
				if(Utils.cannotFit(playerInv,shopStack)){
					p.sendMessage(getMessage(PLAYER_INVENTORY_FULL));
					Utils.soundDenied(p);
					break;
				}
				final int maxStack = shopStack.getMaxStackSize();
				final double bal = econ.getBalance(p);
				int amount = (int)Math.floor(bal / priceBuy) * this.getQuantityDefault();
				if(amount == 0){
					Utils.soundDenied(p);
					break;
				}
				if(amount > maxStack){
					amount = maxStack;
				}
//				final double price = amount*(priceBuy/shopItem.getQuantityDefault());
				double price = BigDecimal.valueOf(amount)
						.multiply(BigDecimal.valueOf(priceBuy).divide(BigDecimal.valueOf(this.getQuantityDefault()),3,RoundingMode.HALF_UP))
						.setScale(2,RoundingMode.HALF_UP)
						.doubleValue();
				shopStack.setAmount(amount);
				final HashMap<Integer,ItemStack> failed = playerInv.addItem(shopStack);
				if(!failed.isEmpty()){
					final int failedAmount = failed.get(0).getAmount();
					price -= failedAmount * (priceBuy / this.getQuantityDefault());
					amount -= failedAmount;
				}
				if(price > 0){
					new VaultTransactionTask(p,-price,shopStack.getType()).runTaskAsynchronously(plugin);
					p.sendMessage(String.format(getFormatBuy(),amount,Utils.getFriendlyName(shopStack),Utils.fmt(price),ShopConfig.getCurrencyName()));
					Utils.soundSuccess(p);
				}else{
					p.sendMessage(getMessage(PLAYER_INVENTORY_FULL));
					Utils.soundDenied(p);
				}
				break;
			}
			case RIGHT:{
				final int quantity = this.getQuantityDefault();
				if(priceSell < 0.01 || !playerInv.containsAtLeast(shopStack,quantity)){
					Utils.soundDenied(p);
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
					new VaultTransactionTask(p,priceSell,shopStack.getType()).runTaskAsynchronously(plugin);
					p.sendMessage(String.format(ShopConfig.getFormatSell(),quantity,Utils.getFriendlyName(shopStack),Utils.fmt(priceSell),ShopConfig.getCurrencyName()));
					Utils.soundSuccess(p);
				}else{
					Utils.soundDenied(p);
				}
				break;
			}
			case SHIFT_RIGHT:{
				if(priceSell <= 0){
					Utils.soundDenied(p);
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
					Utils.soundDenied(p);
					break;
				}

				shopStack.setAmount(amount);
				if(playerInv.removeItem(shopStack).isEmpty()){
					double total = BigDecimal.valueOf(priceSell).multiply(BigDecimal.valueOf(amount)).divide(BigDecimal.valueOf(this.getQuantityDefault()),RoundingMode.FLOOR).doubleValue();
					new VaultTransactionTask(p,total,shopStack.getType()).runTaskAsynchronously(plugin);
					p.sendMessage(String.format(ShopConfig.getFormatSell(),amount,Utils.getFriendlyName(shopStack),Utils.fmt(total),ShopConfig.getCurrencyName()));
					Utils.soundSuccess(p);
				}else{
					plugin.getLogger().warning("Failed transaction: " + p.getName() + " " + shopStack.getType().toString());
					Utils.soundDenied(p);
				}
				break;
			}
			default:{
			}
		}
		return false;
	}

	public double getPriceBuy(){
		return priceBuy;
	}

	public double getPriceSell(){
		return priceSell;
	}

	public int getQuantityDefault(){
		return quantityDefault;
	}

}
