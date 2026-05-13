package com.example.qlydatve.utils

import android.graphics.Bitmap
import android.graphics.Color

object QRGenerator {

    /**
     * Generates a simple placeholder QR bitmap.
     * For production, integrate a library like ZXing or QRGen.
     */
    fun generate(content: String, size: Int = 512): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        // Placeholder: fill with black border and white center
        for (x in 0 until size) {
            for (y in 0 until size) {
                val isBorder = x < 20 || x > size - 20 || y < 20 || y > size - 20
                bitmap.setPixel(x, y, if (isBorder) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }

    fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return android.util.Base64.encodeToString(stream.toByteArray(), android.util.Base64.DEFAULT)
    }
}
