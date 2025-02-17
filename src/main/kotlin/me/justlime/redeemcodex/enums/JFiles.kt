/*
 *
 *  RedeemCodeX
 *  Copyright 2024 JUSTLIME
 *
 *  This software is licensed under the Apache License 2.0 with a Commons Clause restriction.
 *  See the LICENSE file for details.
 *
 *
 *
 */


package me.justlime.redeemcodex.enums

enum class JFiles(val filename: String) {
    CONFIG("config.yml"),
    MESSAGES("messages_{lang}.yml"),
    TEMPLATE("template.yml"),
    GUI("gui.yml")
}