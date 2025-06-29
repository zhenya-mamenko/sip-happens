package com.siphappens

object ImageHelper {

    private val imageResources = arrayOf(
        intArrayOf(R.drawable.image_1_state_0, R.drawable.image_1_state_1, R.drawable.image_1_state_2, R.drawable.image_1_state_3, R.drawable.image_1_state_4, R.drawable.image_1_state_5),
        intArrayOf(R.drawable.image_2_state_0, R.drawable.image_2_state_1, R.drawable.image_2_state_2, R.drawable.image_2_state_3, R.drawable.image_2_state_4, R.drawable.image_2_state_5),
        intArrayOf(R.drawable.image_3_state_0, R.drawable.image_3_state_1, R.drawable.image_3_state_2, R.drawable.image_3_state_3, R.drawable.image_3_state_4, R.drawable.image_3_state_5)
    )

    fun getImageResourceId(imageCode: Int, counter: Int, maximum: Int): Int {
        val state = getImageState(counter, maximum)
        return try {
            imageResources[imageCode - 1][state]
        } catch (e: ArrayIndexOutOfBoundsException) {
            imageResources[0][0]
        }
    }

    private fun getImageState(counter: Int, maximum: Int): Int {
        val percentage = (counter * 100) / maximum

        return when {
            counter == 0 -> 0
            counter == maximum -> 4
            counter > maximum -> 5
            percentage <= 25 -> 1
            percentage <= 50 -> 2
            else -> 3
        }
    }
}
