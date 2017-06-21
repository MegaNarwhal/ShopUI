package us.blockbox.shopui;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ShopUserManager{
	private static final long DELAY_REMOVE = 3L;

	private ShopUserManager(){
	}

	private static ShopUserManager instance = new ShopUserManager();

	public static ShopUserManager getInstance(){
		return instance;
	}

	private final Set<UUID> users = new HashSet<>(Math.max(500,Bukkit.getMaxPlayers() * 2));

	public boolean isRecentUser(OfflinePlayer player){
		return users.contains(player.getUniqueId());
	}

	public boolean add(OfflinePlayer player){
		final UUID uuid = player.getUniqueId();
		if(users.add(uuid)){
			new BukkitRunnable(){
				@Override
				public void run(){
					users.remove(uuid);
				}
			}.runTaskLater(ShopUI.getInstance(),DELAY_REMOVE);
			return true;
		}
		return false;
	}
}