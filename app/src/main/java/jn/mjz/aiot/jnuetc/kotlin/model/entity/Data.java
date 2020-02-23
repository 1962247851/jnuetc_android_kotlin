package jn.mjz.aiot.jnuetc.kotlin.model.entity;

import androidx.annotation.Nullable;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;

import com.youth.xframe.XFrame;
import com.youth.xframe.entity.DateDifference;
import com.youth.xframe.utils.XAppUtils;
import com.youth.xframe.utils.XDateUtils;
import com.youth.xframe.utils.XEmptyUtils;
import com.youth.xframe.utils.http.HttpCallBack;
import com.youth.xframe.utils.http.XHttp;
import com.youth.xframe.utils.log.XLog;
import com.youth.xframe.widget.XToast;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jn.mjz.aiot.jnuetc.kotlin.R;
import jn.mjz.aiot.jnuetc.kotlin.model.application.App;
import jn.mjz.aiot.jnuetc.kotlin.model.greendao.DaoSession;
import jn.mjz.aiot.jnuetc.kotlin.model.greendao.DataChangeLogDao;
import jn.mjz.aiot.jnuetc.kotlin.model.greendao.DataDao;
import jn.mjz.aiot.jnuetc.kotlin.model.util.DateUtil;
import jn.mjz.aiot.jnuetc.kotlin.model.util.GsonUtil;
import jn.mjz.aiot.jnuetc.kotlin.model.util.HttpUtil;
import jn.mjz.aiot.jnuetc.kotlin.view.fragment.datachangelog.DataChangeLogFragment;

/**
 * @author 19622
 */
@Entity
public class Data extends BaseObservable {

    @Id
    private Long id;

    @NotNull
    @Index(unique = true)
    private String uuid;

    @NotNull
    private long date;

    @NotNull
    private short state;

    @NotNull
    private String name;

    @NotNull
    private String college;

    @NotNull
    private String grade;

    private String tel;

    private String qq;

    @NotNull
    private String local;

    @NotNull
    private short district;

    @NotNull
    private String model;

    @NotNull
    private String message;

    private String repairer;

    private long orderDate;

    private long repairDate;

    private String mark;

    private String service;

    private String repairMessage;

    private String photo;

    @ToMany(referencedJoinProperty = "dataId")
    private List<DataChangeLog> dataChangeLogs;

    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /**
     * Used for active entity operations.
     */
    @Generated(hash = 1702140558)
    private transient DataDao myDao;

    @Keep
    public Data() {
        uuid = UUID.randomUUID().toString();
        this.mark = GET_MARKS().get(0);
        this.service = GET_SERVICES().get(0);
        this.repairMessage = "";
    }

    @Generated(hash = 813129418)
    public Data(Long id, @NotNull String uuid, long date, short state, @NotNull String name, @NotNull String college, @NotNull String grade, String tel, String qq, @NotNull String local, short district, @NotNull String model, @NotNull String message, String repairer, long orderDate,
                long repairDate, String mark, String service, String repairMessage, String photo) {
        this.id = id;
        this.uuid = uuid;
        this.date = date;
        this.state = state;
        this.name = name;
        this.college = college;
        this.grade = grade;
        this.tel = tel;
        this.qq = qq;
        this.local = local;
        this.district = district;
        this.model = model;
        this.message = message;
        this.repairer = repairer;
        this.orderDate = orderDate;
        this.repairDate = repairDate;
        this.mark = mark;
        this.service = service;
        this.repairMessage = repairMessage;
        this.photo = photo;
    }


    @org.jetbrains.annotations.NotNull
    @Override
    public String toString() {
        return GsonUtil.getInstance().toJson(this);
    }


    public Long getId() {
        return this.id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getUuid() {
        return this.uuid;
    }


    public void setUuid(String uuid) {
        this.uuid = uuid;
    }


    public short getState() {
        return this.state;
    }


    public void setState(short state) {
        this.state = state;
    }


    @Bindable
    public String getName() {
        return this.name;
    }


    public void setName(String name) {
        this.name = name;
        notifyPropertyChanged(BR.name);
    }

    public static List<String> getColleges() {
        return Arrays.asList(XFrame.getResources().getStringArray(R.array.spinner_college_entries));
    }

    @Bindable
    public String getCollege() {
        return this.college;
    }

    public void setCollege(String college) {
        this.college = college;
        notifyPropertyChanged(BR.college);
    }

    @Bindable
    public String getGrade() {
        return this.grade;
    }


    public void setGrade(String grade) {
        this.grade = grade;
        notifyPropertyChanged(BR.grade);
    }

    @Bindable
    public String getTel() {
        return this.tel;
    }


    public void setTel(String tel) {
        this.tel = tel;
        notifyPropertyChanged(BR.tel);
    }


    @Bindable
    public String getQq() {
        return this.qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
        notifyPropertyChanged(BR.qq);
    }

    public static List<String> GET_LOCALS_N() {
        return Arrays.asList(XFrame.getResources().getStringArray(R.array.spinner_local_n_entries));
    }

    public static List<String> GET_LOCALS_S() {
        return Arrays.asList(XFrame.getResources().getStringArray(R.array.spinner_local_s_entries));
    }

    public static List<String> GET_MARKS() {
        return Arrays.asList(XFrame.getResources().getStringArray(R.array.spinner_mark_entries));
    }

    public static List<String> GET_GRADES() {
        return Arrays.asList(XFrame.getResources().getStringArray(R.array.spinner_grade_entries));
    }

    public static List<String> GET_SERVICES() {
        return Arrays.asList(XFrame.getResources().getStringArray(R.array.spinner_service_entries));
    }

    public static List<String> GET_LOCALS() {
        return Arrays.asList(XFrame.getResources().getStringArray(R.array.spinner_local_entries));
    }

    public static List<String> GET_COLLEGES() {
        return Arrays.asList(XFrame.getResources().getStringArray(R.array.spinner_college_entries));
    }

    @Bindable
    public String getLocal() {
        return this.local;
    }


    public void setLocal(String local) {
        this.district = "杏桃桔桂梅榴李竹".indexOf(local.charAt(0)) != -1 ? (short) 0 : (short) 1;
        this.local = local;
        notifyPropertyChanged(BR.local);
    }


    @Bindable
    public String getModel() {
        return this.model;
    }


    public void setModel(String model) {
        this.model = model;
        notifyPropertyChanged(BR.model);
    }

    @Bindable
    public String getMessage() {
        return this.message;
    }


    public void setMessage(String message) {
        this.message = message;
        notifyPropertyChanged(BR.message);
    }

    @Bindable
    public String getRepairer() {
        return this.repairer;
    }


    public void setRepairer(String repairer) {
        this.repairer = repairer;
        notifyPropertyChanged(BR.repairer);
    }

    @Bindable
    public String getMark() {
        return this.mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
        notifyPropertyChanged(BR.mark);
    }

    @Bindable
    public String getService() {
        return this.service;
    }

    public void setService(String service) {
        this.service = service;
        notifyPropertyChanged(BR.service);
    }

    @Bindable
    public String getRepairMessage() {
        return this.repairMessage;
    }

    public void setRepairMessage(String repairMessage) {
        this.repairMessage = repairMessage;
        notifyPropertyChanged(BR.repairMessage);
    }

    public short getDistrict() {
        return this.district;
    }

    public void setDistrict(short district) {
        this.district = district;
    }

    public String getPhoto() {
        return this.photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public List<String> getPhotoUrlList() {
        List<String> urlList = null;
        if (photo != null && !photo.isEmpty()) {
            urlList = new LinkedList<>();
            List<String> photoList = GsonUtil.parseJsonArray2List(photo, String.class);
            for (String s : photoList) {
                urlList.add(String.format("%s?path=/opt/dataDP/&fileName=%s%s", HttpUtil.Urls.File.DOWNLOAD, uuid, s));
            }
        }
        return urlList;
    }


    /**
     * 判断是否全为非空
     *
     * @return 全为非空
     */
    public boolean isAllNotEmpty() {
        boolean b = true;
        //判断报修的信息
        if (XEmptyUtils.isEmpty(getLocal()) || XEmptyUtils.isEmpty(getName()) || XEmptyUtils.isEmpty(getTel()) || XEmptyUtils.isEmpty(getQq()) || XEmptyUtils.isEmpty(getCollege()) || XEmptyUtils.isEmpty(getGrade()) || XEmptyUtils.isEmpty(getModel()) || XEmptyUtils.isEmpty(getMessage())) {
            b = false;
        }
        //还要判断反馈的
        if (getState() == 2) {
            if (XEmptyUtils.isEmpty(getRepairer()) || XEmptyUtils.isEmpty(getMark()) || XEmptyUtils.isEmpty(getService()) || XEmptyUtils.isEmpty(getRepairMessage())) {
                b = false;
            }
        }
        return b;
    }

    public String getDateString() {
        return DateUtil.getDateAndTime(date, " ");
    }

    public long getDate() {
        return this.date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getProcessingTitle() {
        switch (state) {
            case 0:
                return "等待处理已耗时";
            case 1:
                return "处理耗时";
            default:
                return "维修总用时";
        }
    }

    public String getProcessingTime() {
        String s = "";
        long startTimeMill;
        long endTimeMill;
        switch (state) {
            case 1:
                startTimeMill = orderDate;
                endTimeMill = System.currentTimeMillis();
                break;
            case 2:
                startTimeMill = orderDate;
                endTimeMill = repairDate;
                break;
            default:
                startTimeMill = date;
                endTimeMill = System.currentTimeMillis();
        }
        DateDifference twoDataDifference = XDateUtils.getTwoDataDifference(new Date(endTimeMill), new Date(startTimeMill));
        int day = (int) twoDataDifference.getDay();
        int hour = (int) twoDataDifference.getHour() % 24;
        int minute = (int) twoDataDifference.getMinute() % 60;
//        int second = (int) twoDataDifference.getSecond() % 60;
        if (day != 0) {
            s = (s + day + "天");
        }
        if (hour != 0) {
            s = (s + hour + "小时");
        }
        if (minute != 0) {
            s = (s + minute + "分钟");
        }
        if (s.isEmpty()) {
            s = "不足一分钟";
        }
        return s;
    }

    public long getOrderDate() {
        return this.orderDate;
    }

    public void setOrderDate(long orderDate) {
        this.orderDate = orderDate;
    }

    public String getRepairDateString() {
        return DateUtil.getDateAndTime(repairDate, " ");
    }

    public long getRepairDate() {
        return this.repairDate;
    }

    public void setRepairDate(long repairDate) {
        this.repairDate = repairDate;
    }

    public String getStateString() {
        switch (state) {
            case 0:
                return XFrame.getString(R.string.NewData);
            case 1:
                return XFrame.getString(R.string.Processing);
            default:
                return XFrame.getString(R.string.Done);
        }
    }

    /**
     * 通过id查询报修单，排序更新日志，并更新本地数据库
     *
     * @param id       id值
     * @param callBack 回调
     */
    public static void queryById(final Long id, final HttpUtil.HttpUtilCallBack<Data> callBack) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        XHttp.obtain().post(HttpUtil.Urls.Data.QUERY_BY_ID, params, new HttpCallBack<MyResponse>() {

            @Override
            public void onSuccess(MyResponse myResponse) {
                if (myResponse.getError() == MyResponse.SUCCESS) {
                    Data fromJson = GsonUtil.getInstance().fromJson(myResponse.getBodyJson(), Data.class);
                    List<DataChangeLog> sorted = DataChangeLogFragment.sortLogByTimeDesc(fromJson.getDataChangeLogs());
                    fromJson.dataChangeLogs.clear();
                    fromJson.dataChangeLogs.addAll(sorted);
                    App.daoSession.getDataDao().insertOrReplace(fromJson);
                    callBack.onResponse(fromJson);
                } else {
                    if (myResponse.getError() == MyResponse.FAILED) {
                        App.daoSession.getDataDao().deleteByKey(id);
                    }
                    callBack.onFailure(myResponse.getMsg());
                }
            }

            @Override
            public void onFailed(String error) {
                callBack.onFailure(error);
            }
        });
    }

    /**
     * 从服务器获取最新保修单数据，并且保存到本地数据库（先删除所有再插入所有）
     *
     * @param callBack 回调
     */
    public static void queryAll(final HttpUtil.HttpUtilCallBack<ArrayList<Data>> callBack) {
        XHttp.obtain().post(HttpUtil.Urls.Data.QUERY_ALL, null, new HttpCallBack<MyResponse>() {
            @Override
            public void onSuccess(MyResponse myResponse) {
                if (myResponse.getError() == MyResponse.SUCCESS) {
                    ArrayList<Data> resultList = GsonUtil.parseJsonArray2List(myResponse.getBodyJson(), Data.class);
                    if (!App.getUser().haveWholeSchoolAccess()) {
                        ArrayList<Data> needToDelete = new ArrayList<>();
                        for (Data d :
                                resultList) {
                            if (d.district != App.getUser().getWhichGroup()) {
                                needToDelete.add(d);
                            }
                        }
                        resultList.removeAll(needToDelete);
                    }
                    App.daoSession.getDataDao().deleteAll();
                    App.daoSession.getDataDao().insertInTx(resultList);
                    callBack.onResponse(resultList);
                } else {
                    XLog.e(myResponse.getMsg());
                    callBack.onFailure(myResponse.getMsg());
                }
            }

            @Override
            public void onFailed(String error) {
                XLog.e(error);
                callBack.onFailure(error);
            }
        });
    }

    /**
     * 修改报修单的所有信息，修改成功后再查询一次报修单，并更新本地数据库，因为dataChangeLogs是另一张表，有延迟
     *
     * @param oldDataJson 修改前的报修单
     * @param callBack    回调,会返回更新后的data
     */
    public void modify(final String oldDataJson, final HttpUtil.HttpUtilCallBack<Data> callBack) {
        Map<String, Object> params = new HashMap<>(3);
        params.put("dataJson", toString());
        params.put("oldDataJson", oldDataJson);
        params.put("name", App.getUser().getUserName());
        XHttp.obtain().post(HttpUtil.Urls.Data.UPDATE, params, new HttpCallBack<MyResponse>() {
            @Override
            public void onSuccess(MyResponse myResponse) {
                if (myResponse.getError() == MyResponse.SUCCESS) {
                    Data data = GsonUtil.getInstance().fromJson(myResponse.getBodyJson(), Data.class);
                    Data.queryById(data.getId(), new HttpUtil.HttpUtilCallBack<Data>() {
                        @Override
                        public void onResponse(@org.jetbrains.annotations.NotNull Data result) {
                            App.daoSession.getDataDao().insertOrReplace(result);
                            callBack.onResponse(result);
                        }

                        @Override
                        public void onFailure(@org.jetbrains.annotations.NotNull String error) {
                            callBack.onFailure(error);
                        }
                    });
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

    /**
     * 批量删除报修单
     *
     * @param ids      要删除的id列表
     * @param callBack 回调
     */
    public static void deleteMany(final List<Long> ids, final HttpUtil.HttpUtilCallBack<Boolean> callBack) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("idListJson", GsonUtil.getInstance().toJson(ids));
        params.put("userJson", App.getUser());
        XHttp.obtain().post(HttpUtil.Urls.Data.DELETE_BY_ID_LIST, params, new HttpCallBack<MyResponse>() {
            @Override
            public void onSuccess(MyResponse myResponse) {
                if (myResponse.getError() == MyResponse.SUCCESS) {
                    App.daoSession.getDataDao().deleteByKeyInTx(ids);
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

    /**
     * 打开qq或Tim并复制qq号
     *
     * @param qq QQ号
     */
    public static void openQq(String qq) {
        if (XAppUtils.isInstallApp("com.tencent.mobileqq")) {
            XAppUtils.startApp("com.tencent.mobileqq");
            App.copyToClipboard(XFrame.getContext(), qq);
        } else if (XAppUtils.isInstallApp("com.tencent.tim")) {
            XAppUtils.startApp("com.tencent.tim");
            App.copyToClipboard(XFrame.getContext(), qq);
        } else {
            XToast.error("未安装QQ和Tim或安装的版本不支持");
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Data) {
            return this.id.equals(((Data) obj).getId());
        }
        return false;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 978765309)
    public List<DataChangeLog> getDataChangeLogs() {
        if (dataChangeLogs == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            DataChangeLogDao targetDao = daoSession.getDataChangeLogDao();
            List<DataChangeLog> dataChangeLogsNew = targetDao._queryData_DataChangeLogs(id);
            synchronized (this) {
                if (dataChangeLogs == null) {
                    dataChangeLogs = dataChangeLogsNew;
                }
            }
        }
        return dataChangeLogs;
    }


    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */
    @Generated(hash = 496895173)
    public synchronized void resetDataChangeLogs() {
        dataChangeLogs = null;
    }


    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }


    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }


    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }


    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 966473446)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getDataDao() : null;
    }


}
