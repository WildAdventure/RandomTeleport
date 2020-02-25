/*
 * Copyright (c) 2020, Wild Adventure
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 * 4. Redistribution of this software in source or binary forms shall be free
 *    of all charges or fees to the recipient of this software.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gmail.filoghost.randomteleport;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import wild.api.util.FileLogger;

public class RandomTeleport extends JavaPlugin implements Listener {
	
	public static RandomTeleport plugin;
	
	private static final String REPLACEMENT = "[randomteleport]";
	private static final String TRIGGER = ChatColor.DARK_PURPLE + "[RandomTeleport]";
	
	private Map<String, Teleport> teleports;
	
	public FileLogger fileLogger = new FileLogger(this, "teleports.log");
	
	@Override
	public void onEnable() {
		if (!Bukkit.getPluginManager().isPluginEnabled("WildCommons")) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[" + this.getName() + "] Richiesto WildCommons!");
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) { }
			Bukkit.shutdown();
			return;
		}
		
		plugin = this;
		
		Bukkit.getPluginManager().registerEvents(this, this);
		new RTReloadCommand(this);
		
		load();
	}
	
	public void load() {
		teleports = new HashMap<>();
		
		saveDefaultConfig();
		reloadConfig();
		
		for (String key : getConfig().getKeys(false)) {
			ConfigurationSection section = getConfig().getConfigurationSection(key);
			
			String world = section.getString("world");
			double centerX = section.getDouble("center-x");
			double centerZ = section.getDouble("center-z");
			double radiusMin = section.getDouble("radius-min");
			double radiusMax = section.getDouble("radius-max");
			int minY = section.getInt("min-y");
			int maxY = section.getInt("max-y");
			
			if (world != null) {
				teleports.put(key, new Teleport(world, centerX, centerZ, radiusMin, radiusMax, minY, maxY));
			} else {
				getLogger().warning("Il random teleport '" + key + "' non ha un mondo!");
			}
		}
		
		getLogger().info("Caricati " + teleports.size() + " teletrasporti casuali: " + teleports.toString());
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onSignComplete(SignChangeEvent event) {
		if (event.getLine(0).equals(TRIGGER)) {
			event.setLine(0, ChatColor.stripColor(TRIGGER));
			return;
		}
		
		if (event.getLine(0).equalsIgnoreCase(REPLACEMENT)) {
			if (event.getPlayer().hasPermission("randomteleport.sign")) {
				
				if (!teleports.containsKey(event.getLine(1))) {
					event.getPlayer().sendMessage(ChatColor.RED + "Random teleport \"" + event.getLine(1).replace(ChatColor.COLOR_CHAR, '&') + "\" non trovato!");
					return;
				}
				
				event.setLine(0, TRIGGER);
				event.getPlayer().sendMessage(ChatColor.GREEN + "Random teleport creato!");
			}
		}
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onSignInteract(PlayerInteractEvent event) {

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && isSign(event.getClickedBlock().getType())) {

			Sign sign = (Sign) event.getClickedBlock().getState();

			if (sign.getLine(0).equals(TRIGGER)) {
				Teleport teleport = teleports.get(sign.getLine(1));
				if (teleport == null) {
					event.getPlayer().sendMessage(ChatColor.RED + "Teletrasporto non trovato, informa lo staff!");
					return;
				}

				teleport.execute(event.getPlayer());
			}
		}
	}
	
	private boolean isSign(Material mat) {
		return mat == Material.SIGN_POST || mat == Material.WALL_SIGN;
	}
	
}
