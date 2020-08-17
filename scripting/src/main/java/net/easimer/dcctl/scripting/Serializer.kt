package net.easimer.dcctl.scripting

import kotlinx.serialization.json.Json
import java.io.DataOutputStream
import java.io.OutputStream

class Serializer(private val stream : OutputStream) {
    fun serialize(script: Script) {
        val dos = DataOutputStream(stream)

        val serializer = Script.serializer()

        val json = Json.encodeToString(serializer, script)

        val bytes = json.toByteArray(Charsets.UTF_8)
        val len = bytes.size.toLong()
        dos.writeLong(len)
        dos.writeLong(0) // reserved field
        dos.write(bytes)

        dos.flush()
    }

    fun close() {
        stream.close()
    }
}