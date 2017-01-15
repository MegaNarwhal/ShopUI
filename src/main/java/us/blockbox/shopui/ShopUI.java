package us.blockbox.shopui;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import us.blockbox.shopui.command.*;
import us.blockbox.shopui.listener.ShopInteractListener;
import us.blockbox.shopui.locale.ShopMessage;
import us.blockbox.shopui.tabcomplete.ShopUICompleter;

import java.util.logging.Logger;

public class ShopUI extends JavaPlugin{

	public static Logger log;
	static ShopUI plugin;
	private static Economy econ;
	public static final String prefix = ChatColor.GREEN + "Shop" + ChatColor.DARK_GRAY + "> " + ChatColor.RESET;
	private final SubCommandHandler sub = SubCommandHandler.getInstance();

	@Override
	public void onEnable(){
		log = getLogger();
		plugin = this;
		ShopConfig shopConfig = ShopConfig.getInstance();
		shopConfig.loadConfig();
		getCommand("shop").setExecutor(new CommandShop());
		getCommand("shopui").setExecutor(new CommandShopUI());
		getCommand("shopui").setTabCompleter(new ShopUICompleter());
		sub.addSubCommand("add",new CommandHeldItemAdd());
		sub.addSubCommand("list",new CommandListShops());
		sub.addSubCommand("create",new CommandCategory());

		setupEconomy();

		getServer().getPluginManager().registerEvents(new ShopInteractListener(this),this);
	}

	@Override
	public void onDisable(){
		for(Player p : getServer().getOnlinePlayers()){
			final String title = p.getOpenInventory().getTitle();
			if(ShopInventory.isShopInventory(title)){
				log.info(p.getName() + " was using shop, closing inventory.");
				p.closeInventory();
				p.sendMessage(ShopMessage.OPEN_FAILED.getMsg());
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

	public static Economy getEcon(){
		return econ;
	}
}