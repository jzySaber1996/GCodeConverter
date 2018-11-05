import java.io.*;
import java.util.ArrayList;
import java.util.Properties;

public class TestProject {
    private static ArrayList<String> zStrings = new ArrayList<>();

    private static ArrayList<Double> zPrintList = new ArrayList<>();
    private static ArrayList<ArrayList<Point>> pointPrintList = new ArrayList<>();

    private static Double avgX, avgY;
    public static void main(String[] args){
        //Integer a = 1;
//        storeData();
        //System.out.println(a);
        avgX = 10.0;
        avgY = 10.0;

        Point pointResult = layerAlgorithm(new Point(1.0, 0.0), new Point(0.0, 2.0), new Point(-1.0, 4.0),
                2, Math.sqrt(5));
//        ArrayList<Integer> testInt = new ArrayList<>();
//        testInt.add(1);
//        testInt.add(2);
//        testInt.add(3);
//        System.out.println(testInt.get(testInt.size()-1));
    }

    private static void reconstructFile() {
//        try {
//            FileInputStream zFile = new FileInputStream("File/zListFile.txt");
//            BufferedReader readerZ = new BufferedReader(new InputStreamReader(zFile));
//            Properties properties = new Properties();
//            properties.loadFromXML(new FileInputStream("File/printParameters.xml"));
//            Double height = Double.valueOf(properties.getProperty("height"));
//            Double firstHeight = Double.valueOf(properties.getProperty("firstHeight"));
//            Integer count = 0;
//            Double zHeight = 0.0;
//            String line = readerZ.readLine();
//            while (line != null){
//                String[] data = line.split(" ");
//                if (data[0].equals("")) break;
//                zHeight = Double.parseDouble(data[0]);
//                if (count == 0) {
//                    zHeight += firstHeight;
//                }
//                else zHeight += height;
//                String newLine = "";
//                Integer length  = data.length;
//                for (int i = 0; i < length; i++){
//                    newLine += (String.valueOf(zHeight) + " ");
//                }
//                zStrings.add(newLine);
//                count++;
//                line = readerZ.readLine();
//            }
//            readerZ.close();
//            File fileZ = new File("File/zListFile.txt");
//            FileWriter fileWriter = new FileWriter(fileZ);
//            fileWriter.write("");
//            for (String fileString : zStrings){
//                fileWriter.write(fileString);
//                fileWriter.write("\n");
//            }
//            fileWriter.flush();
//            fileWriter.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
    private static void storeData(){
//        try {
//            FileInputStream fileX = new FileInputStream("File/xListFile.txt");
//            FileInputStream fileY = new FileInputStream("File/yListFile.txt");
//            FileInputStream fileZ = new FileInputStream("File/zListFile.txt");
//            BufferedReader readerX = new BufferedReader(new InputStreamReader(fileX));
//            BufferedReader readerY = new BufferedReader(new InputStreamReader(fileY));
//            BufferedReader readerZ = new BufferedReader(new InputStreamReader(fileZ));
//            String lineX = readerX.readLine();
//            String lineY = readerY.readLine();
//            String lineZ = readerZ.readLine();
//            while ((lineX != null) && (lineY != null) && (lineZ != null)){
//                Double zData = Double.valueOf(lineZ.split(" ")[0]);
//                zPrintList.add(zData);
//                String[] xPoints = lineX.split(" ");
//                String[] yPoints = lineY.split(" ");
//                ArrayList<Point> singleLayerPoints = new ArrayList<>();
//                for (int i=0; i < xPoints.length; i++){
//                    Point point = new Point();
//                    point.setX(Double.parseDouble(xPoints[i]));
//                    point.setY(Double.parseDouble(yPoints[i]));
//                    singleLayerPoints.add(point);
//                }
//                pointPrintList.add(singleLayerPoints);
//                lineX = readerX.readLine();
//                lineY = readerY.readLine();
//                lineZ = readerZ.readLine();
//            }
//            readerX.close();
//            readerY.close();
//            readerZ.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        int a = 0;
    }

    private static Point layerAlgorithm(Point pointPrevious, Point point, Point pointLatter,
                                        Integer layer, Double d) {
//        Double w1_1 = pointLatter.getY() - point.getY();
//        Double w1_2 = point.getX() - pointLatter.getX();
//        Double w2_1 = point.getY() - pointPrevious.getY();
//        Double w2_2 = pointPrevious.getX() - point.getX();
//        Double b_1 = pointLatter.getX() * point.getY() - point.getX() * pointLatter.getY();
//        Double b_2 = point.getX() * pointPrevious.getY() - pointPrevious.getX() * point.getY();
//        Double C_1 = 0.0, C_2 = 0.0;
//        if (w1_1 * avgX + w1_2 * avgY + b_1 < 0) {
//            C_1 = b_1 + (layer - 1) * d * Math.sqrt(Math.pow(w1_1, 2) + Math.pow(w1_2, 2));
//        } else {
//            C_1 = b_1 - (layer - 1) * d * Math.sqrt(Math.pow(w1_1, 2) + Math.pow(w1_2, 2));
//        }
//        if (w2_1 * avgX + w2_2 * avgY + b_2 < 0) {
//            C_2 = b_2 + (layer - 1) * d * Math.sqrt(Math.pow(w2_1, 2) + Math.pow(w2_2, 2));
//        } else {
//            C_2 = b_2 - (layer - 1) * d * Math.sqrt(Math.pow(w2_1, 2) + Math.pow(w2_2, 2));
//        }
        Double pointX = 0.0, pointY = 0.0;
//        if (w2_2 * w1_1 - w2_1 * w1_2 != 0) {
//            pointX = (w1_2 * C_2 - w2_2 * C_1) / (w2_2 * w1_1 - w2_1 * w1_2);
//            pointY = (w1_1 * C_2 - w2_1 * C_1) / (w1_2 * w2_1 - w2_2 * w1_1);
//        }
//        else {
//            Double w3_1 = w1_2;
//            Double w3_2 = -1 * w1_1;
//            Double C_3 = w1_1 * point.getY() - w1_2 * point.getX();
//            pointX = (w1_2 * C_3 - w3_2 * C_1) / (w3_2 * w1_1 - w3_1 * w1_2);
//            pointY = (w1_1 * C_3 - w3_1 * C_1) / (w1_2 * w3_1 - w3_2 * w1_1);
//        }
        return new Point(pointX, pointY);
    }
}
