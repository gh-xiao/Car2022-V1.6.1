package car.bkrc.com.car2021.Utils.QRcode;

import android.util.Log;

public class GetCode {

    private String code = "";

    public String getCode() {
        return code;
    }

    /**
     * 解析二维码字符串
     *
     * @param str 二维码字符串
     */
    public String parsing(String str) {
        StringBuilder sb = new StringBuilder();
        for (char ch : str.toCharArray()) {
            if (Character.isDigit(ch) || Character.isUpperCase(ch) || Character.isLowerCase(ch))
//            if (Character.isDigit(ch) || Character.isUpperCase(ch))
                sb.append(ch);
        }
        code = sb.toString();
//        System.out.println(code);
        Log.i("code", code);
        return code;
    }
}
