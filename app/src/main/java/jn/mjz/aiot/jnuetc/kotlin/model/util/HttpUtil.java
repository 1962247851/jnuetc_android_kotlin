package jn.mjz.aiot.jnuetc.kotlin.model.util;

import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;

import androidx.appcompat.app.AppCompatActivity;

import com.youth.xframe.XFrame;
import com.youth.xframe.utils.http.XHttp;
import com.youth.xframe.utils.log.XLog;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import jn.mjz.aiot.jnuetc.kotlin.model.entity.MyResponse;
import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static okhttp3.internal.Util.EMPTY_REQUEST;

/**
 * @author 19622C
 */
public class HttpUtil {

    private static final String HOST = "https://www.10000hours.love:444/";
    private static final int CACHE_SIZE = 10 * 1024 * 1024;
    private static OkHttpClient client;

    public static final class Urls {

        public static final class Code {
            private static final String CODE = HOST + "code/";
            public static final String INSERT = CODE + "insert";
        }

        public static final class Version {
            private static final String VERSION = HOST + "version/";
            public static final String QUERY_ALL = VERSION + "queryAll";
            public static final String INSERT = VERSION + "insert";
        }

        public static final class User {
            private static final String USER = HOST + "user/";

            public static final String LOGIN = USER + "login";
            public static final String QUERY_ALL = USER + "queryAll";
            public static final String MODIFY = USER + "modify";
        }

        public static final class Data {
            private static final String DATA = HOST + "data/";

            public static final String QUERY_ALL = DATA + "queryAll";
            public static final String QUERY_BY_ID = DATA + "queryById";
            public static final String UPDATE = DATA + "update";
            public static final String INSERT = DATA + "insert";
            public static final String DELETE_BY_ID_LIST = DATA + "deleteByIdList";
        }

        public static final class State {
            private static final String STATE = HOST + "state/";
            public static final String CHECK_SERVICE = STATE + "checkService";
            public static final String CHANGE_SERVICE = STATE + "changeService";
        }

        public static final class MingJuURL {
            private static final String MING_JU = HOST + "mingju/";
            public static final String GET_MING_JU = MING_JU + "getMingJu";
        }

        public static final class File {
            private static final String FILE = HOST + "file/";

            public static final String DOWNLOAD = FILE + "download";
            public static final String UPLOAD = FILE + "upload";
        }

    }

    public static class Post {

        public static void postJson(String url, String jsonString, HttpUtilCallBack<String> callback) {
            initClient();
            RequestBody body = RequestBody.create(jsonString, MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    XHttp.handler.post(() -> callback.onFailure(e.getMessage() == null ? "" : e.getMessage()));
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String result = response.body().string();
                        XHttp.handler.post(() -> callback.onResponse(result));
                    } else {
                        XHttp.handler.post(() -> callback.onFailure(response.message()));
                    }
                }

            });
        }

        public static void uploadFile(String url, String fileName, String path, File file, HttpUtilCallBack<String> callback) {
            initClient();

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", fileName,
                            RequestBody.create(file, MediaType.parse("multipart/form-data")))
                    .build();

            Request request = new Request.Builder()
                    .url(url + "?path=" + path + "&fileName=" + fileName)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    XHttp.handler.post(() -> callback.onFailure(e.getMessage() == null ? "" : e.getMessage()));
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String result = response.body().string();
                        XHttp.handler.post(() -> callback.onResponse(result));
                    } else {
                        XHttp.handler.post(() -> callback.onFailure(response.message()));
                    }
                }
            });
        }

        public static void uploadHaveResponse(String url, Map<String, Object> params, HttpUtilCallBack callBack) {
            initClient();
            RequestBody body = EMPTY_REQUEST;
            if (null != params && !params.isEmpty()) {
                MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                for (String key : params.keySet()) {
                    Object value = params.get(key);
                    if (value != null) {
                        if (value instanceof File) {
                            File file = (File) value;
                            builder.addFormDataPart(key, file.getName(),
                                    RequestBody.create(file, MediaType.parse("application/octet-stream")));//"application/octet-stream"
                        } else {
                            builder.addFormDataPart(key, value.toString());
                        }
                    }
                }
                body = builder.build();
            }
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    XLog.e(e.getMessage());
                    XHttp.handler.post(() -> callBack.onFailure(e.getMessage() == null ? "" : e.getMessage()));
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String result = response.body().string();
                        XLog.json(result);
                        XHttp.handler.post(() -> {
                            try {
                                callBack.onResponse(new MyResponse(result));
                            } catch (Exception e) {
                                callBack.onFailure(response.message());
                            }
                        });
                    } else {
                        XLog.e(response.message());
                        XHttp.handler.post(() -> callBack.onFailure(response.message()));
                    }
                }
            });
        }
    }

    public static class Get {

        public static void downloadFile(AppCompatActivity activity, Uri uri, String url, Map<String, Object> params, IFileDownloadListener fileDownloadListener) {
            initClient();
            Request request = new Request.Builder().url(url + getUrlParamsByMap(params)).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    XHttp.handler.post(() -> fileDownloadListener.onError(e.getMessage()));
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) {
                    if (response.isSuccessful()) {
                        fileDownloadListener.onStart();
                        InputStream is = null;
                        byte[] buf = new byte[2048];
                        int len;
                        FileOutputStream fos = null;
                        //储存下载文件
                        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//                            Log.e("onResponse: ", "Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)");
                        }
//                                File file = new File(activity.getExternalFilesDir(null), fileName);
//                                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);//Q用不了
                        try {
                            ParcelFileDescriptor pfd = activity.getContentResolver().
                                    openFileDescriptor(uri, "w");
                            fos = new FileOutputStream(pfd.getFileDescriptor());
//                            if (file.exists()) {
//                                file.delete();
//                            } else {
//                                file.createNewFile();
//                            }
                            is = response.body().byteStream();
                            long total = response.body().contentLength();
//                            Log.e("onResponse:", " contentLength = " + total);
//                            fos = new FileOutputStream(file);
                            long sum = 0;
                            while ((len = is.read(buf)) != -1) {
                                fos.write(buf, 0, len);
                                sum += len;
                                int progress = (int) (sum * 1.0 / total);
                                fileDownloadListener.onDownloading(progress);
                            }
                            fos.flush();
                            //下载完成
                            XHttp.handler.post(fileDownloadListener::onFinish);
                        } catch (Exception e) {
                            fileDownloadListener.onError(e.getMessage());
                        } finally {
                            try {
                                if (is != null) {
                                    is.close();
                                }
                                if (fos != null) {
                                    fos.close();
                                }
                            } catch (IOException e) {
                                fileDownloadListener.onError(e.getMessage());
                            }
                        }
                    }
                }
            });
        }
    }

    private static void initClient() {
        if (client == null) {
            client = new OkHttpClient().newBuilder()
                    .retryOnConnectionFailure(true)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .cache(new Cache(XFrame.getContext().getCacheDir(), CACHE_SIZE))
                    .build();
        }
    }

    private static String getUrlParamsByMap(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "";
        }
        StringBuilder params = new StringBuilder("?");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            params.append(entry.getKey());
            params.append("=");
            params.append(entry.getValue());
            params.append("&");
        }
        String str = params.toString();
        return str.substring(0, str.length() - 1);
    }

    public interface HttpUtilCallBack<T> {
        /**
         * onResponse
         *
         * @param result 结果
         */
        void onResponse(@NotNull T result);

        /**
         * onFailure
         *
         * @param error 错误信息
         */
        void onFailure(@NotNull String error);
    }

    public interface IFileDownloadListener {
        /**
         * 开始下载
         */
        void onStart();

        void onDownloading(int progress);

        void onFinish();

        void onError(String errorMessage);
    }
}
