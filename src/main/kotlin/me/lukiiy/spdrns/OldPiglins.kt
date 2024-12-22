package me.lukiiy.spdrns

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.PiglinBarterEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionType
import kotlin.random.Random

class OldPiglins : JavaPlugin(), Listener {
    private val trades = mutableListOf<Trade>()

    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)
        initTrades()
    }
    
    private fun initTrades() {
        // Soul speed book
        trades += Trade(5) {
            ItemStack(Material.ENCHANTED_BOOK).apply {
                itemMeta = (itemMeta as EnchantmentStorageMeta).apply { addStoredEnchant(Enchantment.SOUL_SPEED, Random.nextInt(1, 4), true) }
            }
        }

        // Soul speed boots
        trades += Trade(8) {
            ItemStack(Material.IRON_BOOTS).apply {
                itemMeta = (itemMeta as EnchantmentStorageMeta).apply { addEnchantment(Enchantment.SOUL_SPEED, Random.nextInt(1, 4)) }
            }
        }

        // Fire res (splash)
        trades += Trade(10) {
            ItemStack(Material.SPLASH_POTION).apply {
                itemMeta = (itemMeta as PotionMeta).apply { basePotionData = PotionData(PotionType.FIRE_RESISTANCE) }
            }
        }

        // Fire res
        trades += Trade(10) {
            ItemStack(Material.POTION).apply {
                itemMeta = (itemMeta as PotionMeta).apply { basePotionData = PotionData(PotionType.FIRE_RESISTANCE) }
            }
        }

        trades += listOf(
            Trade(10) { ItemStack(Material.IRON_NUGGET, Random.nextInt(9, 37)) },
            Trade(20) { ItemStack(Material.QUARTZ, Random.nextInt(8, 17)) },
            Trade(20) { ItemStack(Material.GLOWSTONE_DUST, Random.nextInt(5, 13)) },
            Trade(20) { ItemStack(Material.MAGMA_CREAM, Random.nextInt(2, 7)) },
            Trade(20) { ItemStack(Material.ENDER_PEARL, Random.nextInt(4, 9)) },
            Trade(20) { ItemStack(Material.STRING, Random.nextInt(8, 25)) },
            Trade(40) { ItemStack(Material.FIRE_CHARGE, Random.nextInt(1, 6)) },
            Trade(40) { ItemStack(Material.GRAVEL, Random.nextInt(8, 17)) },
            Trade(40) { ItemStack(Material.LEATHER, Random.nextInt(4, 11)) },
            Trade(40) { ItemStack(Material.NETHER_BRICK, Random.nextInt(4, 17)) },
            Trade(40) { ItemStack(Material.OBSIDIAN, 1) },
            Trade(40) { ItemStack(Material.CRYING_OBSIDIAN, Random.nextInt(1, 4)) },
            Trade(40) { ItemStack(Material.SOUL_SAND, Random.nextInt(4, 17)) }
        )
    }

    @EventHandler
    fun barter(e: PiglinBarterEvent) {
        e.isCancelled = true

        val totalWeight = trades.sumOf { it.weight }
        val roll = Random.nextInt(totalWeight)
        var currentWeight = 0

        val selectedTrade = trades.first { trade ->
            currentWeight += trade.weight
            roll < currentWeight
        }

        e.entity.run {
            val item = world.dropItem(location.add(0.0, 1.75, 0.0), selectedTrade.creator())
            world.playSound(location, Sound.ENTITY_PIGLIN_AMBIENT, 1f, 1f)
            swingOffHand()

            world.players.minByOrNull { it.location.distanceSquared(location) }?.let {
                item.velocity = it.location.toVector()
                    .subtract(location.toVector())
                    .normalize()
                    .multiply(0.2)
                    .setY(0.05)
            }
        }
    }

    @EventHandler
    fun noBruteSpawn(e: CreatureSpawnEvent) {
        if (e.entityType == EntityType.PIGLIN_BRUTE && e.spawnReason != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) e.isCancelled = true
    }

    @EventHandler
    fun noBruteGen(e: ChunkLoadEvent) {
        e.chunk.entities.forEach {
            if (it.type == EntityType.PIGLIN_BRUTE) it.remove()
        }
    }
}
