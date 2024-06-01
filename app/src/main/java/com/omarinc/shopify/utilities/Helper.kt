package com.omarinc.shopify.utilities

import com.omarinc.shopify.productdetails.model.Comment

object Helper {
    fun getRandomComments(comments: List<Comment>, count: Int): List<Comment> {
        return comments.shuffled().take(count)
    }

    fun generateStaticComments(): List<Comment> {
        return listOf(
            Comment("Ahmed", "Great product, very satisfied!", 5.0f),
            Comment("Sara", "Good value for money.", 4.0f),
            Comment("Mohamed", "Not bad, but could be better.", 3.0f),
            Comment("Mona", "Loved it! Highly recommend.", 5.0f),
            Comment("Omar", "Average quality, but decent for the price.", 3.5f),
            Comment("Aya", "Excellent quality and fast shipping.", 5.0f),
            Comment("Hassan", "Not what I expected.", 2.0f),
            Comment("Nour", "Fantastic product, will buy again.", 5.0f),
            Comment("Youssef", "Okay, but had some issues with delivery.", 3.0f),
            Comment("Hana", "Perfect! Just what I needed.", 5.0f),
            Comment("Kareem", "Quality is not as advertised.", 2.5f),
            Comment("Dina", "Very happy with my purchase.", 4.5f),
            Comment("Ali", "Not bad.", 3.0f),
            Comment("Layla", "Absolutely love it! Great buy.", 5.0f),
            Comment("Tamer", "Product arrived damaged.", 1.0f),
            Comment("Fatma", "Surprisingly good quality.", 4.5f),
            Comment("Adel", "Not worth the price.", 2.0f),
            Comment("Salma", "Exceeded my expectations!", 5.0f),
            Comment("Ibrahim", "Wouldn't recommend.", 2.0f),
            Comment("Reem", "Good, but shipping took too long.", 3.5f),
        )
    }


}