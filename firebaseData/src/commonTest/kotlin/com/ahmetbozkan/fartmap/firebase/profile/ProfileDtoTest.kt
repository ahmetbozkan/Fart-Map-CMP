package com.ahmetbozkan.fartmap.firebase.profile

import com.ahmetbozkan.fartmap.core.domain.profile.NewProfile
import dev.gitlive.firebase.firestore.Timestamp
import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileDtoTest {

    /** Must equal the `hasExactly` create set in backend/firestore.rules (users); optional fields are not sent. */
    @Test
    fun fieldsAreExactlyTheRulesCreateSet() {
        val descriptor = ProfileDto.serializer().descriptor
        val names = (0 until descriptor.elementsCount).map(descriptor::getElementName).toSet()
        assertEquals(
            setOf(
                "user_id",
                "display_name",
                "avatar_id",
                "follower_count",
                "following_count",
                "is_following_list_public",
                "created_at",
                "updated_at",
            ),
            names,
        )
    }

    @Test
    fun newProfileMapsWithServerDefaults() {
        val dto = NewProfile(displayName = "Sneaky Bean 42", avatarId = "avatar_free_bean").toDto("u1")
        assertEquals("u1", dto.userId)
        assertEquals("Sneaky Bean 42", dto.displayName)
        assertEquals("avatar_free_bean", dto.avatarId)
        assertEquals(0, dto.followerCount)
        assertEquals(0, dto.followingCount)
        assertEquals(false, dto.isFollowingListPublic)
        assertEquals(Timestamp.ServerTimestamp, dto.createdAt)
        assertEquals(Timestamp.ServerTimestamp, dto.updatedAt)
    }

    /** No defaults: every rules-required field is encoded even if `encodeDefaults` is off. */
    @Test
    fun noFieldReliesOnADefault() {
        val descriptor = ProfileDto.serializer().descriptor
        val optional = (0 until descriptor.elementsCount).filter(descriptor::isElementOptional)
            .map(descriptor::getElementName)
        assertEquals(emptyList(), optional)
    }
}
