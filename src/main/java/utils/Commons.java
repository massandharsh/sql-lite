package utils;

public class Commons {
    public static int decodeVariant(byte [] bytes){
        int value = 0;
        int shift = 0;
        for (byte b : bytes) {
            value |= ((b & 0x7F) << shift);
            shift += 7;
            if ((b & 0x80) == 0) {
                break;
            }
        }
        return value;
    }
}
