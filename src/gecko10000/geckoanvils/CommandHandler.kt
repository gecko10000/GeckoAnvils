package gecko10000.geckoanvils

import gecko10000.geckoanvils.di.MyKoinComponent
import gecko10000.geckoanvils.managers.DataManager
import gecko10000.geckolib.extensions.MM
import gecko10000.geckolib.extensions.parseMM
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.koin.core.component.inject
import redempt.redlib.commandmanager.CommandHook
import redempt.redlib.commandmanager.CommandParser

class CommandHandler : MyKoinComponent {

    private val plugin: GeckoAnvils by inject()
    private val dataManager: DataManager by inject()

    init {
        CommandParser(plugin.getResource("command.rdcml")).parse().register("mm", this)
    }

    @CommandHook("reload")
    fun reload(sender: CommandSender) {
        plugin.reloadConfigs()
        sender.sendMessage(parseMM("<green>Configs reloaded."))
    }

    @CommandHook("reset")
    fun reset(sender: CommandSender, target: Player) {
        dataManager.resetData(target)
        sender.sendMessage(
            MM.deserialize(
                "<green>Reset data for <name>.",
                Placeholder.component("name", target.name())
            )
        )
    }

}
