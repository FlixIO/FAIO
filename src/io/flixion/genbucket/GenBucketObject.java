package io.flixion.genbucket;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;

import io.flixion.main.FAIOPlugin;

public class GenBucketObject {
	int taskID;
	
	public boolean checkFactionZone(Location loc, FPlayer p) {
		if (!Board.getInstance().getFactionAt(new FLocation(loc)).getId().equals(p.getFactionId())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public void initHorizontalGenProcess(Location loc, Material m, BlockFace f, FPlayer p) {
		
		taskID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(FAIOPlugin.getInstance(), new Runnable() {
			int coord = 1;
			@Override
			public void run() {
				if (coord == 33) {
					Bukkit.getScheduler().cancelTask(taskID);
				}
				if (f == BlockFace.SOUTH) { //Increasing Z-Coord
					if (!checkFactionZone(new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ() + coord), p)) {
						if (Bukkit.getWorld(loc.getWorld().getName()).getBlockAt(new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ() + coord)).getType() == Material.AIR) {
							Bukkit.getWorld(loc.getWorld().getName()).getBlockAt(new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ() + coord)).setType(m);
							
							coord++;
						}
						else {
							Bukkit.getScheduler().cancelTask(taskID);
						}
					}
					else {
						Bukkit.getScheduler().cancelTask(taskID);
					}
				}
				else if (f == BlockFace.NORTH) { //Decreasing Z-Coord
					if (!checkFactionZone(new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ() - coord), p)) {
						if (Bukkit.getWorld(loc.getWorld().getName()).getBlockAt(new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ() - coord)).getType() == Material.AIR) {
							Bukkit.getWorld(loc.getWorld().getName()).getBlockAt(new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ() - coord)).setType(m);
							
							coord++;
						}
						else {
							Bukkit.getScheduler().cancelTask(taskID);
						}
					}
					else {
						Bukkit.getScheduler().cancelTask(taskID);
					}
				}
				else if (f == BlockFace.WEST) { //Decreasing X-Coord
					if (!checkFactionZone(new Location(loc.getWorld(), loc.getX() - coord, loc.getY(), loc.getZ()), p)) {
						if (Bukkit.getWorld(loc.getWorld().getName()).getBlockAt(new Location(loc.getWorld(), loc.getX() - coord, loc.getY(), loc.getZ())).getType() == Material.AIR) {
							Bukkit.getWorld(loc.getWorld().getName()).getBlockAt(new Location(loc.getWorld(), loc.getX() - coord, loc.getY(), loc.getZ())).setType(m);
							
							coord++;
						}
						else {
							Bukkit.getScheduler().cancelTask(taskID);
						}
					}
					else {
						Bukkit.getScheduler().cancelTask(taskID);
					}
				}
				else if (f == BlockFace.EAST) { //Increasing X-Coord
					if (!checkFactionZone(new Location(loc.getWorld(), loc.getX() + coord, loc.getY(), loc.getZ()), p)) {
						if (Bukkit.getWorld(loc.getWorld().getName()).getBlockAt(new Location(loc.getWorld(), loc.getX() + coord, loc.getY(), loc.getZ())).getType() == Material.AIR) {
							Bukkit.getWorld(loc.getWorld().getName()).getBlockAt(new Location(loc.getWorld(), loc.getX() + coord, loc.getY(), loc.getZ())).setType(m);
							
							coord++;
						}
						else {
							Bukkit.getScheduler().cancelTask(taskID);
						}
					}
					else {
						Bukkit.getScheduler().cancelTask(taskID);
					}
				}
			}
		}, 0, 20/GenBucket.getBlocksPerSecond());
	}
	
	public void initVerticalGenProcess(Location loc, Material m) {
		taskID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(FAIOPlugin.getInstance(), new Runnable() {
			int yLevel = loc.getBlockY() - 1;
			
			@Override
			public void run() {
				if (m == Material.SAND) {
//					if (Bukkit.getWorld(loc.getWorld().getName()).getBlockAt(new Location(loc.getWorld(), loc.getX(), loc.getY() - 1, loc.getZ())).getType() != Material.SAND) {
//						Bukkit.getWorld(loc.getWorld().getName()).getBlockAt(new Location(loc.getWorld(), loc.getX(), loc.getY() - 1, loc.getZ())).setType(m);
//					}
//					else {
//						Bukkit.getScheduler().cancelTask(taskID);
//					}
					if (Bukkit.getWorld(loc.getWorld().getName()).getBlockAt(new Location(loc.getWorld(), loc.getX(), yLevel - 1, loc.getZ())).getType() == Material.AIR) {
						Bukkit.getWorld(loc.getWorld().getName()).getBlockAt(new Location(loc.getWorld(), loc.getX(), yLevel, loc.getZ())).setType(m);
						Bukkit.getWorld(loc.getWorld().getName()).getBlockAt(new Location(loc.getWorld(), loc.getX(), yLevel - 1, loc.getZ())).setType(Material.COBBLESTONE);
						yLevel--;
					}
					else {
						Bukkit.getScheduler().cancelTask(taskID);
						Bukkit.getWorld(loc.getWorld().getName()).getBlockAt(new Location(loc.getWorld(), loc.getX(), yLevel, loc.getZ())).setType(Material.SAND);
					}
				}
				else {
					if (Bukkit.getWorld(loc.getWorld().getName()).getBlockAt(new Location(loc.getWorld(), loc.getX(), yLevel, loc.getZ())).getType() == Material.AIR) {
						Bukkit.getWorld(loc.getWorld().getName()).getBlockAt(new Location(loc.getWorld(), loc.getX(), yLevel, loc.getZ())).setType(m);
						
						yLevel--;
					}
					else {
						Bukkit.getScheduler().cancelTask(taskID);
					}
				}
			}
			
		}, 0, (int) 20/GenBucket.getBlocksPerSecond());
	}
}
