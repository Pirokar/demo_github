package im.threads.business.models

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ConsultRoleTest {

    @Test
    fun whenConsultRoleFromStringWithValidRole_thenReturnsCorrectRole() {
        assertEquals(ConsultRole.BOT, ConsultRole.consultRoleFromString("BOT"))
        assertEquals(ConsultRole.EXTERNAL_BOT, ConsultRole.consultRoleFromString("EXTERNAL_BOT"))
        assertEquals(ConsultRole.OPERATOR, ConsultRole.consultRoleFromString("OPERATOR"))
        assertEquals(ConsultRole.SUPERVISOR, ConsultRole.consultRoleFromString("SUPERVISOR"))
        assertEquals(ConsultRole.SYSTEM, ConsultRole.consultRoleFromString("SYSTEM"))
        assertEquals(ConsultRole.INTEGRATION, ConsultRole.consultRoleFromString("INTEGRATION"))
    }

    @Test
    fun whenConsultRoleFromStringWithInvalidRole_thenReturnsOperator() {
        assertEquals(ConsultRole.OPERATOR, ConsultRole.consultRoleFromString("INVALID"))
    }

    @Test
    fun whenConsultRoleFromStringWithNull_thenReturnsOperator() {
        assertEquals(ConsultRole.OPERATOR, ConsultRole.consultRoleFromString(null))
    }
}
