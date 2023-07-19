package cn.gb2260;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * 国标GB2260
 */
public class GB2260 {
    /**
     * 修订版本号
     */
    private final Revision revision;

    /**
     * 行政区数据
     */
    private HashMap<String, String> data;

    /**
     * 行政区列表
     */
    private ArrayList<Division> provinces;

    /**
     * 国标GB2260构造, 默认最新修订版 V2014
     */
    public GB2260() {
        this(Revision.V2014);
    }

    /**
     * 构造指定版本的GB2260
     *
     * @param revision 修订版本
     */
    public GB2260(Revision revision) {
        this.revision = revision;
        data = new HashMap<String, String>();
        provinces = new ArrayList<Division>();
        InputStream inputStream = getClass().getResourceAsStream("/data/" + revision.getCode() + ".txt");
        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
        try {
            while (r.ready()) {
                String line = r.readLine();
                String[] split = line.split("\t");
                String code = split[0];
                String name = split[1];

                data.put(code, name);

                if (Pattern.matches("^\\d{2}0{4}$", code)) {
                    Division division = new Division();
                    division.setCode(code);
                    division.setName(name);
                    provinces.add(division);
                }
            }
        } catch (IOException e) {
            System.err.println("Error in loading GB2260 data!");
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据行政区代码获取行政区对象
     *
     * @param code 行政区代码
     * @return 行政区对象
     */
    public Division getDivision(String code) {
        if (code.length() != 6) {
            throw new InvalidCodeException("Invalid code");
        }

        if (!data.containsKey(code)) {
            return null;
        }

        Division division = new Division();
        division.setName(data.get(code));
        division.setRevision(getRevision().getCode());
        division.setCode(code);

        if (Pattern.matches("^\\d{2}0{4}$", code)) {
            return division;
        }

        String provinceCode = code.substring(0, 2) + "0000";
        division.setProvince(data.get(provinceCode));

        if (Pattern.matches("^\\d{4}0{2}$", code)) {
            return division;
        }

        String prefectureCode = code.substring(0, 4) + "00";
        division.setPrefecture(data.get(prefectureCode));

        division.setRevision(this.revision.getCode());
        return division;
    }

    /**
     * 获取修订版号码
     *
     * @return
     */
    public Revision getRevision() {
        return revision;
    }

    /**
     * 获取全部省份列表
     *
     * @return
     */
    public ArrayList<Division> getProvinces() {
        return provinces;
    }

    /**
     * 根据省代码获取地级市列表
     *
     * @param code 省代码 XX0000
     * @return 地级市列表
     */
    public ArrayList<Division> getPrefectures(String code) {
        ArrayList<Division> rv = new ArrayList<Division>();

        if (!Pattern.matches("^\\d{2}0{4}$", code)) {
            throw new InvalidCodeException("Invalid province code");
        }

        if (!data.containsKey(code)) {
            throw new InvalidCodeException("Province code not found");
        }

        Division province = getDivision(code);

        Pattern pattern = Pattern.compile("^" + code.substring(0, 2) + "\\d{2}00$");
        for (String key : data.keySet()) {
            if (pattern.matcher(key).matches()) {
                Division division = getDivision(key);
                division.setProvince(province.getName());
                rv.add(division);
            }
        }

        return rv;
    }

    /**
     * 根据地级市代码获取区县列表
     *
     * @param code 地级市代码
     * @return 区县列表
     */
    public ArrayList<Division> getCounties(String code) {
        ArrayList<Division> rv = new ArrayList<Division>();

        if (!Pattern.matches("^\\d+[1-9]0{2,3}$", code)) {
            throw new InvalidCodeException("Invalid prefecture code");
        }

        if (!data.containsKey(code)) {
            throw new InvalidCodeException("Prefecture code not found");
        }

        Division prefecture = getDivision(code);
        Division province = getDivision(code.substring(0, 2) + "0000");

        Pattern pattern = Pattern.compile("^" + code.substring(0, 4) + "\\d+$");
        for (String key : data.keySet()) {
            if (pattern.matcher(key).matches()) {
                Division division = getDivision(key);
                division.setProvince(province.getName());
                division.setPrefecture(prefecture.getName());
                rv.add(division);
            }
        }

        return rv;
    }
}
