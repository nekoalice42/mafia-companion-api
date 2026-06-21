package me.nekoalice.mafia.api.contracts.resources

import io.ktor.resources.Resource

@Resource("/user")
public class UserResource {
    @Resource("/me")
    public class Me(public val parent: UserResource)
}
