package us.blockbox.shopui;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

public class VaultTransactionTask extends BukkitRunnable{

	private static final Economy econ = ShopUI.getEcon();
	private OfflinePlayer player;
	private double money;
	private static final ShopTransactionLogger logger = new ShopTransactionLogger("transactions");
	private final Material material;

	public VaultTransactionTask(OfflinePlayer player,double money,Material material){
		this.player = player;
		this.money = money;
		this.material = material;
	}

	@Override
	public void run(){
/*		if(Math.abs(money) >= 64){
			Bukkit.getLogger().warning("[ShopUI] Large transaction: " + player.getName() + " bought " + material.toString() + " for " + money);
		}*/
		if(money > 0){
			final EconomyResponse response = econ.depositPlayer(player,money);
			if(!response.transactionSuccess()){
				ShopUI.log.info(response.errorMessage);
			}
		}else if(money < 0){
			final EconomyResponse response = econ.withdrawPlayer(player,-money);
			if(!response.transactionSuccess()){
				ShopUI.log.info(response.errorMessage);
			}
		}
		logger.logToFile(player.getName() + "," + material.toString() + "," + money);
	}
}
