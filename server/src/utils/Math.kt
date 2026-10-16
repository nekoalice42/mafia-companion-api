package me.nekoalice.mafia.api.server.utils

internal val Double.roundedToNearestInt: Int
    get() = when {
        !isFinite() -> throw IllegalArgumentException("Invalid input: $this")
        this < 0 -> throw UnsupportedOperationException("Negative values are unsupported")
        else -> (this + 0.5).toInt()
    }

internal infix fun Int.roundDiv(other: Int) = when {
    other <= 0 || this < 0 -> throw IllegalArgumentException("Invalid input: $this roundDiv $other")
    other == 1 -> this
    else -> (this + other / 2) / other
}
