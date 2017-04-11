package us.blockbox.shopui;

import net.milkbowl.vault.item.ItemInfo;
import net.milkbowl.vault.item.Items;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.logging.Logger;

public class Utils{
	private Utils(){
	}

	private static final Logger log = ShopUI.getInstance().getLogger();


	private static String prettyName(String item){
		return WordUtils.capitalizeFully(item.replace('_',' '));
	}

	public static String getFriendlyName(ItemStack stack){
		final ItemInfo itemInfo = Items.itemByStack(stack);
		return ((itemInfo == null) ? prettyName(stack.getType().toString()) : itemInfo.getName());
	}

	private static String prettyNumber(BigDecimal number){
		return (number.remainder(BigDecimal.ONE).equals(BigDecimal.ZERO) ? String.valueOf(number.intValue()) : number.toPlainString());
	}

	public static String fmt(double d){
		return d == (long)d ? String.format("%d",(long)d) : String.format("%s",d);
	}

	public static boolean cannotFit(final Inventory inventory,final ItemStack itemStack){
		if(inventory.firstEmpty() != -1){
			return false;
		}
		if(!inventory.containsAtLeast(itemStack,1)){
			if(ShopItemNew.getConfig().debugEnabled()){
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
				if(ShopItemNew.getConfig().debugEnabled()){
					log.info(itemStack.getType().toString() + " can fit in inventory, similar stack present in inventory has enough space for items.");
				}
				return false;
			}
		}
		if(ShopItemNew.getConfig().debugEnabled()){
			log.info(itemStack.getType().toString() + " cannot fit in inventory, similar stacks are present but none have enough space.");
		}
		return true;
	}

	public static void soundDenied(Player p){
		p.playSound(p.getLocation(),Sound.BLOCK_NOTE_HAT,2F,1.88F);
	}

	public static void soundSuccess(Player p){
		p.playSound(p.getLocation(),Sound.ENTITY_EXPERIENCE_ORB_PICKUP,2F,1.18F);
	}

	public static int nearestMultiple(int number,int round){
		return (int)Math.ceil((float)number / round) * round;
	}
}