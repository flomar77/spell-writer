package com.spellwriter.data.models

data class Progress(
    val wizardStars: Int = 0,
    val pirateStars: Int = 0,
    val currentWorld: World = World.WIZARD
)

enum class World {
    WIZARD,
    PIRATE
}

fun Progress.isWorldUnlocked(world: World): Boolean {
    return when (world) {
        World.WIZARD -> true // Always unlocked
        World.PIRATE -> wizardStars >= 3
    }
}

fun Progress.getCurrentStar(world: World): Int {
    return when (world) {
        World.WIZARD -> wizardStars + 1
        World.PIRATE -> pirateStars + 1
    }.coerceAtMost(3)
}
