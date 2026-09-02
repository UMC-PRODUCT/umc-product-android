package com.example.mypage.qrcode

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.EnumMap

/**
 * ZXing 라이브러리 기반 QR 코드 비트맵 생성, 갤러리 저장 및 외부 공유 URI 발급을 담당하는 유틸리티 싱글톤 객체
 *
 * 명함 딥링크 URL을 비트맵 이미지로 렌더링하고, MediaStore API를 이용한 파일 저장과 FileProvider 기반의 외부 공유 기능을 전적으로 처리합니다.
 *
 * 주요 동작 흐름:
 * 1. generateQrCode 메서드를 호출하여 딥링크 텍스트를 UTF-8 인코딩 형태의 ZXing BitMatrix로 변환하고 ImageBitmap 객체를 반환합니다.
 * 2. saveImageToGallery 메서드로 사용자의 단말 갤러리 내 Pictures/UserCardQR 폴더 경로에 100% 압축률의 PNG 파일로 비동기 기입합니다.
 * 3. getShareableImageUri 메서드로 시스템 캐시 폴더(cacheDir/images)에 임시 이미지를 생성한 후 FileProvider URI를 추출해 인텐트 공유를 지원합니다.
 *
 */
object QrCodeUtils {

    /**
     * 문자열 텍스트 데이터(인텐트 URL)를 ZXing 그래픽 행렬로 인코딩하여 Compose ImageBitmap으로 반환하는 메서드
     *
     * @param content QR 코드 내부에 매핑할 텍스트 문자열
     * @param size 생성될 정사각형 QR 비트맵의 가로/세로 픽셀 크기 (기본값: 512px)
     * @return 렌더링 완료된 ImageBitmap 객체 (실패 시 null 반환)
     */
    fun generateQrCode(content: String, size: Int = 512): ImageBitmap? {
        return try {
            val writer = QRCodeWriter()

            //한글 깨짐 방지
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
                put(EncodeHintType.MARGIN, 1) //여백을 줄여 QR 인식률 향상 (선택)
            }

            // ZXing 비트 행렬 생성
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            // BitMatrix 데이터를 안드로이드 픽셀 데이터로 변환 바인딩
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

    /**
     * 생성된 ImageBitmap을 시스템 MediaStore API를 통해 사용자 기기의 갤러리에 PNG 파일로 기입 저장하는 메서드
     *
     * @param context 애플리케이션 컨텍스트
     * @param imageBitmap 저장할 ImageBitmap 객체
     * @param title 저장 파일 접두사명
     * @return 갤러리 저장 성공 여부 (Boolean)
     */
    fun saveImageToGallery(context: Context, imageBitmap: ImageBitmap, title: String = "MyUserCard_QR"): Boolean {
        val bitmap = imageBitmap.asAndroidBitmap()
        val filename = "${title}_${System.currentTimeMillis()}.png"
        var outputStream: OutputStream? = null

        return try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")

                // Android 10 (API 29) 이상 범위 세분화 경로 지정
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

    /**
     * QR 코드 이미지를 내부 캐시 영역에 임시 작성하고 외부 카카오톡/메일 등으로 공유 가능한 Content URI를 생성하는 메서드
     *
     * @param context 애플리케이션 컨텍스트
     * @param imageBitmap 공유할 QR 이미지 비트맵
     * @return FileProvider를 통해 인스턴스화된 공유 가능 Uri (실패 시 null 반환)
     */
    fun getShareableImageUri(context: Context, imageBitmap: ImageBitmap): Uri? {
        return try {
            val bitmap = imageBitmap.asAndroidBitmap()
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs() // 폴더 생성

            val file = File(cachePath, "shared_qr_code.png")
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.close()

            // FileProvider를 통해 외부 공유 가능한 Content Uri 생성
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}