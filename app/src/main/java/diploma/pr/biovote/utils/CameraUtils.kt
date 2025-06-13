package diploma.pr.biovote.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream

/**
 * Утиліти для конвертації ImageProxy → Bitmap.
 */
object CameraUtils {
    // Перетворює ImageProxy (NV21) у Bitmap
    fun ImageProxy.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer
        val vuBuffer = planes[2].buffer
        val ySize = yBuffer.remaining()
        val vuSize = vuBuffer.remaining()
        val nv21 = ByteArray(ySize + vuSize)
        yBuffer.get(nv21, 0, ySize)
        vuBuffer.get(nv21, ySize, vuSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 90, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    // Стандартна функція, яку можна викликати так: CameraUtils.imageProxyToBitmap(image)
    fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        return image.toBitmap()
    }
}