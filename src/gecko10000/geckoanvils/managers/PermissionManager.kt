package gecko10000.geckoanvils.managers

import gecko10000.geckoanvils.GeckoAnvils
import gecko10000.geckoanvils.di.MyKoinComponent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.koin.core.component.inject

class PermissionManager : MyKoinComponent {

    companion object {
        private const val ENCHANT_COUNT_PREFIX = "geckoanvils.enchant.slots."
        private const val REPAIR_COUNT_PREFIX = "geckoanvils.repair.slots."
        private const val ENCHANT_SPEEDUP_PREFIX = "geckoanvils.enchant.speedup."
        private const val REPAIR_SPEEDUP_PREFIX = "geckoanvils.repair.speedup."
    }

    private val plugin: GeckoAnvils by inject()

    init {
        // Check these to "register" them with the permissions plugin.
        for (prefix in setOf(
            ENCHANT_COUNT_PREFIX,
            REPAIR_COUNT_PREFIX,
            ENCHANT_SPEEDUP_PREFIX,
            REPAIR_SPEEDUP_PREFIX,
        )) {
            Bukkit.getConsoleSender().hasPermission(prefix)
        }
    }

    private fun intPermMax(player: Player, prefix: String) = player.effectivePermissions
        .mapNotNull { if (it.value) it.permission else null } // get positive perms
        .filter { it.startsWith(prefix) } // get relevant perms
        .mapNotNull { it.substringAfter(prefix).toIntOrNull() } // get integer value
        .maxOrNull() ?: 0 // total

    private fun doublePermMax(player: Player, prefix: String) = player.effectivePermissions
        .mapNotNull { if (it.value) it.permission else null }
        .filter { it.startsWith(prefix) }
        .mapNotNull { it.substringAfter(prefix).toDoubleOrNull() }
        .maxOrNull() ?: 0.0

    fun getAllowedEnchants(player: Player) = intPermMax(player, ENCHANT_COUNT_PREFIX)
    fun getAllowedRepairs(player: Player) = intPermMax(player, REPAIR_COUNT_PREFIX)
    fun getEnchantTimeSpeedup(player: Player) = doublePermMax(player, ENCHANT_SPEEDUP_PREFIX) + 1
    fun getRepairTimeSpeedup(player: Player) = doublePermMax(player, REPAIR_SPEEDUP_PREFIX) + 1

}
