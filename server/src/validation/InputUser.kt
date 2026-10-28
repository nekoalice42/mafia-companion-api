package me.nekoalice.mafia.api.server.validation

import me.nekoalice.mafia.api.dto.user.InputUser

fun InputUser.validate() = runValidation {
    require(username.isNotBlank()) { "Username must not be blank" }
}
