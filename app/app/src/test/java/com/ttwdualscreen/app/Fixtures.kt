package com.ttwdualscreen.app

object Fixtures {
    fun load(name: String): String =
        requireNotNull(Fixtures::class.java.getResourceAsStream("/fixtures/$name")) {
            "fixture $name missing (copyProtocolFixtures should put protocol/fixtures on the test classpath)"
        }.bufferedReader().use { it.readText() }
}
