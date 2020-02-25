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

import java.util.Date;
import java.util.Random;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

@AllArgsConstructor
@Getter
@ToString
public class Teleport {
	
	private static final Random RANDOM = new Random();

	private String worldName;
	private double centerX, centerZ;
	private double minRadius, maxRadius;
	private int minY, maxY;
	
	public void execute(Player player) {
		World world = Bukkit.getWorld(worldName);
		
		if (world == null) {
			player.sendMessage(ChatColor.RED + "Mondo \"" + worldName + "\" non trovato! Per favore informa lo staff.");
			return;
		}
		
		int tries = 0;

		while (true) {
			tries++;
			
			double randomX = centerX + plusOrMinus() * randomBetween(minRadius, maxRadius);
			double randomZ = centerZ + plusOrMinus() * randomBetween(minRadius, maxRadius);
			
			Block block = getSafeBlock(world, (int) randomX, (int) randomZ);
			
			if (block != null) {
				// Blocco sicuro trovato
				Location loc = block.getLocation().add(0.5, 1.5, 0.5);
				RandomTeleport.plugin.fileLogger.log(new Date().toString() + ": " + player.getName() + " teleported to " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ());
				player.teleport(loc);
				return;
			}
			
			if (tries >= 5) {
				player.sendMessage(ChatColor.RED + "Non è stato possibile trovare un luogo sicuro, riprova.");
				return;
			}
		}
	}
	
	private static double randomBetween(double min, double max) {
		return RANDOM.nextDouble() * (max - min) + min;
	}
	
	private static int plusOrMinus() {
		return RANDOM.nextBoolean() ? 1 : -1;
	}
	
	private Block getSafeBlock(World world, int x, int z) {
		
		int y = world.getMaxHeight() - 1;
		Block block = world.getBlockAt(x, y, z);
		
		while (y > 0 && isPassThrough(block.getType())) {
			
			if (minY != 0 && y < minY) {
				return null; // Siccome si scende sempre, la condizione è vera
			}
			
			y--;
			block = world.getBlockAt(x, y, z);
		}
		
		if (maxY != 0 && y > maxY) {
			return null;
		}
		
		if (isSafeSolid(block.getType())) {
			return block;
		}

		return null;
	}
	
	private boolean isSafeSolid(Material mat) {
		switch (mat) {
			case STONE:
			case GRASS:
			case DIRT:
			case COBBLESTONE:
			case WOOD:
			case BEDROCK:
			case SAND:
			case SANDSTONE:
			case RED_SANDSTONE:
			case COAL_ORE:
			case IRON_ORE:
			case SNOW_BLOCK:
			case SNOW:
			case MYCEL:
			case GRAVEL:
			case CLAY:
			case HARD_CLAY:
			case STAINED_CLAY:
			case PRISMARINE:
			case HUGE_MUSHROOM_1:
			case HUGE_MUSHROOM_2:
			case LOG:
			case LOG_2:
			case LEAVES:
			case LEAVES_2:
			case NETHERRACK:
			case QUARTZ_ORE:
			case SOUL_SAND:
			case ENDER_STONE:
			case OBSIDIAN:
				return true;
			default:
				return false;
		}
	}
	
	private boolean isPassThrough(Material mat) {
		switch (mat) {
			case AIR:
			case LONG_GRASS:
			case SAPLING:
			case DEAD_BUSH:
			case YELLOW_FLOWER:
			case RED_ROSE:
			case BROWN_MUSHROOM:
			case RED_MUSHROOM:
			case TORCH:
			case VINE:
				return true;
			default:
				return false;
		}
	}
	
}
