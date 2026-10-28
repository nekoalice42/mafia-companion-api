package me.nekoalice.mafia.api.server.validation

import me.nekoalice.mafia.api.dto.auth.LoginData

fun LoginData.validate() = runValidation {
    require(username.isNotBlank()) { "Username must not be blank" }
    require(password.isNotBlank()) { "Password must not be blank" }
}
