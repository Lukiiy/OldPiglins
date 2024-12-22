package me.lukiiy.spdrns

import org.bukkit.inventory.ItemStack

data class Trade(val weight: Int, val creator: () -> ItemStack)
