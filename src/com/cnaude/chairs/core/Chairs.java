package com.cnaude.chairs.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.cnaude.chairs.api.APIInit;
import com.cnaude.chairs.commands.ChairsCommand;
import com.cnaude.chairs.listeners.NANLoginListener;
import com.cnaude.chairs.listeners.TrySitEventListener;
import com.cnaude.chairs.listeners.TryUnsitEventListener;
import com.cnaude.chairs.sitaddons.ChairEffects;
import com.cnaude.chairs.sitaddons.CommandRestrict;
import com.cnaude.chairs.vehiclearrow.NMSAccess;

public class Chairs extends JavaPlugin {

	public final HashSet<UUID> sitDisabled = new HashSet<>();
	public final HashMap<Material, Double> validChairs = new HashMap<>();
	public final List<Material> validSigns = new ArrayList<Material>();
	public final HashSet<String> sitDisabledCommands = new HashSet<>();
	public final HashSet<String> disabledWorlds = new HashSet<>();
	public boolean autoRotate, signCheck, notifyplayer;
	public boolean ignoreIfBlockInHand;
	public double distance;
	public int maxChairWidth;
	public boolean sitHealEnabled;
	public int sitMaxHealth;
	public int sitHealthPerInterval;
	public int sitHealInterval;
	public boolean sitPickupEnabled;
	public boolean sitDisableAllCommands = false;
	public String msgSitting, msgStanding, msgOccupied, msgNoPerm, msgReloaded, msgDisabled, msgEnabled, msgCommandRestricted;


	private PlayerSitData psitdata;
	public PlayerSitData getPlayerSitData() {
		return psitdata;
	}
	private NMSAccess nmsaccess = new NMSAccess();
	protected NMSAccess getNMSAccess() {
		return nmsaccess;
	}


	public ChairEffects chairEffects;

	@Override
	public void onEnable() {
		log = this.getLogger();
		try {
			nmsaccess.setupChairsArrow();
		} catch (Exception e) {
			e.printStackTrace();
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		chairEffects = new ChairEffects(this);
		psitdata = new PlayerSitData(this);
		getConfig().options().copyDefaults(true);
		saveConfig();
		loadConfig();
		if (sitHealEnabled) {
			chairEffects.startHealing();
		}
		if (sitPickupEnabled) {
			chairEffects.startPickup();
		}
		getServer().getPluginManager().registerEvents(new NANLoginListener(), this);
		getServer().getPluginManager().registerEvents(new TrySitEventListener(this), this);
		getServer().getPluginManager().registerEvents(new TryUnsitEventListener(this), this);
		getServer().getPluginManager().registerEvents(new CommandRestrict(this), this);
		getCommand("chairs").setExecutor(new ChairsCommand(this));
		new APIInit().initAPI(getPlayerSitData());
	}

	@Override
	public void onDisable() {
		if (psitdata != null) {
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (psitdata.isSitting(player)) {
					psitdata.unsitPlayerForce(player);
				}
			}
		}
		if (chairEffects != null) {
			chairEffects.cancelHealing();
			chairEffects.cancelPickup();
			chairEffects = null;
		}
		log = null;
		nmsaccess = null;
		psitdata = null;
		saveSitDisabled();
	}

	public void loadConfig() {
		FileConfiguration config = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(),"config.yml"));
		autoRotate = config.getBoolean("auto-rotate");
		signCheck = config.getBoolean("sign-check");
		distance = config.getDouble("distance");
		maxChairWidth = config.getInt("max-chair-width");
		notifyplayer = config.getBoolean("notify-player");
		ignoreIfBlockInHand = config.getBoolean("ignore-if-item-in-hand");

		sitHealEnabled = config.getBoolean("sit-effects.healing.enabled", false);
		sitHealInterval = config.getInt("sit-effects.healing.interval",20);
		sitMaxHealth = config.getInt("sit-effects.healing.max-percent",100);
		sitHealthPerInterval = config.getInt("sit-effects.healing.amount",1);

		sitPickupEnabled = config.getBoolean("sit-effects.itempickup.enabled", false);

		sitDisableAllCommands = config.getBoolean("sit-restrictions.commands.all");
		sitDisabledCommands.clear();
		sitDisabledCommands.addAll(config.getStringList("sit-restrictions.commands.list"));

		disabledWorlds.clear();
		disabledWorlds.addAll(config.getStringList("disabled-worlds"));

		msgSitting = ChatColor.translateAlternateColorCodes('&',config.getString("messages.sitting"));
		msgStanding = ChatColor.translateAlternateColorCodes('&',config.getString("messages.standing"));
		msgOccupied = ChatColor.translateAlternateColorCodes('&',config.getString("messages.occupied"));
		msgNoPerm = ChatColor.translateAlternateColorCodes('&',config.getString("messages.no-permission"));
		msgEnabled = ChatColor.translateAlternateColorCodes('&',config.getString("messages.enabled"));
		msgDisabled = ChatColor.translateAlternateColorCodes('&',config.getString("messages.disabled"));
		msgReloaded = ChatColor.translateAlternateColorCodes('&',config.getString("messages.reloaded"));
		msgCommandRestricted = ChatColor.translateAlternateColorCodes('&',config.getString("messages.command-restricted"));

		validChairs.clear();
		for (String s : config.getStringList("sit-blocks")) {
			String type;
			double sh = 0.7;
			String tmp[] = s.split("[:]");
			type = tmp[0];
			if (tmp.length == 2) {
				sh = Double.parseDouble(tmp[1]);
			}
			Material mat = Material.matchMaterial(type);
			if (mat != null) {
				logInfo("Allowed block: " + mat.toString() + " => " + sh);
				validChairs.put(mat, sh);
			} else {
				logError("Invalid block: " + type);
			}
		}

		validSigns.clear();
		for (String type : config.getStringList("valid-signs")) {
			try {
				validSigns.add(Material.matchMaterial(type));
			} catch (Exception e) {
				logError(e.getMessage());
			}
		}

		try {
			File sitDisabledFile = new File(getDataFolder(), "sit-disabled.txt");
			if (sitDisabledFile.exists()) {
				String line = null;
				sitDisabled.clear();
				BufferedReader br = new BufferedReader(new FileReader(sitDisabledFile));
				while ((line = br.readLine()) != null) {
					try {
						sitDisabled.add(UUID.fromString(line));
					} catch (IllegalArgumentException e) {
						/* Not a UUID */
					}
				}
				br.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveSitDisabled() {
		try {
			File sitDisabledFile = new File(getDataFolder(), "sit-disabled.txt");
			if (!sitDisabledFile.exists())
				sitDisabledFile.createNewFile();
			PrintWriter writer = new PrintWriter(sitDisabledFile, "UTF-8");
			writer.println("# The following players disabled Chairs for themselves");
			for (UUID uuid : sitDisabled)
				writer.println(uuid.toString());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Logger log;

	public void logInfo(String _message) {
		log.log(Level.INFO, _message);
	}

	public void logError(String _message) {
		log.log(Level.SEVERE, _message);
	}

}
