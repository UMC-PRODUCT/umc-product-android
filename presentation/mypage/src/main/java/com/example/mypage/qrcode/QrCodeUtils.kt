package com.example.mypage.qrcode

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.io.OutputStream
import java.util.EnumMap

object QrCodeUtils {

    /**
     * 텍스트 문자열을 비트맵 이미지로 변환하여 QR 코드로 생성
     * @param content QR 코드에 담을 텍스트 (기기 모델명 등)
     * @param size QR 코드 정사각형 픽셀 크기
     */
    fun generateQrCode(content: String, size: Int = 512): ImageBitmap? {
        return try {
            val writer = QRCodeWriter()

            //한글 깨짐 방지
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
                put(EncodeHintType.MARGIN, 1) //여백을 줄여 QR 인식률 향상 (선택)
            }

            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(
                        x, y,
                        if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                    )
                }
            }
            bitmap.asImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    //ImageBitmap을 갤러리에 저장
    fun saveImageToGallery(context: Context, imageBitmap: ImageBitmap, title: String = "MyUserCard_QR"): Boolean {
        val bitmap = imageBitmap.asAndroidBitmap()
        val filename = "${title}_${System.currentTimeMillis()}.png"
        var outputStream: OutputStream? = null

        return try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/UserCardQR")
                }
            }

            val imageUri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            ) ?: return false

            outputStream = context.contentResolver.openOutputStream(imageUri)
            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            outputStream?.close()
        }
    }
}