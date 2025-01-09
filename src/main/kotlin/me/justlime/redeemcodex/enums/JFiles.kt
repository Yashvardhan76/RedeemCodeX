package me.justlime.redeemcodex.enums

enum class JFiles(val filename: String) {
    CONFIG("config.yml"),
    MESSAGES("messages_{lang}.yml"),
    TEMPLATE("template.yml"),
    GUI("gui.yml")
}