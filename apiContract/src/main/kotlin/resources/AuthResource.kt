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
}
