package jn.mjz.aiot.jnuetc.kotlin.model.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;

import jn.mjz.aiot.jnuetc.kotlin.model.util.GsonUtil;

@Entity
public class DataChangeLog {
    @Id
    private Long id;
    @NotNull
    private String changeInfo;
    @NotNull
    private long date;
    @NotNull
    private String name;
    @NotNull
    private Long dataId;

    @Keep
    public DataChangeLog() {
        this.date = System.currentTimeMillis();
    }

    @Generated(hash = 1722556467)
    public DataChangeLog(Long id, @NotNull String changeInfo, long date,
            @NotNull String name, @NotNull Long dataId) {
        this.id = id;
        this.changeInfo = changeInfo;
        this.date = date;
        this.name = name;
        this.dataId = dataId;
    }

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

    public String getChangeInfo() {
        return this.changeInfo;
    }

    public void setChangeInfo(String changeInfo) {
        this.changeInfo = changeInfo;
    }

    public long getDate() {
        return this.date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getDataId() {
        return this.dataId;
    }

    public void setDataId(Long dataId) {
        this.dataId = dataId;
    }
}
