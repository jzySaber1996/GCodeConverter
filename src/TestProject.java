import java.io.*;
import java.util.ArrayList;
import java.util.Properties;

public class TestProject {
    private static ArrayList<String> zStrings = new ArrayList<>();

    private static ArrayList<Double> zPrintList = new ArrayList<>();
    private static ArrayList<ArrayList<Point>> pointPrintList = new ArrayList<>();

    public static void main(String[] args){
        //Integer a = 1;
        storeData();
        //System.out.println(a);
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
}
