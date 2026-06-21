package me.nekoalice.mafia.api.server.validation

import me.nekoalice.mafia.api.dto.models.LoginData

fun LoginData.validate(): Boolean = username.isNotBlank() && password.isNotBlank()
