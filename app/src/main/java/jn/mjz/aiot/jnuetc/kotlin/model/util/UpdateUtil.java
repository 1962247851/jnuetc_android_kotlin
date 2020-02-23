package jn.mjz.aiot.jnuetc.kotlin.model.util;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.KeyEvent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.youth.xframe.XFrame;
import com.youth.xframe.utils.XAppUtils;
import com.youth.xframe.utils.XDateUtils;
import com.youth.xframe.utils.http.HttpCallBack;
import com.youth.xframe.utils.http.XHttp;
import com.youth.xframe.widget.XToast;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jn.mjz.aiot.jnuetc.kotlin.R;
import jn.mjz.aiot.jnuetc.kotlin.model.application.App;
import jn.mjz.aiot.jnuetc.kotlin.model.entity.Data;
import jn.mjz.aiot.jnuetc.kotlin.model.entity.MyResponse;
import jn.mjz.aiot.jnuetc.kotlin.model.entity.Version;
import jn.mjz.aiot.jnuetc.kotlin.view.custom.LoadingDialog;

/**
 * @author 19622
 */
public class UpdateUtil {

    public static void checkForUpdate(boolean hideLoadingDialogAndToast, Context context, @Nullable IServerAvailableListener iServerAvailableListener) {
        if (!hideLoadingDialogAndToast) {
            LoadingDialog.with(context).show();
        }
        UpdateUtil.checkForUpdate(new IUpdateListener() {
            @Override
            public void haveNewVersion(@NotNull Version currentVersion, @NotNull Version newVersion) {
                if (iServerAvailableListener != null) {
                    iServerAvailableListener.onServerValid(currentVersion, newVersion);
                }
                if (!hideLoadingDialogAndToast) {
                    LoadingDialog.with(context).cancel();
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                String[] messages = newVersion.getMessage().split("。");
                StringBuilder stringBuilder = new StringBuilder();
                for (String s : messages) {
                    stringBuilder.append(s)
                            .append("\n");
                }
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                builder.setCancelable(false)
                        .setOnKeyListener((dialogInterface, i, keyEvent) -> keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK)
                        .setMessage("更新时间：" + XDateUtils.millis2String(newVersion.getDate(), "yyyy/MM/dd HH:mm:ss") + "\n\n" + stringBuilder.toString())
                        .setTitle("发现新版本" + newVersion.getVersion())
                        .setPositiveButton("前往下载", (dialogInterface, i) -> context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(newVersion.getUrl()))));
                AlertDialog alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
                //防止点击消失
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
                    //进入浏览器下载
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(newVersion.getUrl())));
                });
            }


            @Override
            public void noUpdate(@NotNull Version currentVersion) {
                if (iServerAvailableListener != null) {
                    iServerAvailableListener.onServerValid(currentVersion, currentVersion);
                }
                if (!hideLoadingDialogAndToast) {
                    LoadingDialog.with(context).cancel();
                    XToast.success("当前是最新版本");
                }
            }

            @Override
            public void develop() {
                if (iServerAvailableListener != null) {
                    iServerAvailableListener.onServerValid(null, null);
                }
                if (!hideLoadingDialogAndToast) {
                    LoadingDialog.with(context).cancel();
                    XToast.success("当前是开发版本");
                }
            }

            @Override
            public void error(@NotNull String error) {
                if (iServerAvailableListener != null) {
                    iServerAvailableListener.onServerInvalid();
                }
                if (!hideLoadingDialogAndToast) {
                    LoadingDialog.with(context).cancel();
                    XToast.error("检查更新失败\n" + error);
                }
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setOnKeyListener((dialogInterface, i, keyEvent) -> keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK)
                        .setTitle("注意")
                        .setMessage("检查更新失败或服务器不可用，请重试或下载最新版本\n当前版本：" + XAppUtils.getVersionName(context) + "\n开发者QQ：1962247851\n")
                        .setNeutralButton("重试", (dialog1, which) -> checkForUpdate(hideLoadingDialogAndToast, context, iServerAvailableListener))
                        .setPositiveButton("复制QQ号并打开QQ", null)
                        .setCancelable(false)
                        .create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
                    Data.openQq("1962247851");
                    XToast.success("QQ：1962247851已复制到剪切板");
                });
            }
        });
    }

    public static void checkForUpdate(IUpdateListener updateListener) {
        XHttp.obtain().post(HttpUtil.Urls.Version.QUERY_ALL, null, new HttpCallBack<MyResponse>() {
            @Override
            public void onSuccess(MyResponse myResponse) {
                if (myResponse.getError() == MyResponse.SUCCESS) {
                    List<Version> versionList = GsonUtil.parseJsonArray2List(myResponse.getBodyJson(), Version.class);
                    if (versionList.isEmpty()) {
                        updateListener.error(XFrame.getString(R.string.NoLog));
                    } else if (versionList.size() == 1) {
                        Version version = versionList.get(0);
                        //最新版本
                        StringBuilder builder = new StringBuilder();
                        String[] messages = version.getMessage().split("。");
                        for (String message : messages) {
                            builder.append(message).append("\n");
                        }
                        version.setMessage(builder.deleteCharAt(builder.length() - 1).toString());
                        //最新版本
                        updateListener.noUpdate(version);
                    } else {
                        //判断
                        float localVersionCode = XAppUtils.getVersionCode(XFrame.getContext());
                        Version newVersion = versionList.get(versionList.size() - 1);
                        float newVersionCode = newVersion.getId() == null ? 0 : newVersion.getId();
                        if (newVersionCode > localVersionCode) {
                            //有新版本
                            Version localVersion = versionList.get((int) localVersionCode - 1);
                            updateListener.haveNewVersion(localVersion, newVersion);
                        } else if (localVersionCode > newVersionCode) {
                            //本地大于服务器，开发版本
                            updateListener.develop();
                        } else {
                            //等于，最新版本
                            StringBuilder builder = new StringBuilder();
                            String[] messages = newVersion.getMessage().split("。");
                            for (String message : messages) {
                                builder.append(message).append("\n");
                            }
                            newVersion.setMessage(builder.deleteCharAt(builder.length() - 1).toString());
                            updateListener.noUpdate(newVersion);
                        }
                    }
                } else {
                    updateListener.error(myResponse.getMsg());
                }
            }

            @Override
            public void onFailed(String error) {
                updateListener.error(error);
            }
        });
    }

    public static void checkHistory(IHistoryListener iHistoryListener) {
        XHttp.obtain().post(HttpUtil.Urls.Version.QUERY_ALL, null, new HttpCallBack<MyResponse>() {
            @Override
            public void onSuccess(MyResponse myResponse) {
                if (myResponse.getError() == MyResponse.SUCCESS) {
                    StringBuilder builder = new StringBuilder();
                    List<Version> versionList = GsonUtil.parseJsonArray2List(myResponse.getBodyJson(), Version.class);
                    if (!versionList.isEmpty()) {
                        for (int i = 0; i < versionList.size(); i++) {
                            Version version = versionList.get(i);
                            if (i != 0) {
                                builder.append("\n\n");
                            }
                            String[] messages = version.getMessage().split("。");
                            builder.append("版本：")
                                    .append(version.getVersion())
                                    .append("（")
                                    .append(DateUtil.getDateAndTime(version.getDate(), " "))
                                    .append("）\n更新内容：\n");
                            for (String message : messages) {
                                builder.append(message).append("\n");
                            }
                            builder.deleteCharAt(builder.length() - 1);
                        }
                        iHistoryListener.success(builder.toString());
                    } else {
                        iHistoryListener.success("暂无更新日志");
                    }
                } else {
                    iHistoryListener.error(myResponse.getMsg());
                }
            }

            @Override
            public void onFailed(String error) {
                iHistoryListener.error(error);
                XToast.error("数据获取失败\n" + error);
            }
        });
    }

    /**
     * 发布新版本
     *
     * @param message  更新日志
     * @param url      下载链接
     * @param callBack 回调
     */
    public static void insert(String message, String url, HttpUtil.HttpUtilCallBack<Boolean> callBack) {
        Version version = new Version();
        version.setMessage(message);
        version.setUrl(url);
        version.setVersion(XAppUtils.getVersionName(XFrame.getContext()));
        Map<String, Object> params = new HashMap<>(2);
        params.put("versionJson", version);
        params.put("userJson", App.getUser());
        XHttp.obtain().post(HttpUtil.Urls.Version.INSERT, params, new HttpCallBack<MyResponse>() {
            @Override
            public void onSuccess(MyResponse myResponse) {
                if (myResponse.getError() == MyResponse.SUCCESS) {
                    callBack.onResponse(true);
                } else {
                    callBack.onFailure(myResponse.getMsg());
                }
            }

            @Override
            public void onFailed(String error) {
                callBack.onFailure(error);
            }
        });
    }

    public interface IServerAvailableListener {
        /**
         * 服务器不可用
         */
        void onServerInvalid();

        /**
         * 服务器正常
         */
        void onServerValid(@Nullable Version currentVersion, @Nullable Version newVersion);
    }

    public interface IUpdateListener {
        /**
         * 有新版本
         *
         * @param newVersion 最新版本
         */
        void haveNewVersion(@NotNull Version currentVersion, @NotNull Version newVersion);

        /**
         * 没有更新
         */
        void noUpdate(@NotNull Version currentVersion);

        /**
         * 内测版本
         */
        void develop();


        /**
         * 错误
         */
        void error(@NotNull String error);
    }

    public interface IHistoryListener {
        /**
         * 成功
         *
         * @param historyString 所有更新日志
         */
        void success(@NotNull String historyString);

        /**
         * 错误
         */
        void error(@NotNull String error);
    }
}
