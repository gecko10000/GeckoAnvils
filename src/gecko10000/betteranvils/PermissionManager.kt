package gecko10000.betteranvils

import gecko10000.betteranvils.di.MyKoinComponent
import org.bukkit.entity.Player
import org.koin.core.component.inject

class PermissionManager : MyKoinComponent {

    companion object {
        private const val ENCHANT_COUNT_PREFIX = "betteranvils.enchant.slots."
        private const val REPAIR_COUNT_PREFIX = "betteranvils.repair.slots."
        private const val ENCHANT_SPEEDUP_PREFIX = "betteranvils.enchant.speedup."
        private const val REPAIR_SPEEDUP_PREFIX = "betteranvils.repair.speedup."
    }

    private val plugin: BetterAnvils by inject()

    private fun intSum(player: Player, prefix: String) = player.effectivePermissions
        .mapNotNull { if (it.value) it.permission else null } // get positive perms
        .filter { it.startsWith(prefix) } // get relevant perms
        .mapNotNull { it.substringAfter(prefix).toIntOrNull() } // get integer value
        .sum() // total

    private fun doubleSum(player: Player, prefix: String) = player.effectivePermissions
        .mapNotNull { if (it.value) it.permission else null }
        .filter { it.startsWith(prefix) }
        .mapNotNull { it.substringAfter(prefix).toDoubleOrNull() }
        .sum()

    fun getAllowedEnchants(player: Player) = intSum(player, ENCHANT_COUNT_PREFIX)
    fun getAllowedRepairs(player: Player) = intSum(player, REPAIR_COUNT_PREFIX)
    fun getEnchantTimeSpeedup(player: Player) = doubleSum(player, ENCHANT_SPEEDUP_PREFIX) + 1
    fun getRepairTimeSpeedup(player: Player) = doubleSum(player, REPAIR_SPEEDUP_PREFIX) + 1

}
