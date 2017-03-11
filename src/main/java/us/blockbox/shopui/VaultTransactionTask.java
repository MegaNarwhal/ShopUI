package us.blockbox.shopui;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Logger;

public class VaultTransactionTask extends BukkitRunnable{

	private static final Economy econ = ShopUI.getInstance().getEcon();
	private OfflinePlayer player;
	private double money;
	private static final Logger log = ShopUI.getInstance().getLogger();
	private static final ShopTransactionLogger logger = new ShopTransactionLogger("transactions");
	private final Material material;
	private final ItemStack stack;

	@Deprecated
	public VaultTransactionTask(OfflinePlayer player,double money,Material material){
		this.player = player;
		this.money = money;
		this.material = material;
		this.stack = null;
	}

	public VaultTransactionTask(ItemStack stack){
		this.stack = stack;
		this.material = null;
	}

	@Override
	public void run(){
/*		if(Math.abs(money) >= 64){
			Bukkit.getLogger().warning("[ShopUI] Large transaction: " + player.getName() + " bought " + material.toString() + " for " + money);
		}*/
		if(money > 0){
			final EconomyResponse response = econ.depositPlayer(player,money);
			if(!response.transactionSuccess()){
				log.info(response.errorMessage);
			}
		}else if(money < 0){
			final EconomyResponse response = econ.withdrawPlayer(player,-money);
			if(!response.transactionSuccess()){
				log.info(response.errorMessage);
			}
		}
		logger.logToFile(player.getName() + "," + getItemInfo() + "," + money);
	}

	private String getItemInfo(){
		if(material == null){
			if(stack == null){
				return "ERROR_UNKNOWN";
			}else{
				StringBuilder info = new StringBuilder(stack.getType().name());
				if(stack.getDurability() != 0){
					info.append(":").append(stack.getDurability());
				}
				if(stack.hasItemMeta()){
					ItemMeta m = stack.getItemMeta();
					if(m.hasDisplayName()){
						info.append(" (").append(stack.getType().name()).append(")");
					}
				}
				return info.toString();
			}
		}else{
			return material.name();
		}
	}
}
