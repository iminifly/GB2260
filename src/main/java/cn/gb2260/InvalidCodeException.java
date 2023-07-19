package cn.gb2260;

/**
 * 无效代码异常
 */
public class InvalidCodeException extends RuntimeException {
    public InvalidCodeException(String s) {
        super(s, new Throwable());
    }
}
