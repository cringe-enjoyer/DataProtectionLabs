import java.io.BufferedReader;
import java.math.BigInteger;
import java.util.ArrayList;

public class Registrar {
    /**
     * Подпись с использованием openssl
     *
     * @param messagePath путь к файлу с сообщением
     * @param privateKeyPath путь к файлу с приватным ключом
     * @return путь к файлу с цифровой подписью
     */
    public static String sign(String messagePath, String privateKeyPath) {
        ProcessBuilder pb = new ProcessBuilder();
        String outFilePath = "F:\\OpenSSLLabs\\Labs\\DefenseLabs\\Lab4\\res\\signature.bin";
        ArrayList<String> com = new ArrayList<>();
        com.add("E:\\Git\\usr\\bin\\openssl.exe");
        com.add("dgst");
        com.add("-sha256");
        com.add("-sign");
        com.add(privateKeyPath);
        com.add("-out");
        com.add(outFilePath);
        com.add(messagePath);
        pb.command(com);
        String res = "";
        try {
            Process process = pb.start();
            BufferedReader reader = process.inputReader();
            res = reader.readLine();
            reader.close();
        } catch (Exception ex) {
            System.out.println(ex);
        }
        //return res;
        return outFilePath;
    }

    /**
     * Подписывает сообщение
     * @param message сообщение
     * @param module модуль
     * @param privateEx приватная экспонента
     * @return шестнадцатеричное представление подписанного сообщения
     */
    public static String signCurs(String message, BigInteger module, BigInteger privateEx) {
        BigInteger mes = new BigInteger(1, Utils.hexStringToBytes(message));
        BigInteger signedMessage = new BigInteger(1, mes.modPow(privateEx, module).toByteArray());
        return Utils.bytesToHexString(signedMessage.toByteArray());
    }
}
