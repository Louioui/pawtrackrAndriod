package com.example.pawtrackr

import com.example.pawtrackr.domain.messaging.MessageProcessor
import org.junit.Assert.assertEquals
import org.junit.Test

class MessageProcessorTest {
    @Test fun substitutes_owner_and_pet() {
        val out = MessageProcessor.process("Hi [OwnerName], [PetName] is ready!", "María", "Luna")
        assertEquals("Hi María, Luna is ready!", out)
    }

    @Test fun blank_or_null_leaves_placeholder() {
        assertEquals("Hi [OwnerName], Luna is ready!", MessageProcessor.process("Hi [OwnerName], [PetName] is ready!", "  ", "Luna"))
        assertEquals("Hi [OwnerName]!", MessageProcessor.process("Hi [OwnerName]!", null, null))
    }

    @Test fun time_substituted_when_provided() {
        assertEquals("See you at 3:00 PM", MessageProcessor.process("See you at [Time]", null, null, "3:00 PM"))
    }
}
