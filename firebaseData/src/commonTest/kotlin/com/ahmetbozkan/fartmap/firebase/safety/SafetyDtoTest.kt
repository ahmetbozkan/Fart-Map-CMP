package com.ahmetbozkan.fartmap.firebase.safety

import com.ahmetbozkan.fartmap.core.domain.safety.ReportReason
import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SafetyDtoTest {

    private fun names(descriptor: SerialDescriptor) =
        (0 until descriptor.elementsCount).map(descriptor::getElementName).toSet()

    /** Must equal the `hasExactly` create set of `match /moderation_reports/{reportId}` in backend/firestore.rules. */
    @Test
    fun reportFieldsAreExactlyTheRulesCreateSet() {
        assertEquals(
            setOf(
                "report_id",
                "reporter_user_id",
                "target_type",
                "target_id",
                "reason_code",
                "status",
                "created_at",
                "updated_at",
            ),
            names(ReportDto.serializer().descriptor),
        )
    }

    @Test
    fun aReportCarriesTheRuleShapedIdAndAnOpenFartTarget() {
        val dto = SafetyDocs.report("bob", "fart-1", ReportReason.MINOR_SAFETY)

        assertEquals("bob_FART_fart-1", dto.reportId, "the rules require {reporter}_{type}_{target}")
        assertEquals("bob", dto.reporterUserId)
        assertEquals("FART", dto.targetType)
        assertEquals("fart-1", dto.targetId)
        assertEquals("MINOR_SAFETY", dto.reasonCode)
        assertEquals("OPEN", dto.status)
        assertEquals(Timestamp.ServerTimestamp, dto.createdAt)
        assertEquals(Timestamp.ServerTimestamp, dto.updatedAt)
    }

    @Test
    fun everyReasonCodeFitsTheRulesLimit() {
        ReportReason.entries.forEach { assertTrue(it.wireName.length in 1..RULES_REASON_MAX, it.wireName) }
    }

    /** Must equal the `hasExactly` create set of `match /blocks/{blockId}`. */
    @Test
    fun blockFieldsAreExactlyTheRulesCreateSet() {
        assertEquals(setOf("blocker_id", "blocked_id", "created_at"), names(BlockDto.serializer().descriptor))
    }

    @Test
    fun aBlockCarriesTheRuleShapedId() {
        val dto = SafetyDocs.block("alice", "bob")

        assertEquals("alice_bob", SafetyDocs.blockId("alice", "bob"))
        assertEquals("alice", dto.blockerId)
        assertEquals("bob", dto.blockedId)
        assertEquals(Timestamp.ServerTimestamp, dto.createdAt)
    }

    @Test
    fun theBlockedSetSkipsMalformedDocs() {
        val docs = listOf(BlockReadDto("bob"), BlockReadDto(""), null, BlockReadDto("carol"), BlockReadDto("bob"))

        assertEquals(setOf("bob", "carol"), SafetyDocs.blockedUids(docs))
    }

    private companion object {
        const val RULES_REASON_MAX = 40
    }
}
