import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

public class Counter {
    private static HashMap<String, Integer> voteRes;

    static {
        voteRes = new HashMap<>();
        voteRes.put("1", 0);
        voteRes.put("2", 0);
        voteRes.put("3", 0);
        voteRes.put("4", 0);
    }

    /**
     * Проверка цифровой подписи с использованием openssl
     * @param publicKeyPath путь к файлу с публичным ключом
     * @param signaturePath путь к файлу с подписанным сообщением
     * @param messagePath путь к файлу с сообщением
     * @param privateKeyPath путь к файлу с приватным ключом
     * @return true если верификация пройдена успешно и false если верификация провалена
     */
    public static boolean verify(String publicKeyPath, String signaturePath, String messagePath, String privateKeyPath) {
        ProcessBuilder pb = new ProcessBuilder();
        ArrayList<String> com = new ArrayList<>();
        com.add("E:\\Git\\usr\\bin\\openssl.exe");
        com.add("dgst");
        com.add("-sha256");
        com.add("-verify");
        com.add(publicKeyPath);
        com.add("-signature");
        com.add(signaturePath);
        com.add(messagePath);
        pb.command(com);
        String res = "";
        try {
            Process process = pb.start();
            BufferedReader reader = process.inputReader();
            res = reader.readLine();
            reader.close();
        } catch (Exception ex) {
            System.out.println("verify: " + ex);
        }
        System.out.println(res);
        if (res != null && !res.isEmpty() && !res.equals("Verification Failure")) {
            addVote(messagePath, privateKeyPath);
            System.out.println(Arrays.toString(voteRes.values().toArray()));
            return true;
        }
        return false;
    }

    /**
     * Верификация сообщения
     * @param message сообщение
     * @param signedMessage подписанное сообщение
     * @param module модуль ключа
     * @param e публичная экспонента
     * @return true если проверка пройдена false если нет
     */
    public static boolean verifyCurs(String message, BigInteger signedMessage, BigInteger module, BigInteger e) {
        //System.out.println(message);
        //System.out.println(signedMessage);
        BigInteger mes = new BigInteger(1, message.trim().getBytes());
        if (signedMessage.modPow(e, module).equals(mes)) {
            addVote(message);
            return true;
        }
        return false;
    }

    /**
     * Выводит результаты голосования
     */
    public static void showResults() {
        Set<String> keys = voteRes.keySet();
        for (String key : keys) {
            System.out.println("Кандидат " + key + " набрал " + voteRes.get(key) + " голосов");
        }
    }
    private static void addVote(String messagePath, String privateKeyPath) {
        String vote = decrypt(messagePath, privateKeyPath);
        System.out.println(vote);
        if (vote != null && !vote.isEmpty() && voteRes.containsKey(vote.trim()))
            voteRes.replace(vote.trim(), voteRes.get(vote.trim()) + 1);
    }

    /**
     * Добавляет голос к общему результату
     * @param vote голос
     */
    private static void addVote(String vote) {
        System.out.println(vote);
        if (vote != null && !vote.isEmpty() && voteRes.containsKey(vote.trim()))
            voteRes.replace(vote.trim(), voteRes.get(vote.trim()) + 1);
    }

    private static String decrypt(String messagePath, String privateKeyPath) {
        String res = "";
        ProcessBuilder pb = new ProcessBuilder();
        ArrayList<String> com = new ArrayList<>();
        String out = "F:\\OpenSSLLabs\\Labs\\DefenseLabs\\Lab4\\res\\decMes.txt";
        com.add("E:\\Git\\usr\\bin\\openssl.exe");
        com.add("rsautl");
        com.add("-decrypt");
        com.add("-inkey");
        com.add(privateKeyPath);
        com.add("-in");
        com.add(messagePath);
        com.add("-out");
        com.add(out);
        pb.command(com);
        try {
            Process process = pb.start();
/*            BufferedReader reader = process.inputReader();
            res = reader.readLine();
            reader.close();*/
        } catch (Exception ex) {
            System.out.println("decrypt: " + ex);
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(out))) {
            res = reader.readLine();
        } catch (Exception ex) {
            System.out.println("decrypt read: " + ex);
        }
        return res;
    }

}
