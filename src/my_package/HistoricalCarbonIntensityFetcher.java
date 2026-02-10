package my_package;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class HistoricalCarbonIntensityFetcher {
    private Map<Integer, Double> dataMap = new HashMap<>();

    // Constructor to read CSV file and store values in a HashMap
    public HistoricalCarbonIntensityFetcher(String filePath) {
        loadCSV(filePath);
    }

    // Method to read CSV and store (index, carbonIntensity) pairs in HashMap
    private void loadCSV(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            //br.readLine(); // Skip header if present
            while ((line = br.readLine()) != null) {
                String[] values = line.split(","); // Assuming CSV is comma-separated
                if (values.length >= 2) {
                    int index = Integer.parseInt(values[0].trim());
                    double carbonIntensity = Double.parseDouble(values[1].trim());
                    dataMap.put(index, carbonIntensity);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to get carbon intensity for a given index
    public Double getCarbonIntensity(int queryIndex) {
        return dataMap.getOrDefault(queryIndex, null); // Returns null if index not found
    }

  }
