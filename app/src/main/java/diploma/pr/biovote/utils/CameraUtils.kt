package diploma.pr.biovote.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream

/**
 * Утиліти для перетворення [ImageProxy] у [Bitmap].
 */
object CameraUtils {

    /**
     * Перетворює [ImageProxy] (формат NV21) у [Bitmap].
     */
    fun ImageProxy.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer
        val vuBuffer = planes[2].buffer

        val ySize = yBuffer.remaining()
        val vuSize = vuBuffer.remaining()
        val nv21 = ByteArray(ySize + vuSize)

        yBuffer.get(nv21, 0, ySize)
        vuBuffer.get(nv21, ySize, vuSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val outputStream = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 70, outputStream)

        val byteArray = outputStream.toByteArray()
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    /**
     * Статична функція для зручного виклику перетворення.
     * Вона також автоматично закриває [ImageProxy].
     */
    fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        return try {
            image.toBitmap()
        } finally {
            image.close()
        }
    }
}