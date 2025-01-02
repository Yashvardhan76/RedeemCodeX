package me.justlime.redeemX.enums

enum class JFiles(val filename: String) {
    CONFIG("config.yml"),
    MESSAGES("messages_{lang}.yml"),
    TEMPLATE("template.yml")
}