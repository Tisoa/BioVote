@file:Suppress("EXTENSION_SHADOWED_BY_MEMBER")

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
 *
 *  ▸  imageProxyToBitmap(..)  — залишив щоб не ламати існуючі виклики.
 *  ▸  ImageProxy.toBitmap()   — extension-версія (коротший синтаксис).
 */
object CameraUtils {

    /** Сумісний з legacy-кодом (Login/Registration/VotingDetail screen). */
    fun imageProxyToBitmap(image: ImageProxy): Bitmap = image.toBitmap()

    /** Extension-функція, якщо захочеш писати  proxy.toBitmap()  напряму. */
    fun ImageProxy.toBitmap(): Bitmap {
        // --- копіюємо NV21 й конвертуємо через YuvImage ---
        val yBuffer  = planes[0].buffer
        val vuBuffer = planes[2].buffer
        val nv21 = ByteArray(yBuffer.remaining() + vuBuffer.remaining()).also {
            yBuffer.get(it, 0,             yBuffer.remaining())
            vuBuffer.get(it, yBuffer.remaining(), vuBuffer.remaining())
        }

        val yuvImg = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val out    = ByteArrayOutputStream()
        yuvImg.compressToJpeg(Rect(0, 0, width, height), 90, out)
        val bytes  = out.toByteArray()
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}