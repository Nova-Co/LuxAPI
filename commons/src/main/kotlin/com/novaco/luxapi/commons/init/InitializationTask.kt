package com.novaco.luxapi.commons.init

/**
 * Marker interface for self-registering platform/consumer setup code.
 * Any class implementing this and exposing a no-arg constructor is automatically
 * discovered and `.run()` at platform startup — the platform decides *how* it
 * discovers implementors (native mod-file scan on Fabric/NeoForge, a classpath
 * walk on Bukkit — see each platform's own `*InitScanner`/`*InitializerRunner`),
 * but every platform runs the same contract once discovery hands it an instance.
 *
 * This exists to close the gap where a new piece of setup code only gets wired
 * into some of `fabric`/`neoforge`/`bukkit`'s hand-written bootstrap classes and
 * silently doesn't run on the platform someone forgot.
 */
fun interface InitializationTask : Runnable
