package me.nekoalice.mafia.api.server.validation

import me.nekoalice.mafia.api.dto.auth.LoginData

fun LoginData.validate(): Boolean = username.isNotBlank() && password.isNotBlank()
