package net.easimer.dcctl.scripting

import kotlinx.serialization.json.Json
import java.io.*

class Deserializer(private val stream : InputStream) {
    fun deserialize(): Script? {
        val dis = DataInputStream(stream)
        val len = dis.readLong()
        val reservedField = dis.readLong()

        if(len > 0) {
            val buf = ByteArray(len.toInt())
            dis.readFully(buf)
            val deserializer = Script.serializer()

            val json = String(buf, Charsets.UTF_8)
            val j = Json.parseToJsonElement(json)
            return Json.Default.decodeFromJsonElement(deserializer, j)
        } else {
            return null
        }
    }

    fun close() {
        stream.close()
    }
}