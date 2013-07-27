package com.aethercraft.factionsregen

import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import scala.util.Random
import com.massivecraft.mcore.ps.PS
import com.massivecraft.factions.entity.{Faction, BoardColls}
import org.bukkit.Server
import org.bukkit.entity.{Player, EntityType, Entity}

class Plugin extends JavaPlugin {

  override def onEnable() {
    RegenTask.runTaskTimer(this, 0, 30*20)
  }

  override def onDisable() {
    RegenTask.cancel()
  }

  object RegenTask extends BukkitRunnable {
    val server: Server = Plugin.this.getServer; import server._
    def run() {
      val chunkX = Random.nextInt(255) - 128
      val chunkZ = Random.nextInt(255) - 128
      val centerPS = PS.valueOf("factions", chunkX, chunkZ)
      val prefix = f"FactionsRegen: ($chunkX%4d, $chunkZ%4d):"
      if (centerPS.asBukkitChunk(true).isLoaded) {
        broadcast(s"$prefix isLoaded skipped", "factionsregen.trace")
      } else {
        val border = List( (-1, 1), (0, 1), (1, 1),
                           (-1, 0), (0, 0), (1, 0),
                           (-1,-1), (0,-1), (1,-1) )
        val pses = border.map{case (x, z) => centerPS.plusChunkCoords(x,z)}
        val factions: List[Faction] = pses.map(BoardColls.get().getFactionAt)
        if (factions.exists(_.isNormal)) {
          broadcast(s"$prefix near ${factions.toSet[Faction].map{_.getName}.mkString(", ")}§F skipped", "factionsregen.trace")
        } else {
          val players: List[Player] = pses.flatMap(_.asBukkitChunk(true).getEntities.collect{ case p: Player => p})
          if (!players.isEmpty) {
            broadcast(s"$prefix, near ${players.map{_.getDisplayName}.mkString(", ")} skipped", "factionsregen.trace")
          } else {
            val start = System.nanoTime()
            val chunk = centerPS.asBukkitChunk
            chunk.getWorld.regenerateChunk(chunk.getX, chunk.getZ)
            broadcast(f"$prefix took (${(System.nanoTime - start) / 1000000000.0}%3.3fs)", "factionsregen.trace")
          }
        }
      }
    }
  }
}
