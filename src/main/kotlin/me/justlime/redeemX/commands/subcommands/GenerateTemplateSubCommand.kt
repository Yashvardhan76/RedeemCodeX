package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.config.yml.JMessage
import me.justlime.redeemX.data.repository.ConfigRepository
import me.justlime.redeemX.models.CodePlaceHolder
import me.justlime.redeemX.models.RedeemTemplate
import org.bukkit.command.CommandSender

class GenerateTemplateSubCommand(plugin: RedeemX) : JSubCommand {
    private val configRepository = ConfigRepository(plugin)

    override fun execute(sender: CommandSender, args: MutableList<String>): Boolean {
        val placeHolder = CodePlaceHolder(sender, args)
        if (args.size < 2) {
            configRepository.sendMsg(JMessage.Commands.GenTemplate.INVALID_SYNTAX, placeHolder)
            return false
        }
        placeHolder.template = args[1]
        val template = placeHolder.template
        if (template.length < 3) {
            configRepository.sendMsg(JMessage.Commands.GenTemplate.LENGTH_ERROR, placeHolder)
            return false
        }

        if (configRepository.getTemplate(template) != null) return configRepository.sendMsg(JMessage.Commands.GenTemplate.CODE_ALREADY_EXIST, placeHolder) != Unit
        val templateState = configRepository.getTemplate()?.copy()
        if (templateState == null) {
            configRepository.createTemplate(RedeemTemplate(name = template))
        } else configRepository.createTemplate(templateState.copy(name = template))
        configRepository.sendMsg(JMessage.Commands.GenTemplate.SUCCESS, placeHolder)
        return true
    }
}
