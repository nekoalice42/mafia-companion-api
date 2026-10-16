package me.nekoalice.mafia.api.contracts.resources

import io.ktor.resources.Resource

@Resource("/auth")
public class AuthResource {
    @Resource("/password")
    public class Password(public val parent: AuthResource)

    @Resource("/token")
    public class Token(public val parent: AuthResource) {
        @Resource("/current")
        public class Current(public val parent: Token)
    }

    @Resource("/telegram")
    public class Telegram(public val parent: AuthResource) {
        @Resource("/callback")
        public class OauthCallback(public val parent: Telegram)

        @Resource("/login")
        public class Login(public val parent: Telegram)

        @Resource("/challenge")
        public class Challenge(public val parent: Telegram)
    }
}
