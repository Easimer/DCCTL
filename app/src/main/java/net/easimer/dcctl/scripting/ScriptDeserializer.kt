package net.easimer.dcctl.scripting

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import java.io.*

class ScriptDeserializer(private val stream : InputStream) {
    @UnstableDefault
    fun deserialize(): Script? {
        val dis = DataInputStream(stream)
        val len = dis.readLong()

        if(len > 0) {
            val buf = ByteArray(len.toInt())
            dis.readFully(buf)
            val deserializer = Script.serializer()

            val json = String(buf, Charsets.UTF_8)
            val j = Json.parseJson(json)
            return Json.Default.fromJson(deserializer, j)
        } else {
            return null
        }
    }

    fun close() {
        stream.close()
    }
}