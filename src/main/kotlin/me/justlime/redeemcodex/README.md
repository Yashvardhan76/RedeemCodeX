# RedeemCodeX Plugin

This plugin provides a comprehensive redeem code system.  It allows for the creation, management, and redemption of codes with various features such as custom commands, durations, usage limits, permissions, PINs, targeted players, and templates.

## Features

* **Code Generation and Storage:**  Redeem codes are generated and stored persistently using a local SQLite database.
* **Custom Commands:** Each code can execute a set of custom commands upon redemption.
* **Durations:** Set time limits for code validity.
* **Redemption Limits:** Configure how many times a code can be redeemed (overall and per player).
* **Permissions:** Restrict code redemption to players with specific permissions.
* **PIN Codes:** Add an extra layer of security with PIN codes.
* **Targeted Players:** Specify which players can redeem a code.
* **Templates:** Create reusable templates for common code configurations.
* **Template Locking:** Lock codes to their templates to prevent unintended modifications.
* **Cooldowns:** Implement cooldowns to prevent rapid code redemption.


## Data Structures

* **RedeemCode:** Represents an individual redeem code with attributes like code, commands, duration, enabled status, redemption limits, permission, PIN, target players, usage statistics, template, template lock status, and cooldown information.
* **RedeemCodeDatabase:**  A simplified representation of the `RedeemCode` used for database storage.  This structure uses simpler data types for compatibility with SQLite.
* **RedeemTemplate:** (Not explicitly defined in the provided code but implied) Represents a template for creating redeem codes.
* **JProperty:** An enum used to represent the properties of a `RedeemCode` for database operations.


## Data Management

* **RedeemCodeRepository:** Manages the interaction with the database and provides methods for retrieving, creating, updating, and deleting redeem codes.  It also handles logic for checking code expiration and applying template settings.
* **RedeemCodeDao:** An interface defining data access methods for interacting with the redeem code database.
* **RedeemCodeDaoImpl:** The concrete implementation of `RedeemCodeDao` using SQLite.
* **DatabaseManager:** (Not explicitly defined but implied) Handles the database connection and setup.
* **RedeemCodeService:** Provides utility functions for managing redeem codes, such as checking durations and expiration times.


## Dependencies

## Dependencies

RedeemCodeX utilizes the following dependencies:

| Dependency                                | Description                                     | Version                |
|-------------------------------------------|-------------------------------------------------|------------------------|
| `org.spigotmc:spigot-api`                 | Minecraft Spigot API for plugin development     | `1.21.1-R0.1-SNAPSHOT` |
| `org.jetbrains.kotlin:kotlin-stdlib-jdk8` | Kotlin Standard Library for JDK 8 features      |                        |
| `com.zaxxer:HikariCP`                     | HikariCP for efficient JDBC connection pooling  | `4.0.3`                |
| `org.xerial:sqlite-jdbc`                  | SQLite JDBC Driver                              | `3.47.0.0`             |
| `net.dv8tion:JDA`                         | Java Discord API (JDA) for Discord integration  | `5.2.1`                |
| `com.google.code.gson:gson`               | Gson for JSON serialization and deserialization | `2.10.1`               |

## TODO List

* **Code Generation Algorithm:**  While the provided code manages code storage and retrieval, the actual code generation process is still performance impact for bulk generation.
* **Redeem Code GUI:**  A graphical user interface for managing and creating redeem codes would significantly improve usability.
* **Metrics/Statistics:** Implement tracking and reporting of redeem code usage statistics.
* **Asynchronous Operations:** For performance optimization, especially with large numbers of redeem codes, consider moving database operations to asynchronous threads.
  Here’s a shorter version of the documentation:

---

# RCX Command Documentation

The `RCXCommand` allows administrators to manage redeem codes with the following subcommands:

## Subcommands

### `gen`
Generates a new redeem code.
```
/rcx gen <arguments>
```
**Permission:** `redeemx.admin.gen`

### `gen_template`
Generates a redeem code based on a template.
```
/rcx gen_template <template_name> <arguments>
```
**Permission:** `redeemx.admin.gen_template`

### `modify`
Modifies an existing redeem code.
```
/rcx modify <redeem_code> <property> <value>
```

**Permission:** `redeemx.admin.modify`

### `modify_template`
Modifies a redeem code template.
```
/rcx modify_template <template_name> <new_arguments>
```
**Permission:** `redeemx.admin.modify`

### `delete` & `delete_all`
Deletes a specific redeem code or all codes.
```
/rcx delete <redeem_code>
/rcx delete_all CONFIRM
```
**Permission:** `redeemx.admin.delete`

### `info`
Displays information about a redeem code.
```
/rcx info <redeem_code>
```
**Permission:** `redeemx.admin.info`

### `renew`
Renews a redeem code.
```
/rcx renew <redeem_code>
```
**Permission:** `redeemx.admin.renew`

### `reload`
Reloads the plugin's configuration.
```
/rcx reload
```
**Permission:** `redeemx.admin.reload`

---

## Permissions Overview

- **`redeemx.admin.gen`**: Generate codes
- **`redeemx.admin.gen_template`**: Generate codes from templates
- **`redeemx.admin.modify`**: Modify codes/templates
- **`redeemx.admin.delete`**: Delete codes
- **`redeemx.admin.info`**: View code info
- **`redeemx.admin.renew`**: Renew codes
- **`redeemx.admin.reload`**: Reload configuration

--- 

This concise documentation covers the basic usage and permissions for the `RCXCommand` in the **RedeemX** plugin.