
# Table of Contents
<!-- TOC -->
* [Table of Contents](#table-of-contents)
* [RedeemCodeX Plugin](#redeemcodex-plugin)
    * [Features](#features)
  * [Redeem Command](#redeem-command)
    * [/redeem](#redeem-)
  * [RCX Command](#rcx-command)
    * [/rcx gen](#rcx-gen)
    * [/rcx modify](#rcx-modify)
    * [/rcx delete](#rcx-delete)
    * [/rcx preview](#rcx-preview)
    * [/rcx usage](#rcx-usage)
    * [/rcx renew](#rcx-renew)
    * [/rcx reload](#rcx-reload)
    * [/rcx info](#rcx-info)
  * [RCX Modify Properties](#rcx-modify-properties)
  * [Permissions Overview](#permissions-overview)
  * [TODO List](#todo-list)
<!-- TOC -->

---

# RedeemCodeX Plugin

This plugin provides a comprehensive redeem code system.  It allows for the creation, management, and redemption of codes with various features such as custom commands, durations, usage limits, permissions, PINs, targeted players, and templates.

### Features
  * Ease of Modify Bulk Codes via Template
  * Support Placeholder api with Hex Colors
  * Discord Bot Support
  * GUI Support For Editing Rewards, Messages, Sounds
  * Template-Sync, This allows you to automatically update all generated bulk codes with a single template modification.
  * Optimized for Best performance
  * and much more

---

## Redeem Command

### /redeem 
```
/redeem <code> [pin]
```

## RCX Command

The `RCXCommand` allows administrators to manage redeem codes with the following subcommands:

### /rcx gen
```
/rcx gen code <CUSTOM> [TEMPLATE]
/rcx gen code <DIGIT> [TEMPLATE] [AMOUNT]
/rcx gen template <template>
```

### /rcx modify
```
/rcx modify code <code> <toggle-property>
/rcx modify code <code> <property> <value>
/rcx modify template <template> <toggle-property>
/rcx modify template <template> <property> <value>
```

### /rcx delete
```
/rcx delete code <code> [codes]
/rcx delete code * CONFIRM
/rcx delete template <template> [Templates]
/rcx delete template * CONFIRM
```

### /rcx preview
```
/rcx preview code <code>
/rcx preview template <template>
```

### /rcx usage
```
/rcx usage code <code>
/rcx usage template <template> 
```

### /rcx renew
```
/rcx renew <code>
```

### /rcx reload
```
/rcx reload
```

### /rcx info
```
/rcx info
```

---

## RCX Modify Properties
- **`enabled`**: toggle Enable/disable a code/template.
```
/rcx modify code <code> enabled
/rcx modify template <template> enabled
```

- **`redemption`**: Set the maximum number of times a code can be redeemed.
```
/rcx modify code <code> redemption <value>
/rcx modify template <template> redemption <value>
```

- **`playerLimit`**: Set the maximum number of players who can redeem a code.
```
/rcx modify code <code> playerLimit <value>
/rcx modify template <template> playerLimit <value>
```

- **`permission`**: Set the permission required to redeem a code.
```
/rcx modify code <code> permission <value>
/rcx modify template <template> permission <value>
```

- **`pin`**: Set a PIN for the code.
```
/rcx modify code <code> pin <value>
/rcx modify template <template> pin <value>
```

- **`target`**: Set the target players or groups allowed to redeem the code.
```
/rcx modify code <code> addTarget <player> <player> <player>...
/rcx modify code <code> removeTarget <player> <player> <player>...
/rcx modify code <code> setTarget <player> <player> <player>...
```

- **`duration`**: Add/Remove/Set the validity duration of a code (e.g. value 1d, 1h, 1m, 1s).
```
/rcx modify code <code> addDuration <value>
/rcx modify template <template> addDuration <value>

/rcx modify code <code> setDuration <value>
/rcx modify template <template> setDuration <value>

/rcx modify code <code> removeDuration <value>
/rcx modify template <template> removeDuration <value>
```

- **`cooldown`**: Set the cooldown period for the code. (e.g.value 1d, 1h, 1m, 1s)
```
/rcx modify code <code> cooldown <value>
/rcx modify template <template> cooldown <value>

```

- **`Commands`**: Add/Remove/Set the commands to execute upon code redemption.
```
/rcx modify code <code> addCommand <command>
/rcx modify template <template> addCommand <command>

/rcx modify code <code> removeCommand <id>
/rcx modify template <template> removeCommand <id>

/rcx modify code <code> setCommand <id> <command>
/rcx modify template <template> setCommand <id> <command>

```

- **`template`**: Set the template for the code.
```
/rcx modify code <code> template <template>

```

- **`sync`**: toggle true/false to sync properities.
```
/rcx modify code <code> sync
/rcx modify template <template> sync 

```



- **`rewards`**: Edit the rewards for the code. (Open in GUI)
```
/rcx modify code <code> edit rewards
/rcx modify template <template> edit rewards

```

- **`messages`**: Edit the messages for the code. (Open in GUI)
```
/rcx modify code <code> edit messages
/rcx modify template <template> messages

```

- **`sound`**: Edit the sound for the code. (Open in GUI)
```
/rcx modify code <code> edit sound
/rcx modify template <template> sound

```

---

## Permissions Overview
- **`redeemx.use`**: allow player to use /redeemed
- **`redeemx.admin.use.gen`**: Generate codes
- **`redeemx.admin.use.modify`**: Modify codes/templates
- **`redeemx.admin.use.delete`**: Delete codes
- **`redeemx.admin.use.info`**: View code info
- **`redeemx.admin.use.renew`**: Renew codes
- **`redeemx.admin.use.reload`**: Reload configuration

---

## TODO List
* GUI Support
* Improvement on Reward GUI
* Improvement on generation `CUSTOM` code to support bulk generation
* Add option to `modify` various code using single command
  * `Example: /rcx modify CODE01 pin 1234 CODE02 CODE03 CODE99`

---
