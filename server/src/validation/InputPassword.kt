package me.nekoalice.mafia.api.server.validation

import me.nekoalice.mafia.api.dto.user.security.InputPassword

fun InputPassword.validate() = runValidation {
    require(password.isNotBlank()) { "Password must not be blank" }
}
