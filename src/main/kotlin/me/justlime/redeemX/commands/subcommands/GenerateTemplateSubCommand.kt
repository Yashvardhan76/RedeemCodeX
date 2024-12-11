package me.justlime.redeemX.commands.subcommands

import me.justlime.redeemX.RedeemX
import me.justlime.redeemX.data.config.ConfigImpl
import me.justlime.redeemX.data.config.ConfigManager
import me.justlime.redeemX.data.config.yml.JMessage
import me.justlime.redeemX.data.models.RedeemTemplate
import me.justlime.redeemX.data.repository.ConfigRepository
import me.justlime.redeemX.data.repository.RedeemCodeRepository
import me.justlime.redeemX.state.RedeemCodeState

class GenerateTemplateSubCommand(plugin: RedeemX): JSubCommand {
    val codeRepository = RedeemCodeRepository(plugin)
    private val configRepository = ConfigRepository(ConfigImpl(plugin, ConfigManager(plugin)))

    override fun execute(state: RedeemCodeState): Boolean {
        if (state.args.size < 2) {
            configRepository.sendMsg(JMessage.Commands.Gen_Template.INVALID_SYNTAX, state)
            return false
        }
        state.template = state.args[1]
        if(state.template.length<3){
            configRepository.sendMsg(JMessage.Commands.Gen_Template.LENGTH_ERROR, state)
            return false
        }

        if(configRepository.getTemplate(state.template) != null) return configRepository.sendMsg(JMessage.Commands.Gen_Template.CODE_ALREADY_EXIST, state) != Unit
        val templateState = configRepository.getTemplate()?.copy()
        if (templateState == null) {
            configRepository.createTemplate(RedeemTemplate(name = state.template))
        }else configRepository.createTemplate(templateState.copy(name = state.template))
        configRepository.sendMsg(JMessage.Commands.Gen_Template.SUCCESS, state)
        return true
    }
}
