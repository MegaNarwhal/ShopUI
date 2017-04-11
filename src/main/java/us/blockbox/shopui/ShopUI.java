package us.blockbox.shopui;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.inventivetalent.update.spiget.SpigetUpdate;
import org.inventivetalent.update.spiget.UpdateCallback;
import org.inventivetalent.update.spiget.comparator.VersionComparator;
import us.blockbox.shopui.command.*;
import us.blockbox.shopui.listener.ShopInteractListener;
import us.blockbox.shopui.locale.ShopMessage;
import us.blockbox.shopui.tabcomplete.ShopUICompleter;

import java.util.logging.Logger;

import static us.blockbox.shopui.locale.ShopMessage.Message.OPEN_FAILED;
import static us.blockbox.shopui.locale.ShopMessage.getMessage;

public class ShopUI extends JavaPlugin{

	private static ShopUI instance = null;
	private Logger log;
	private Economy econ;
	@Override
	public void onEnable(){
		log = getLogger();
		if(instance == null){
			instance = this;
		}
		ShopConfig shopConfig = ShopConfig.getInstance();
		shopConfig.loadConfig();
		if(shopConfig.isUpdaterEnabled()){
			final SpigetUpdate updater = new SpigetUpdate(this,33864);
			updater.setVersionComparator(VersionComparator.EQUAL);
//			updater.setVersionComparator(VersionComparator.SEM_VER);
			updater.checkForUpdate(new UpdateCallback(){
				@Override
				public void updateAvailable(String newVersion,String downloadUrl,boolean hasDirectDownload){
					log.warning("An update is available! You're running " + getDescription().getVersion() + ", the latest version is " + newVersion + ".");
					log.warning(downloadUrl);
					log.warning("You can disable update checking in the config.yml.");
				}

				@Override
				public void upToDate(){
					log.info("You're running the latest version. You can disable update checking in the config.yml.");
				}
			});
		}
		getCommand("shop").setExecutor(new CommandShop());
		getCommand("shopui").setExecutor(new CommandShopUI());
		getCommand("shopui").setTabCompleter(new ShopUICompleter());
		SubCommandHandler sub = SubCommandHandler.getInstance();
		sub.addSubCommand("add",new CommandHeldItemAdd());
		sub.addSubCommand("list",new CommandListShops());
		sub.addSubCommand("create",new CommandCategory());

		if(setupEconomy()){
			log.info("Economy successfully hooked: " + econ.getName());
		}else{
			log.severe("Failed to hook economy. Disabling ShopUI.");
			Bukkit.getPluginManager().disablePlugin(this);
		}
		ShopMessage.loadMessages();
		getServer().getPluginManager().registerEvents(new ShopInteractListener(),this);
	}

	@Override
	public void onDisable(){
		for(final Player p : getServer().getOnlinePlayers()){
			final String title = p.getOpenInventory().getTitle();
			if(ShopInventory.isShopInventory(title)){
				log.info(p.getName() + " was using shop, closing inventory.");
				p.closeInventory();
				p.sendMessage(getMessage(OPEN_FAILED));
			}
		}
		ShopTransactionLogger.flushAllQueues();
	}

	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}

	public Economy getEcon(){
		return econ;
	}

	public static ShopUI getInstance(){
		return instance;
	}
}