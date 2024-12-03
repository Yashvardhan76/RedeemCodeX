package me.justlime.redeemX.commands.subcommands

class ReloadSubCommand(val plugin: me.justlime.redeemX.RedeemX) : JSubCommand {
    private val config = plugin.configFile
    private val stateManager = plugin.stateManager
    private val db = plugin.redeemCodeDB

    override fun execute(sender: org.bukkit.command.CommandSender, args: Array<out String>): Boolean {
        val state = stateManager.createState(sender)
        try {
            config.reloadAllConfigs()
            db.init()
            config.dm(me.justlime.redeemX.config.JMessage.Commands.Reload.SUCCESS, state)
            return true

        } catch (e: Exception) {
            config.dm(me.justlime.redeemX.config.JMessage.Commands.Reload.FAILED, state)
            return false
        }
    }
}