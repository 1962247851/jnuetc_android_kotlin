package jn.mjz.aiot.jnuetc.kotlin.model.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.youth.xframe.utils.http.HttpCallBack;
import com.youth.xframe.utils.log.XLog;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import jn.mjz.aiot.jnuetc.kotlin.model.application.App;
import jn.mjz.aiot.jnuetc.kotlin.model.entity.Data;
import jn.mjz.aiot.jnuetc.kotlin.model.entity.MyResponse;
import jn.mjz.aiot.jnuetc.kotlin.model.greendao.DataDao;

/**
 * @author 19622
 */
public class FileUtil {

    public static void exportDatasToExcel(AppCompatActivity activity, Uri uri, IOnExportListener onExportListener) {
        // 设置第一行名
        String[] title = {"报修序号", "报修时间", "状态", "报修人", "学院", "年级", "电话", "QQ", "园区", "南北区", "设备型号", "问题详情", "维修人", "接单时间", "维修时间", "对用户电脑水平评估", "服务内容", "故障描述及解决过程"};
        HSSFWorkbook workbook = new HSSFWorkbook();

        HSSFSheet sheet = workbook.createSheet();

        HSSFRow row = sheet.createRow(0);
        HSSFCell cell;

        // 写入第一行
        for (int i = 0; i < title.length; i++) {
            cell = row.createCell(i);
            cell.setCellValue(title[i]);
        }
        DataDao dataDao = App.daoSession.getDataDao();
        List<Data> dataList = dataDao.loadAll();

        //写入数据
        for (int i = 1; i <= dataList.size(); i++) {

            HSSFRow nextrow = sheet.createRow(i);
            Data data = dataList.get(i - 1);
            HSSFCell cell2 = nextrow.createCell(0);
            cell2.setCellValue(data.getId());

            cell2 = nextrow.createCell(1);
            cell2.setCellValue(DateUtil.getDateAndTime(data.getDate(), " "));

            cell2 = nextrow.createCell(2);
            cell2.setCellValue(data.getState() == 0 ? "未处理" : data.getState() == 1 ? "已接单" : "已维修");

            cell2 = nextrow.createCell(3);
            cell2.setCellValue(data.getName());

            cell2 = nextrow.createCell(4);
            cell2.setCellValue(data.getCollege());

            cell2 = nextrow.createCell(5);
            cell2.setCellValue(data.getGrade());

            cell2 = nextrow.createCell(6);
            cell2.setCellValue(data.getTel());

            cell2 = nextrow.createCell(7);
            cell2.setCellValue(data.getQq());

            cell2 = nextrow.createCell(8);
            cell2.setCellValue(data.getLocal());

            cell2 = nextrow.createCell(9);
            cell2.setCellValue(data.getDistrict() == 0 ? "北区" : "南区");

            cell2 = nextrow.createCell(10);
            cell2.setCellValue(data.getModel());

            cell2 = nextrow.createCell(11);
            cell2.setCellValue(data.getMessage());

            cell2 = nextrow.createCell(12);
            cell2.setCellValue(data.getRepairer());

            cell2 = nextrow.createCell(13);
            cell2.setCellValue(data.getState() != 0 ? DateUtil.getDateAndTime(data.getOrderDate(), " ") : "");

            cell2 = nextrow.createCell(14);
            cell2.setCellValue(data.getState() == 2 ? DateUtil.getDateAndTime(data.getRepairDate(), " ") : "");

            cell2 = nextrow.createCell(15);
            cell2.setCellValue(data.getMark());

            cell2 = nextrow.createCell(16);
            cell2.setCellValue(data.getService());

            cell2 = nextrow.createCell(17);
            cell2.setCellValue(data.getRepairMessage());

        }

        try {
            FileOutputStream fos = getFosFromUri(activity.getContentResolver(), uri, "rw");
            workbook.write(fos);
            fos.close();
            onExportListener.onSuccess();
        } catch (IOException e) {
            e.printStackTrace();
            onExportListener.onError();
        }
    }

    public static void uploadTipDp(final String fileName, final File file, final HttpCallBack<Boolean> callBack) {
        if (file != null && file.length() != 0) {
            HttpUtil.Post.uploadFile(HttpUtil.Urls.File.UPLOAD, fileName, "/opt/dayDP/", file, new HttpUtil.HttpUtilCallBack<String>() {
                @Override
                public void onResponse(@NotNull String result) {
                    MyResponse myResponse = new MyResponse(result);
                    if (myResponse.getError() == MyResponse.SUCCESS) {
                        callBack.onSuccess(true);
                    } else {
                        callBack.onFailed(myResponse.getMsg());
                    }
                }

                @Override
                public void onFailure(@NotNull String error) {
                    callBack.onFailed(error);
                }
            });
        } else {
            callBack.onFailed("未选择文件");
        }
    }

    public static void createFile(AppCompatActivity activity, String mimeType, String fileName) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

        // Filter to only show results that can be "opened", such as
        // a file (as opposed to a list of contacts or timezones).
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Create a file with the requested MIME type.
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        activity.startActivityForResult(intent, 1234);
    }

    public interface IOnExportListener {
        void onSuccess();

        void onError();
    }

    /**
     * @param contentResolver contentResolver
     * @param uri             uri The URI whose file is to be opened.
     * @param mode            mode Access mode for the file.  May be "r" for read-only access,
     *                        "rw" for read and write access, or "rwt" for read and write access
     *                        that truncates any existing file.
     * @return fileOutputStream
     */
    public static FileOutputStream getFosFromUri(ContentResolver contentResolver, Uri uri, @NotNull String mode) {
        ParcelFileDescriptor pfd = null;
        FileOutputStream fileOutputStream = null;
        try {
            pfd = contentResolver.
                    openFileDescriptor(uri, mode);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (pfd != null && pfd.getFileDescriptor() != null) {
            fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
        }
        return fileOutputStream;
    }

    public static Bitmap getBitmapFromUri(ContentResolver contentResolver, Uri uri, @NotNull String mode) {
        Bitmap bitmap = null;
        ParcelFileDescriptor pfd = null;
        try {
            pfd = contentResolver.
                    openFileDescriptor(uri, mode);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (pfd != null && pfd.getFileDescriptor() != null) {
            bitmap = BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor());
        }
        return bitmap;
    }

    /**
     * 从uri拿到file对象
     * {"path":"/storage/emulated/0/DCIM/Screenshots/Screenshot_2019-12-28-09-57-56-780_com.superlib.png"}
     *
     * @param uri     uri
     * @param context context
     * @return File
     */
    public static File uriToFile(Uri uri, Context context) {
        String path = null;
        if ("file".equals(uri.getScheme())) {
            path = uri.getEncodedPath();
            if (path != null) {
                path = Uri.decode(path);
                ContentResolver cr = context.getContentResolver();
                StringBuffer buff = new StringBuffer();
                buff.append("(").append(MediaStore.Images.ImageColumns.DATA).append("=").append("'" + path + "'").append(")");
                Cursor cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA}, buff.toString(), null, null);
                int index = 0;
                int dataIdx = 0;
                for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                    index = cur.getColumnIndex(MediaStore.Images.ImageColumns._ID);
                    index = cur.getInt(index);
                    dataIdx = cur.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    path = cur.getString(dataIdx);
                }
                cur.close();
                if (index == 0) {
                } else {
                    Uri u = Uri.parse("content://media/external/images/media/" + index);
                    System.out.println("temp uri is :" + u);
                }
            }
            if (path != null) {
                return new File(path);
            }
        } else if ("content".equals(uri.getScheme())) {
            // 4.2.2以后
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                path = cursor.getString(columnIndex);
            }
            cursor.close();

            return new File(path);
        } else {
            //Log.i(TAG, "Uri Scheme:" + uri.getScheme());
        }
        return null;
    }

    private File uri2File(Uri uri, Activity activity) {
        //uri转换成file
        String[] arr = {MediaStore.Images.Media.DATA};
        Cursor cursor = activity.managedQuery(uri, arr, null, null, null);
        int imgIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String imgPath = cursor.getString(imgIndex);
        return new File(imgPath);
    }

    public static void saveImage(@NonNull String path, @NonNull Bitmap bitmap) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            XLog.e(e, "保存图片出错");
            e.printStackTrace();
        }
    }

    @Nullable
    public static Bitmap openImage(@NonNull String path) {
        Bitmap bitmap = null;
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path));
            bitmap = BitmapFactory.decodeStream(bis);
            bis.close();
        } catch (IOException e) {
            XLog.e(e, "读取图片出错");
            e.printStackTrace();
        }
        return bitmap;
    }
}
