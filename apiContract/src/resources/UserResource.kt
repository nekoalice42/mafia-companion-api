package me.nekoalice.mafia.api.contracts.resources

import io.ktor.resources.Resource
import kotlinx.serialization.SerialName
import me.nekoalice.mafia.api.dto.user.UserId
import me.nekoalice.mafia.api.dto.user.external.ExternalUserProvider

@Resource("/user")
public class UserResource {
    @Resource("/me")
    public class Me(public val parent: UserResource)

    @Resource("/{user_id}")
    public class ById(
        public val parent: UserResource,
        @SerialName("user_id") public val id: UserId,
    ) {
        @Resource("/external")
        public class External(public val parent: ById) {
            @Resource("/{provider_name}")
            public class Provider(
                public val parent: External,
                @SerialName("provider_name") public val provider: ExternalUserProvider,
            )
        }

        @Resource("/password")
        public class Password(public val parent: ById)
    }
}
