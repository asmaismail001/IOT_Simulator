package com.example.iotsimulator.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.example.iotsimulator.R

// Google Font Provider
private val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// Playfair Display (Serif) for Main Title and Temperature Display
val playfairFont = GoogleFont("Playfair Display")
val SerifFontFamily = FontFamily(
    Font(googleFont = playfairFont, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = playfairFont, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = playfairFont, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = playfairFont, fontProvider = fontProvider, weight = FontWeight.Bold)
)

// Inter (Sans-Serif) for Body, Labels, Buttons, and Network Info
val interFont = GoogleFont("Inter")
val SansFontFamily = FontFamily(
    Font(googleFont = interFont, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = interFont, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = interFont, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = interFont, fontProvider = fontProvider, weight = FontWeight.Bold)
)

val Typography = Typography(
    // Large Hero Temperature Display (Playfair Display, 58sp, SemiBold)
    displayLarge = TextStyle(
        fontFamily = SerifFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 58.sp,
        lineHeight = 62.sp,
        letterSpacing = (-0.5).sp
    ),
    // App Header Main Title (Playfair Display, 28sp)
    headlineMedium = TextStyle(
        fontFamily = SerifFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.3).sp
    ),
    // Card Subtitles & Button Text (Inter, 14sp, semibold)
    titleMedium = TextStyle(
        fontFamily = SansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    ),
    // Body & Detail Text (Inter, 12sp, normal)
    bodyMedium = TextStyle(
        fontFamily = SansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.1.sp
    ),
    // Small Uppercase Metadata Labels (Inter, 11sp, bold, 1.5sp letter spacing)
    labelSmall = TextStyle(
        fontFamily = SansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.5.sp
    )
)