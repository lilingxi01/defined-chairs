package com.cnaude.chairs.core;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.cnaude.chairs.commands.ChairsCommand;
import com.cnaude.chairs.listeners.NANLoginListener;
import com.cnaude.chairs.listeners.TrySitEventListener;
import com.cnaude.chairs.listeners.TryUnsitEventListener;
import com.cnaude.chairs.sitaddons.ChairEffects;
import com.cnaude.chairs.sitaddons.CommandRestrict;

public class Chairs extends JavaPlugin {

	private static Chairs instance;

	public static Chairs getInstance() {
		return instance;
	}

	public Chairs() {
		instance = this;
	}

	private final ChairsConfig config = new ChairsConfig(this);
	public ChairsConfig getChairsConfig() {
		return config;
	}
	private final PlayerSitData psitdata = new PlayerSitData(this);
	public PlayerSitData getPlayerSitData() {
		return psitdata;
	}


	public final ChairEffects chairEffects = new ChairEffects(this);
	public final SitUtils utils = new SitUtils(this);

	@Override
	public void onEnable() {
		try {
			Files.copy(Chairs.class.getClassLoader().getResourceAsStream("config_help.txt"), new File(getDataFolder(), "config_help.txt").toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
		}
		reloadConfig();
		getServer().getPluginManager().registerEvents(new NANLoginListener(), this);
		getServer().getPluginManager().registerEvents(new TrySitEventListener(this), this);
		getServer().getPluginManager().registerEvents(new TryUnsitEventListener(this), this);
		getServer().getPluginManager().registerEvents(new CommandRestrict(this), this);
		getCommand("chairs").setExecutor(new ChairsCommand(this));
	}

	@Override
	public void onDisable() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (psitdata.isSitting(player)) {
				psitdata.unsitPlayerForce(player, true);
			}
		}
		chairEffects.cancelHealing();
		chairEffects.cancelPickup();
		saveSitDisabled();
	}

	@Override
	public void reloadConfig() {
		config.reloadConfig();
		if (config.effectsHealEnabled) {
			chairEffects.restartHealing();
		} else {
			chairEffects.cancelHealing();
		}
		if (config.effectsItemPickupEnabled) {
			chairEffects.restartPickup();
		} else {
			chairEffects.cancelPickup();
		}
	}

	protected void loadSitDisabled() {
		try {
			for (String line: Files.readAllLines(new File(getDataFolder(), "sit-disabled.txt").toPath())) {
				try {
					getPlayerSitData().disableSitting(UUID.fromString(line));
				} catch (IllegalArgumentException e) {
				}
			}
		} catch (IOException e) {
		}
	}

	protected void saveSitDisabled() {
		try {
			File sitDisabledFile = new File(getDataFolder(), "sit-disabled.txt");
			if (!sitDisabledFile.exists()) {
				sitDisabledFile.createNewFile();
			}
			try (PrintWriter writer = new PrintWriter(sitDisabledFile, "UTF-8")) {
				writer.println("# The following players disabled Chairs for themselves");
				for (UUID uuid : getPlayerSitData().sitDisabled) {
					writer.println(uuid.toString());
				}
			}
		} catch (IOException e) {
		}
	}

}
