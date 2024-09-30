import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class SecretSharing {

    private static class Point {
        int x;
        BigInteger y;

        Point(int x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
    }

    private static BigInteger decodeValue(String base, String value) {
        return new BigInteger(value, Integer.parseInt(base));
    }

    private static BigInteger lagrangeInterpolation(List<Point> points, int x) {
        BigInteger total = BigInteger.ZERO;
        for (int i = 0; i < points.size(); i++) {
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;
            for (int j = 0; j < points.size(); j++) {
                if (i != j) {
                    numerator = numerator.multiply(BigInteger.valueOf(x - points.get(j).x));
                    denominator = denominator.multiply(BigInteger.valueOf(points.get(i).x - points.get(j).x));
                }
            }
            total = total.add(points.get(i).y.multiply(numerator).divide(denominator));
        }
        return total;
    }

    private static BigInteger findSecret(JsonObject data) {
        int k = data.getJsonObject("keys").getInt("k");
        List<Point> points = new ArrayList<>();
        for (String key : data.keySet()) {
            if (!key.equals("keys")) {
                int x = Integer.parseInt(key);
                JsonObject value = data.getJsonObject(key);
                BigInteger y = decodeValue(value.getString("base"), value.getString("value"));
                points.add(new Point(x, y));
            }
        }
        return lagrangeInterpolation(points.subList(0, k), 0);
    }

    private static List<Integer> findWrongPoints(JsonObject data) {
        int k = data.getJsonObject("keys").getInt("k");
        List<Point> points = new ArrayList<>();
        for (String key : data.keySet()) {
            if (!key.equals("keys")) {
                int x = Integer.parseInt(key);
                JsonObject value = data.getJsonObject(key);
                BigInteger y = decodeValue(value.getString("base"), value.getString("value"));
                points.add(new Point(x, y));
            }
        }

        List<Integer> wrongPoints = new ArrayList<>();
        for (int i = k; i < points.size(); i++) {
            Point p = points.get(i);
            if (!lagrangeInterpolation(points.subList(0, k), p.x).equals(p.y)) {
                wrongPoints.add(p.x);
            }
        }
        return wrongPoints;
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java SecretSharing <input_file.json>");
            return;
        }

        String inputFile = args[0];

        try (JsonReader jsonReader = Json.createReader(new FileReader(inputFile))) {
            JsonObject testcase = jsonReader.readObject();

            BigInteger secret = findSecret(testcase);

            List<Integer> wrongPoints = findWrongPoints(testcase);

            System.out.println("Secret: " + secret);
            System.out.println("Wrong points: " + wrongPoints);
        } catch (IOException e) {
            System.err.println("Error reading the input file: " + e.getMessage());
        }
    }
}
