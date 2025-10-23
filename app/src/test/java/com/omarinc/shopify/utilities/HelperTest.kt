package com.omarinc.shopify.utilities

import android.graphics.Color
import com.omarinc.shopify.productdetails.model.Comment
import org.junit.Assert.*
import org.junit.Test
import java.util.regex.Pattern

class HelperTest {

    @Test
    fun getRandomComments_WithValidList_ShouldReturnRandomComments() {
        val comments = Helper.generateStaticComments()
        val randomComments = Helper.getRandomComments(comments, 5)
        assertEquals(5, randomComments.size)
    }

    @Test
    fun generateStaticComments_ShouldReturnCorrectNumberOfComments() {
        val comments = Helper.generateStaticComments()
        assertEquals(20, comments.size)
    }

    @Test
    fun getColorFromName_WithValidColorName_ShouldReturnCorrectColor() {
        assertEquals(Color.RED, Helper.getColorFromName("red"))
        assertEquals(Color.GREEN, Helper.getColorFromName("green"))
    }

    @Test
    fun getColorFromName_WithInvalidColorName_ShouldReturnGray() {
        assertEquals(Color.GRAY, Helper.getColorFromName("invalidColor"))
    }

    @Test
    fun encodeEmail_ShouldReplaceDotsWithCommas() {
        val email = "test@example.com"
        val encodedEmail = Helper.encodeEmail(email)
        assertEquals("test@example,com", encodedEmail)
    }

    @Test
    fun decodeEmail_ShouldReplaceCommasWithDots() {
        val encodedEmail = "test@example,com"
        val decodedEmail = Helper.decodeEmail(encodedEmail)
        assertEquals("test@example.com", decodedEmail)
    }

    @Test
    fun validateEmail_WithValidEmail_ShouldReturnTrue() {
        assertTrue(Helper.validateEmail("test@example.com"))
    }

    @Test
    fun validateEmail_WithInvalidEmail_ShouldReturnFalse() {
        assertFalse(Helper.validateEmail("invalid-email"))
    }

    @Test
    fun validatePassword_WithValidPassword_ShouldReturnTrue() {
        assertTrue(Helper.validatePassword("Valid123"))
    }

    @Test
    fun validatePassword_WithInvalidPassword_ShouldReturnFalse() {
        assertFalse(Helper.validatePassword("invalid"))
    }

    @Test
    fun validatePhoneNumber_WithValidPhoneNumber_ShouldReturnTrue() {
        assertTrue(Helper.validatePhoneNumber("+1234567890"))
    }

    @Test
    fun validatePhoneNumber_WithInvalidPhoneNumber_ShouldReturnFalse() {
        assertFalse(Helper.validatePhoneNumber("invalid"))
    }
}
