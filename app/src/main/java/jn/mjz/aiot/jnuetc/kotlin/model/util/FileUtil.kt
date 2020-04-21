package jn.mjz.aiot.jnuetc.kotlin.model.util

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.youth.xframe.utils.http.HttpCallBack
import com.youth.xframe.utils.http.XHttp
import com.youth.xframe.utils.log.XLog
import com.youth.xframe.utils.permission.XPermission
import com.youth.xframe.widget.XToast
import jn.mjz.aiot.jnuetc.kotlin.R
import jn.mjz.aiot.jnuetc.kotlin.model.application.App
import jn.mjz.aiot.jnuetc.kotlin.model.entity.MyResponse
import jn.mjz.aiot.jnuetc.kotlin.model.util.HttpUtil.HttpUtilCallBack
import org.apache.poi.hssf.usermodel.HSSFCell
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io.*

/**
 * @author 19622
 */
class FileUtil {

    interface IOnExportListener {
        fun onSuccess()
        fun onError()
    }

    private fun uri2File(uri: Uri, activity: Activity): File {
        //uri转换成file
        val arr = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = activity.managedQuery(uri, arr, null, null, null)
        val imgIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val imgPath = cursor.getString(imgIndex)
        return File(imgPath)
    }

    companion object {
        private val IMAGE_EXTENSIONS = listOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
        private val AUDIO_EXTENSIONS =
            listOf("wav", "aif", "au", "mp3", "ram", "wma", "mmf", "amr", "aac", "flac")
        private val VIDEO_EXTENSIONS = listOf(
            "mpeg",
            "mpg",
            "mp4",
            "m4v",
            "mov",
            "3gp",
            "3gpp",
            "3g2",
            "3gpp2",
            "mkv",
            "webm",
            "ts",
            "avi"
        )

        @JvmStatic
        fun isImage(extensionWithoutDot: String?): Boolean {
            return extensionWithoutDot != null && IMAGE_EXTENSIONS.indexOf(extensionWithoutDot) != -1
        }

        @JvmStatic
        fun isVideo(extensionWithoutDot: String?): Boolean {
            return extensionWithoutDot != null && VIDEO_EXTENSIONS.indexOf(extensionWithoutDot) != -1
        }

        @JvmStatic
        fun isAudio(extensionWithoutDot: String?): Boolean {
            return extensionWithoutDot != null && AUDIO_EXTENSIONS.indexOf(extensionWithoutDot) != -1
        }

        @JvmStatic
        fun exportDatasToExcel(
            activity: AppCompatActivity,
            uri: Uri?,
            onExportListener: IOnExportListener
        ) {
            // 设置第一行名
            val title = arrayOf(
                "报修序号",
                "报修时间",
                "状态",
                "报修人",
                "学院",
                "年级",
                "电话",
                "QQ",
                "园区",
                "南北区",
                "设备型号",
                "问题详情",
                "维修人",
                "接单时间",
                "维修时间",
                "对用户电脑水平评估",
                "服务内容",
                "故障描述及解决过程"
            )
            val workbook = HSSFWorkbook()
            val sheet = workbook.createSheet()
            val row = sheet.createRow(0)
            var cell: HSSFCell

            // 写入第一行
            for (i in title.indices) {
                cell = row.createCell(i)
                cell.setCellValue(title[i])
            }
            val dataDao = App.daoSession.dataDao
            val dataList =
                dataDao.loadAll()

            //写入数据
            for (i in 1..dataList.size) {
                val nextrow = sheet.createRow(i)
                val data = dataList[i - 1]
                var cell2 = nextrow.createCell(0)
                cell2.setCellValue(data.id.toDouble())
                cell2 = nextrow.createCell(1)
                cell2.setCellValue(
                    DateUtil.getDateAndTime(
                        data.date,
                        " "
                    )
                )
                cell2 = nextrow.createCell(2)
                cell2.setCellValue(
                    when {
                        data.state.toInt() == 0 -> "未处理"
                        data.state
                            .toInt() == 1 -> "已接单"
                        else -> "已维修"
                    }
                )
                cell2 = nextrow.createCell(3)
                cell2.setCellValue(data.name)
                cell2 = nextrow.createCell(4)
                cell2.setCellValue(data.college)
                cell2 = nextrow.createCell(5)
                cell2.setCellValue(data.grade)
                cell2 = nextrow.createCell(6)
                cell2.setCellValue(data.tel)
                cell2 = nextrow.createCell(7)
                cell2.setCellValue(data.qq)
                cell2 = nextrow.createCell(8)
                cell2.setCellValue(data.local)
                cell2 = nextrow.createCell(9)
                cell2.setCellValue(if (data.district.toInt() == 0) "北区" else "南区")
                cell2 = nextrow.createCell(10)
                cell2.setCellValue(data.model)
                cell2 = nextrow.createCell(11)
                cell2.setCellValue(data.message)
                cell2 = nextrow.createCell(12)
                cell2.setCellValue(data.repairer)
                cell2 = nextrow.createCell(13)
                cell2.setCellValue(
                    if (data.state
                            .toInt() != 0
                    ) DateUtil.getDateAndTime(
                        data.orderDate,
                        " "
                    ) else ""
                )
                cell2 = nextrow.createCell(14)
                cell2.setCellValue(
                    if (data.state
                            .toInt() == 2
                    ) DateUtil.getDateAndTime(
                        data.repairDate,
                        " "
                    ) else ""
                )
                cell2 = nextrow.createCell(15)
                cell2.setCellValue(data.mark)
                cell2 = nextrow.createCell(16)
                cell2.setCellValue(data.service)
                cell2 = nextrow.createCell(17)
                cell2.setCellValue(data.repairMessage)
            }
            try {
                val fos =
                    getFosFromUri(activity.contentResolver, uri, "rw")
                workbook.write(fos)
                fos.close()
                onExportListener.onSuccess()
            } catch (e: IOException) {
                e.printStackTrace()
                onExportListener.onError()
            }
        }

        @JvmStatic
        fun uploadTipDp(
            fileName: String?,
            file: File?,
            callBack: HttpCallBack<Boolean>
        ) {
            if (file != null && file.length() != 0L) {
                HttpUtil.Post.uploadFile(
                    HttpUtil.Urls.File.UPLOAD,
                    fileName,
                    "/opt/dayDP/",
                    file,
                    object : HttpUtilCallBack<String?> {
                        override fun onResponse(result: String) {
                            val myResponse = MyResponse(result)
                            if (myResponse.error == MyResponse.SUCCESS) {
                                callBack.onSuccess(true)
                            } else {
                                callBack.onFailed(myResponse.msg)
                            }
                        }

                        override fun onFailure(error: String) {
                            callBack.onFailed(error)
                        }
                    })
            } else {
                callBack.onFailed("未选择文件")
            }
        }

        @JvmStatic
        fun createFile(
            activity: AppCompatActivity,
            mimeType: String?,
            fileName: String?
        ) {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)

            // Filter to only show results that can be "opened", such as
            // a file (as opposed to a list of contacts or timezones).
            intent.addCategory(Intent.CATEGORY_OPENABLE)

            // Create a file with the requested MIME type.
            intent.type = mimeType
            intent.putExtra(Intent.EXTRA_TITLE, fileName)
            activity.startActivityForResult(intent, 1234)
        }

        /**
         * @param contentResolver contentResolver
         * @param uri             uri The URI whose file is to be opened.
         * @param mode            mode Access mode for the file.  May be "r" for read-only access,
         * "rw" for read and write access, or "rwt" for read and write access
         * that truncates any existing file.
         * @return fileOutputStream
         */
        @JvmStatic
        fun getFosFromUri(
            contentResolver: ContentResolver,
            uri: Uri?,
            mode: String
        ): FileOutputStream {
            var pfd: ParcelFileDescriptor? = null
            var fileOutputStream: FileOutputStream? = null
            try {
                pfd = contentResolver.openFileDescriptor(uri!!, mode)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            if (pfd != null && pfd.fileDescriptor != null) {
                fileOutputStream = FileOutputStream(pfd.fileDescriptor)
            }
            return fileOutputStream!!
        }

        fun getBitmapFromUri(
            contentResolver: ContentResolver,
            uri: Uri?,
            mode: String
        ): Bitmap? {
            var bitmap: Bitmap? = null
            var pfd: ParcelFileDescriptor? = null
            try {
                pfd = contentResolver.openFileDescriptor(uri!!, mode)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            if (pfd != null && pfd.fileDescriptor != null) {
                bitmap = BitmapFactory.decodeFileDescriptor(pfd.fileDescriptor)
            }
            return bitmap
        }

        /**
         * 从uri拿到file对象
         * {"path":"/storage/emulated/0/DCIM/Screenshots/Screenshot_2019-12-28-09-57-56-780_com.superlib.png"}
         *
         * @param uri     uri
         * @param context context
         * @return File
         */
        @JvmStatic
        fun uriToFile(uri: Uri, context: Context): File? {
            var path: String? = null
            if ("file" == uri.scheme) {
                path = uri.encodedPath
                if (path != null) {
                    path = Uri.decode(path)
                    val cr = context.contentResolver
                    val buff = StringBuffer()
                    buff.append("(").append(MediaStore.Images.ImageColumns.DATA).append("=")
                        .append("'$path'").append(")")
                    val cur = cr.query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        arrayOf(
                            MediaStore.Images.ImageColumns._ID,
                            MediaStore.Images.ImageColumns.DATA
                        ),
                        buff.toString(),
                        null,
                        null
                    )
                    var index = 0
                    var dataIdx = 0
                    cur!!.moveToFirst()
                    while (!cur.isAfterLast) {
                        index = cur.getColumnIndex(MediaStore.Images.ImageColumns._ID)
                        index = cur.getInt(index)
                        dataIdx = cur.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                        path = cur.getString(dataIdx)
                        cur.moveToNext()
                    }
                    cur.close()
                    if (index == 0) {
                    } else {
                        val u =
                            Uri.parse("content://media/external/images/media/$index")
                        println("temp uri is :$u")
                    }
                }
                if (path != null) {
                    return File(path)
                }
            } else if ("content" == uri.scheme) {
                // 4.2.2以后
                val proj =
                    arrayOf(MediaStore.Images.Media.DATA)
                val cursor =
                    context.contentResolver.query(uri, proj, null, null, null)
                if (cursor!!.moveToFirst()) {
                    val columnIndex =
                        cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    path = cursor.getString(columnIndex)
                }
                cursor.close()
                return File(path)
            } else {
                //Log.i(TAG, "Uri Scheme:" + uri.getScheme());
            }
            return null
        }

        @JvmStatic
        fun saveImage(path: String, bitmap: Bitmap) {
            try {
                val bos =
                    BufferedOutputStream(FileOutputStream(path))
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
                bos.flush()
                bos.close()
            } catch (e: IOException) {
                XLog.e(e, "保存图片出错")
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun openImage(path: String): Bitmap? {
            var bitmap: Bitmap? = null
            try {
                val bis =
                    BufferedInputStream(FileInputStream(path))
                bitmap = BitmapFactory.decodeStream(bis)
                bis.close()
            } catch (e: IOException) {
                XLog.e(e, "读取图片出错")
                e.printStackTrace()
            }
            return bitmap
        }

        @JvmStatic
        fun saveFileByGlide(
            context: Context,
            url: String,
            child: String
        ) {
            XPermission.requestPermissions(
                context,
                App.REQUEST_EXTERNAL_STROGE,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                object : XPermission.OnPermissionListener {
                    override fun onPermissionGranted() {
                        glideSaveFile(context, url, child)
                    }

                    override fun onPermissionDenied() {
                        XPermission.showTipsDialog(context)
                    }
                })
        }

        @JvmStatic
        private fun glideSaveFile(
            context: Context,
            url: String,
            child: String
        ) {
            Glide.with(context).asBitmap().load(url)
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any,
                        target: com.bumptech.glide.request.target.Target<Bitmap?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any,
                        target: com.bumptech.glide.request.target.Target<Bitmap?>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        saveImageFile(context, child, resource!!)
                        return false
                    }
                }).submit()
        }

        /**
         * 保存图片到picture 目录，Android Q适配，最简单的做法就是保存到公共目录，不用SAF存储
         *
         * @param context
         * @param bitmap
         * @param fileName
         */
        @JvmStatic
        fun addPictureToAlbum(
            context: Context,
            bitmap: Bitmap,
            fileName: String?
        ): Boolean {
            val contentValues = ContentValues()
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            contentValues.put(MediaStore.Images.Media.DESCRIPTION, fileName)
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            val uri = context.contentResolver
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            var outputStream: OutputStream? = null
            try {
                outputStream = context.contentResolver.openOutputStream(uri!!)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream!!.close()
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
            return true
        }


        @JvmStatic
        fun saveImageFile(context: Context, fileName: String, file: Bitmap) {
            val extension = fileName.substringAfterLast(".")
            if (!isImage(extension)) {
                return
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Add a specific media item.
                val contentResolver = context.contentResolver
                // Find all audio files on the primary external storage device.
                // On API <= 28, use VOLUME_EXTERNAL instead.
                val imageCollection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                // Publish a new Image.
                val contentValues = ContentValues().apply {
                    put(
                        MediaStore.Images.Media.DISPLAY_NAME, fileName
                    )
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES
                    )
                    put(
                        MediaStore.Images.Media.IS_PENDING, 0
                    )
                }
                val uri = contentResolver.insert(imageCollection, contentValues)
                    val outputStream: OutputStream?
                try {
                    outputStream = contentResolver.openOutputStream(uri!!)
                    file.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    outputStream!!.close()
                    XHttp.handler.post { XToast.success("图片保存成功") }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                val path =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).path + "/" + context.getString(
                        R.string.app_name
                    )
                val toSave = File("$path/$fileName")
                if (toSave.exists()) {
                    XHttp.handler.post { XToast.info("图片已存在") }
                } else {
                    createFile(toSave)
                    file.compress(Bitmap.CompressFormat.JPEG, 100, toSave.outputStream())
                    XHttp.handler.post { XToast.success("图片成功保存在\n${path}/${fileName}") }
                }
            }
        }

        @JvmStatic
        fun createFile(file: File) {
            try {
                if (file.parentFile!!.exists()) {
                    file.createNewFile()
                } else {
                    createDir(file.parentFile!!.absolutePath)
                    file.createNewFile()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun createDir(dirPath: String): String {
            try {
                val file = File(dirPath)
                if (file.parentFile!!.exists()) {
                    file.mkdir()
                    return file.absolutePath
                } else {
                    createDir(file.parentFile!!.absolutePath)
                    file.mkdir()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return dirPath
        }
    }
}