package com.xaiver.cobbleballworks.registry

/**
 * Every Cobblemon ball that Ballworks re-plumbs into the blank -> hammered lid -> assembly flow.
 * Adding a new ball: append its short id here and drop matching recipe JSONs under
 * data/cobbleballworks/recipes/. Item registration, creative tab, and lang entries follow
 * automatically from this list.
 */
object BallRegistry {
    val ALL: List<String> = listOf(
        "azure",
        "beast",
        "citrine",
        "dive",
        "dream",
        "dusk",
        "fast",
        "friend",
        "great",
        "heal",
        "heavy",
        "level",
        "love",
        "lure",
        "luxury",
        "master",
        "moon",
        "nest",
        "net",
        "park",
        "poke",
        "premier",
        "quick",
        "repeat",
        "roseate",
        "safari",
        "slate",
        "sport",
        "timer",
        "ultra",
        "verdant",
    )
}
