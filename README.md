
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
  * [Permissions Overview](#permissions-overview)
  * [TODO List](#todo-list)
<!-- TOC -->

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
