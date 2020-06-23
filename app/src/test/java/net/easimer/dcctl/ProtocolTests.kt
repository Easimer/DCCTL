package net.easimer.dcctl

import kotlinx.io.ByteArrayInputStream
import kotlinx.io.ByteArrayOutputStream
import net.easimer.dcctl.scripting.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Tests for script serialization/deserialization.
 */
class ProtocolTests {
    @Test
    fun emptyScriptTest() {
        val emptyScript = Script(emptyList())

        val baos = ByteArrayOutputStream()
        val ser = ScriptSerializer(baos)
        ser.serialize(emptyScript)
        ser.close()
        val buf = baos.toByteArray()

        val bais = ByteArrayInputStream(buf)
        val deser = ScriptDeserializer(bais)
        val script = deser.deserialize()
        deser.close()
        assertNotNull(script)
        if(script != null) {
            assertEquals(emptyScript.commands, script.commands)
        }
    }

    @Test
    fun allCommandsAreSerializableTest() {
        val S = createScript()

        val baos = ByteArrayOutputStream()
        val ser = ScriptSerializer(baos)
        ser.serialize(S)
        ser.close()
    }

    @Test
    fun scriptTest() {
        val S = createScript()

        val baos = ByteArrayOutputStream()
        val ser = ScriptSerializer(baos)
        ser.serialize(S)
        ser.close()
        val buf = baos.toByteArray()

        val bais = ByteArrayInputStream(buf)
        val deser = ScriptDeserializer(bais)
        val script = deser.deserialize()
        deser.close()
        assertNotNull(script)
        if(script != null) {
            assertEquals(S.commands, script.commands)
        }
    }

    /**
     * Create a Script that contains all kinds of commands
     */
    private fun createScript(): Script {
        val subclasses = ScriptCommand::class.sealedSubclasses.toMutableSet()

        val ret = Script(listOf(
            ScriptCommand.Wait(5.0f),
            ScriptCommand.CaptureMultiple(1.0f, 3),
            ScriptCommand.AudioSignal(SoundEffect.Blip),
            ScriptCommand.Blink(0.75f)
        ))

        // Make sure there is an item for every subclass of ScriptCommand
        ret.commands.forEach {
            subclasses.remove(it::class)
        }

        assertEquals(
            "Couldn't find one or more subclasses of ScriptCommand in the list above!",
            0, subclasses.size)

        return ret
    }
}
