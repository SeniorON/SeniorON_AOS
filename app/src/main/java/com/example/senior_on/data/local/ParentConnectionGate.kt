package com.example.senior_on.data.local

/** Process-local approval. A restored process must check the server before parent writes. */
object ParentConnectionGate {
    private var revision = 0L
    private var parent = false
    private var pending = false
    private var ready = false

    @Synchronized fun initialize(isParent: Boolean) {
        revision++
        parent = isParent
        pending = false
        ready = false
    }
    @Synchronized fun beginSession(isParent: Boolean) {
        initialize(isParent)
        pending = isParent
    }
    @Synchronized fun hold(): Long {
        revision++
        pending = true
        ready = false
        return revision
    }
    @Synchronized fun approve(expected: Long): Boolean {
        if (expected != revision) return false
        pending = false
        ready = true
        return true
    }
    @Synchronized fun backgroundCheckVersion(): Long? = if (parent && !pending && !ready) revision else null
    @Synchronized fun isReady(): Boolean = ready
    @Synchronized fun blocks(method: String, path: String): Boolean {
        if (!parent || ready) return false
        return path == "/ws" ||
            (method == "PUT" && path == "/api/devices/status") ||
            (method == "PATCH" && path in setOf("/api/devices/location", "/api/devices/fcm-token")) ||
            (method == "POST" && path.startsWith("/api/event/"))
    }
}
