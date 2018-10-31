import java.util.Scanner;

public class CalculateDistance {
    public static void main(String[] args) {
        System.out.println("Input the four numbers:");
        Scanner scanner = new Scanner(System.in);
        Double x1 = scanner.nextDouble();
        Double y1 = scanner.nextDouble();
        Double x2 = scanner.nextDouble();
        Double y2 = scanner.nextDouble();
        System.out.println("Input Extrusion:");
        Double e1 = scanner.nextDouble();
        Double e2 = scanner.nextDouble();
        Double calculateE = calculate(x1, y1, x2, y2);
        System.out.println(calculateE + "," + (e2 - e1));
    }

    private static Double calculate(Double x1, Double y1,
                                    Double x2, Double y2) {
        Double e = 0.0;
        Double length = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
        Double baseArea = Math.PI * Math.pow(1.75, 2) / 4;
        e = length * 0.48 * 0.15 / baseArea;
        return e;
    }
}
