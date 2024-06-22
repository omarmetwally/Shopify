package com.omarinc.shopify.utilities

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.omarinc.shopify.R
import com.omarinc.shopify.productdetails.model.Comment
import java.util.regex.Pattern

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

    fun getColorFromName(colorName: String): Int {
        return when (colorName.toLowerCase()) {
            "red" -> Color.RED
            "green" -> Color.GREEN
            "blue" -> Color.BLUE
            "black" -> Color.BLACK
            "white" -> Color.WHITE
            "burgundy" -> Color.parseColor("#800020")
            "burgandy" -> Color.parseColor("#800020")
            "yellow" -> Color.YELLOW
            "cyan" -> Color.CYAN
            "magenta" -> Color.MAGENTA
            "gray" -> Color.GRAY
            "dark_gray" -> Color.DKGRAY
            "light_gray" -> Color.LTGRAY
            "purple" -> Color.parseColor("#800080")
            "orange" -> Color.parseColor("#FFA500")
            "pink" -> Color.parseColor("#FFC0CB")
            "brown" -> Color.parseColor("#A52A2A")
            "beige" -> Color.parseColor("#F5F5DC")
            "olive" -> Color.parseColor("#808000")
            "maroon" -> Color.parseColor("#800000")
            "navy" -> Color.parseColor("#000080")
            "teal" -> Color.parseColor("#008080")
            "lime" -> Color.parseColor("#00FF00")
            "indigo" -> Color.parseColor("#4B0082")
            "violet" -> Color.parseColor("#EE82EE")
            "gold" -> Color.parseColor("#FFD700")
            "silver" -> Color.parseColor("#C0C0C0")
            "turquoise" -> Color.parseColor("#40E0D0")
            "coral" -> Color.parseColor("#FF7F50")
            "aqua" -> Color.parseColor("#00FFFF")
            "chocolate" -> Color.parseColor("#D2691E")
            "crimson" -> Color.parseColor("#DC143C")
            "fuchsia" -> Color.parseColor("#FF00FF")
            "khaki" -> Color.parseColor("#F0E68C")
            "lavender" -> Color.parseColor("#E6E6FA")
            "plum" -> Color.parseColor("#DDA0DD")
            "salmon" -> Color.parseColor("#FA8072")
            "sienna" -> Color.parseColor("#A0522D")
            "tan" -> Color.parseColor("#D2B48C")
            else -> Color.GRAY
        }
    }

    fun encodeEmail(email: String): String {
        return email.replace(".", ",")
    }

    fun decodeEmail(encodedEmail: String): String {
        return encodedEmail.replace(",", ".")
    }

    fun showAlertDialog(
        context: Context,
        title: String,
        message: String,
        positiveButtonText: String,
        positiveButtonAction: (() -> Unit)? = null,
        negativeButtonText: String? = null,
        negativeButtonAction: (() -> Unit)? = null
    ) {
        val builder = AlertDialog.Builder(context, R.style.CustomAlertDialog)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.alert_dialog, null)
        builder.setView(view)

        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvMessage = view.findViewById<TextView>(R.id.tvMessage)
        val btnPositive = view.findViewById<Button>(R.id.btnPositive)
        val btnNegative = view.findViewById<Button>(R.id.btnNegative)

        tvTitle.text = title
        tvMessage.text = message
        btnPositive.text = positiveButtonText

        val dialog = builder.create()

        btnPositive.setOnClickListener {
            positiveButtonAction?.invoke()
            dialog.dismiss()
        }

        if (negativeButtonText != null) {
            btnNegative.text = negativeButtonText
            btnNegative.visibility = View.VISIBLE
            btnNegative.setOnClickListener {
                negativeButtonAction?.invoke()
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    fun validateEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return Pattern.compile(emailPattern).matcher(email).matches()
    }

    fun validatePassword(password: String): Boolean {
        val passwordPattern = "^(?!.*12345)(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{6,}$"
        return password.matches(passwordPattern.toRegex())
    }
    fun validatePhoneNumber(phoneNumber: String): Boolean {
        val phonePattern = "^\\+?[1-9]\\d{1,14}\$"
        return phoneNumber.matches(phonePattern.toRegex())
    }
    fun isNetworkConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION")
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
    }
}