package me.justlime.redeemX.enums

sealed interface JTab {

    enum class GeneralActions(val value: String) {
        Gen("gen"),

        //GUI("gui"), //TODO

        Modify("modify"),


        Delete("delete"),

        Info("info"),

        Reload("reload"),
        Preview("preview"),



        Usage("usage"),

        Renew("renew"),

        Help("help")

        //GUI("gui"), //TODO

    }

    enum class Type(val value: String) {
        Code("code"), Template("template"),
    }

    enum class Generate(val value: String) {
        Digit("DIGIT"), Custom("CUSTOM"), Amount("AMOUNT")
    }

    enum class Modify(val value: String) {

        Enabled("enabled"), // Can be toggled
        Locked("locked"),   // Can be toggled
        SetRedemption("redemption"), SetPlayerLimit("playerLimit"),

        SetCommand("setCommand"), AddCommand("addCommand"), RemoveCommand("removeCommand"), ListCommand("listCommand"),

        SetDuration("setDuration"), AddDuration("addDuration"), RemoveDuration("removeDuration"),

        SetPermission("permission"), // Can be toggled
        SetPin("pin"),         // Can be toggled

        SetTarget("setTarget"), AddTarget("addTarget"), RemoveTarget("removeTarget"), ListTarget("listTarget"),

        SetTemplate("template"), Cooldown("cooldown");

        enum class Edit(val value: String): JTab {
            It("edit"),
            Reward("rewards"),
            Message("messages"),
            Sound("sound")
        }

    }
    enum class Template(val value: String) {
        SetRedemption("redemption"), SetPlayerLimit("playerLimit"),

        SetCommand("setCommand"), AddCommand("addCommand"), RemoveCommand("removeCommand"), ListCommand("listCommand"),

        SetDuration("setDuration"), AddDuration("addDuration"), RemoveDuration("removeDuration"),

        SetCooldown("cooldown"),

        SetPermission("permission"),
        TogglePermissionRequired("togglePermission"), // Can be toggled

        SetPin("pin"),// Can be toggled
        Locked("locked")
    }

    enum class Boolean(val value: String){
        True("true"), False("false")
    }

    enum class Delete(val value: String) {
        All("*"), Confirm("CONFIRM"), Last("LAST"),
    }
}
