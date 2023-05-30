public class Utils {
    /**
     * Перевод байтов в шестнадцатеричную строку
     * @param b массив байтов
     * @return шестнадцатеричное представление байтов
     */
    public static String bytesToHexString(byte[] b) {
        StringBuilder res = new StringBuilder();
        for (byte value : b)
            res.append(Integer.toString((value & 0xff) + 0x100, 16).substring(1));

        return res.toString();
    }

    /**
     * Перевод шестнадцатеричной строки в байты
     * @param hexString шестнадцатеричная строка
     * @return массив байтов
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString.length() % 2 != 0)
            hexString = "0" + hexString;

        int len = hexString.length();
        byte[] byteArray = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            byteArray[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return byteArray;
    }
}
