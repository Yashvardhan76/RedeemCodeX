package me.justlime.redeemX.enums

sealed interface Tab {
    // General Actions
    enum class GeneralActions(val value: String) {
        Gen("create"),
        Modify("modify"),
        ModifyTemplate("modifyTemplate"),
        Delete("delete"),
        DeleteAll("delete_all"),
        Info("info"),
        Reload("reload"),
        Usage("usage"), //TODO
        GUI("gui"), //TODO
        CONFIRM("CONFIRM"),
        Renew("renew")
    }

    // Modify Commands
    enum class Modify(val value: String) {
        Enabled("enabled"), // Can be toggled
        Locked("locked"),   // Can be toggled
        SetRedemption("setRedemptionLimit"),
        SetPlayerLimit("setPlayerLimit"),

        SetCommand("setCommand"),
        AddCommand("addCommand"),
        RemoveCommand("removeCommand"),
        ListCommand("listCommand"),
        PreviewCommand("previewCommand"),

        SetDuration("setDuration"),
        AddDuration("addDuration"),
        RemoveDuration("removeDuration"),

        SetPermission("setPermission"), // Can be toggled
        SetPin("setPin"),         // Can be toggled

        SetTarget("setTarget"),
        AddTarget("addTarget"),
        RemoveTarget("removeTarget"),
        ListTarget("listTarget"),


        SetTemplate("setTemplate"),    // Can be toggled
        Cooldown("cooldown")
    }

    //Template Commands
    enum class Template(val value: String){

        Rename("rename"), //TODO
        Enabled("enabled"), // Can be toggled

        SetRedemption("setRedemptionLimit"),
        SetPlayerLimit("setPlayerLimit"),

        SetCommand("setCommand"),
        AddCommand("addCommand"),
        RemoveCommand("removeCommand"),
        ListCommand("listCommand"),
        PreviewCommand("previewCommand"),

        SetDuration("setDuration"),
        AddDuration("addDuration"),
        RemoveDuration("removeDuration"),

        Permission("setPermission"), // Can be toggled
        SetPin("setPin"),         // Can be toggled

        Cooldown("cooldown")
    }

    // Generation Commands
    enum class Generate(val value: String) {
        Digit("DIGIT"),
        Template("template"),
        Custom("CUSTOM"),
        Amount("AMOUNT")
    }

}
