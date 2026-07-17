package dev.busung.s25uroot

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.UUID

enum class InstallRunResult {
    Running,
    Succeeded,
    Failed,
}

data class InstallHistoryEntry(
    val id: String,
    val startedAtMillis: Long,
    val completedAtMillis: Long?,
    val result: InstallRunResult,
    val log: String,
)

class InstallHistoryStore(context: Context) {
    private val directory = File(context.filesDir, "install-history").apply { mkdirs() }

    fun load(): List<InstallHistoryEntry> = directory
        .listFiles { file -> file.extension == "json" }
        .orEmpty()
        .map(::decode)
        .sortedByDescending(InstallHistoryEntry::startedAtMillis)

    fun closeInterruptedRuns(): List<InstallHistoryEntry> = load().map { entry ->
        if (entry.result == InstallRunResult.Running) {
            entry.copy(
                completedAtMillis = System.currentTimeMillis(),
                result = InstallRunResult.Failed,
            ).also(::save)
        } else {
            entry
        }
    }

    fun create(): InstallHistoryEntry = InstallHistoryEntry(
        id = UUID.randomUUID().toString(),
        startedAtMillis = System.currentTimeMillis(),
        completedAtMillis = null,
        result = InstallRunResult.Running,
        log = "",
    ).also(::save)

    fun save(entry: InstallHistoryEntry) {
        val target = File(directory, "${entry.id}.json")
        val temporary = File(directory, "${entry.id}.tmp")
        temporary.writeText(encode(entry).toString(), Charsets.UTF_8)
        Files.move(
            temporary.toPath(),
            target.toPath(),
            StandardCopyOption.REPLACE_EXISTING,
            StandardCopyOption.ATOMIC_MOVE,
        )
    }

    private fun encode(entry: InstallHistoryEntry) = JSONObject()
        .put("id", entry.id)
        .put("startedAtMillis", entry.startedAtMillis)
        .put("completedAtMillis", entry.completedAtMillis ?: JSONObject.NULL)
        .put("result", entry.result.name)
        .put("log", entry.log)

    private fun decode(file: File): InstallHistoryEntry {
        val value = JSONObject(file.readText(Charsets.UTF_8))
        return InstallHistoryEntry(
            id = value.getString("id"),
            startedAtMillis = value.getLong("startedAtMillis"),
            completedAtMillis = if (value.isNull("completedAtMillis")) {
                null
            } else {
                value.getLong("completedAtMillis")
            },
            result = InstallRunResult.valueOf(value.getString("result")),
            log = value.getString("log"),
        )
    }
}
