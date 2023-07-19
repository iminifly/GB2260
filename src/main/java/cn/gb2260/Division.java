package cn.gb2260;

/**
 * 行政区信息
 */
public class Division {

    /**
     * 行政区名称
     */
    private String name;

    /**
     * 行政区代码
     */
    private String code;

    /**
     * 修订版本号
     */
    private String revision;

    /**
     * 省
     */
    private String province;

    /**
     * 市
     */
    private String prefecture;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getPrefecture() {
        return prefecture;
    }

    public void setPrefecture(String prefecture) {
        this.prefecture = prefecture;
    }

    @Override
    public String toString() {
        return (province == null ? "" : province + " ") + (prefecture == null ? "" : prefecture + " ") + (name == null ? "" : name);
    }
}
