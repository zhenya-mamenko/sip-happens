package com.siphappens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ImageHelperTest {

    @Test
    fun `getImageResourceId should return correct resource for imageCode 1`() {
        val imageCode = 1
        val counter = 2
        val maximum = 4

        val result = ImageHelper.getImageResourceId(imageCode, counter, maximum)

        assertEquals(R.drawable.image_1_state_2, result)
    }

    @Test
    fun `getImageResourceId should return state 0 when counter is 0`() {
        val imageCode = 1
        val counter = 0
        val maximum = 4

        val result = ImageHelper.getImageResourceId(imageCode, counter, maximum)

        assertEquals(R.drawable.image_1_state_0, result)
    }

    @Test
    fun `getImageResourceId should return state 4 when counter equals maximum`() {
        val imageCode = 2
        val counter = 5
        val maximum = 5

        val result = ImageHelper.getImageResourceId(imageCode, counter, maximum)

        assertEquals(R.drawable.image_2_state_4, result)
    }

    @Test
    fun `getImageResourceId should return state 5 when counter exceeds maximum`() {
        val imageCode = 3
        val counter = 7
        val maximum = 5

        val result = ImageHelper.getImageResourceId(imageCode, counter, maximum)

        assertEquals(R.drawable.image_3_state_5, result)
    }

    @Test
    fun `getImageResourceId should handle percentage calculation correctly`() {
        val imageCode = 1
        val maximum = 10

        assertEquals(R.drawable.image_1_state_1, ImageHelper.getImageResourceId(imageCode, 2, maximum))
        assertEquals(R.drawable.image_1_state_1, ImageHelper.getImageResourceId(imageCode, 1, maximum))

        assertEquals(R.drawable.image_1_state_2, ImageHelper.getImageResourceId(imageCode, 3, maximum))
        assertEquals(R.drawable.image_1_state_2, ImageHelper.getImageResourceId(imageCode, 5, maximum))

        assertEquals(R.drawable.image_1_state_3, ImageHelper.getImageResourceId(imageCode, 6, maximum))
        assertEquals(R.drawable.image_1_state_3, ImageHelper.getImageResourceId(imageCode, 9, maximum))
    }

    @Test
    fun `getImageResourceId should handle invalid imageCode gracefully`() {
        val invalidImageCode = 999
        val counter = 2
        val maximum = 4

        val result = ImageHelper.getImageResourceId(invalidImageCode, counter, maximum)

        assertEquals(R.drawable.image_1_state_0, result)
    }

    @Test
    fun `getImageResourceId should handle edge case with maximum 1`() {
        val imageCode = 1
        val maximum = 1

        assertEquals(R.drawable.image_1_state_0, ImageHelper.getImageResourceId(imageCode, 0, maximum))
        assertEquals(R.drawable.image_1_state_4, ImageHelper.getImageResourceId(imageCode, 1, maximum))
        assertEquals(R.drawable.image_1_state_5, ImageHelper.getImageResourceId(imageCode, 2, maximum))
    }

    @Test
    fun `getImageResourceId should work for all valid imageCodes`() {
        val counter = 2
        val maximum = 4

        assertNotEquals(0, ImageHelper.getImageResourceId(1, counter, maximum))
        assertNotEquals(0, ImageHelper.getImageResourceId(2, counter, maximum))
        assertNotEquals(0, ImageHelper.getImageResourceId(3, counter, maximum))
    }
}
