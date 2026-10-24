package me.nekoalice.mafia.api.server.validation

import me.nekoalice.mafia.api.dto.auth.LoginData

fun LoginData.validate() = runValidation {
    require(username.isNotBlank()) { "username is blank" }
    require(password.isNotBlank()) { "password is blank" }
}
