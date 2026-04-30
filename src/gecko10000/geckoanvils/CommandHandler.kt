package gecko10000.geckoanvils

import gecko10000.geckoanvils.di.MyKoinComponent
import gecko10000.geckoanvils.managers.DataManager
import gecko10000.geckolib.extensions.MM
import gecko10000.geckolib.extensions.parseMM
import io.papermc.paper.plugin.lifecycle.event.handler.LifecycleEventHandler
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.strokkur.commands.Command
import net.strokkur.commands.Executes
import net.strokkur.commands.permission.Permission
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.koin.core.component.inject

@Command("geckoanvils")
@Permission("geckoanvils.command")
class CommandHandler : MyKoinComponent {

    private val plugin: GeckoAnvils by inject()
    private val dataManager: DataManager by inject()

    fun register() {
        plugin.lifecycleManager
            .registerEventHandler(LifecycleEvents.COMMANDS.newHandler(LifecycleEventHandler { event ->
                CommandHandlerBrigadier.register(
                    event.registrar()
                )
            }))
    }

    @Executes("reload")
    @Permission("geckoanvils.reload")
    fun reload(sender: CommandSender) {
        plugin.reloadConfigs()
        sender.sendMessage(parseMM("<green>Configs reloaded."))
    }

    @Executes("reset")
    @Permission("geckoanvils.reset")
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
