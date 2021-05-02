import java.io.*;

public class Grid_Map_Iterator {

    static int all_known_grids;

    // Dimensions of grid map
    final static int map_width = 15;
    final static int map_height = 20;

    static String P1_map_descriptor_hex;
    static String P2_map_descriptor_hex;

    static int[][] P1_map_descriptor;
    static int[][] P2_map_descriptor;

    public static void init() {
        P1_map_descriptor = new int[map_height][map_width];
        P2_map_descriptor = new int[map_height][map_width];

        all_known_grids = 0;
    }

    public Grid_Map_Iterator() {
        P1_map_descriptor = new int[map_height][map_width];
        P2_map_descriptor = new int[map_height][map_width];

        all_known_grids = 0;
    }

    // convert hexadecimal to binary
    public static String hexToBinary(String hex) {
        hex = hex.replaceAll("0", "0000");
        hex = hex.replaceAll("1", "0001");
        hex = hex.replaceAll("2", "0010");
        hex = hex.replaceAll("3", "0011");
        hex = hex.replaceAll("4", "0100");
        hex = hex.replaceAll("5", "0101");
        hex = hex.replaceAll("6", "0110");
        hex = hex.replaceAll("7", "0111");
        hex = hex.replaceAll("8", "1000");
        hex = hex.replaceAll("9", "1001");
        hex = hex.replaceAll("A", "1010");
        hex = hex.replaceAll("B", "1011");
        hex = hex.replaceAll("C", "1100");
        hex = hex.replaceAll("D", "1101");
        hex = hex.replaceAll("E", "1110");
        hex = hex.replaceAll("F", "1111");
        return hex;
    }

    // Parse text file to extract map information
    public static int[][] parse_text_file_for_map_info(String p1FileName, String p2FileName) {

        // Initialise file reading object variables
        BufferedReader buffer_read_p1 = null;
        BufferedReader buffer_read_p2 = null;
        int[][] fileMap = new int[map_height][map_width];
        File file_P1 = new File(p1FileName);
        File file_P2 = new File(p2FileName);

        // Read TXT file
        try {
            buffer_read_p1 = new BufferedReader(new FileReader(file_P1));
            buffer_read_p2 = new BufferedReader(new FileReader(file_P2));
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        }

        // read hexadecimal values
        try {
            // iterators for the two strings
            int p1Index = 0;
            int p2Index = 0;

            String p1_hex_string = buffer_read_p1.readLine().toUpperCase();
            String p2_hex_string = buffer_read_p2.readLine().toUpperCase();

            // convert hex to binary string
            String p1String = hexToBinary(p1_hex_string);
            String p2String = hexToBinary(p2_hex_string);
            p1String = p1String.substring(0, p1String.length() - 2);

            // traverse P1 and P2
            for (int row = map_height - 1; row >= 0; row--) {
                for (int col = 0; col < map_width; col++) {
                    if (Character.getNumericValue(p1String.charAt(p1Index)) == 0) {
                        // fileMap[row][col] = Explored_Types.convertTypeToInt("EMPTY");
                        fileMap[row][col] = Explored_Types.convertTypeToInt("UN_EMPTY");
                    } else {
                        if (p2Index < p2String.length() && Character.getNumericValue(p2String.charAt(p2Index)) == 0) {
                            fileMap[row][col] = Explored_Types.convertTypeToInt("EMPTY");
                        } else {
                            fileMap[row][col] = Explored_Types.convertTypeToInt("OBSTACLE");
                        }
                        p2Index++;
                    }
                    p1Index++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileMap;
    }

    // Convert grid exploration results from binary to hexadecimal format
    public static void print_explored_results_to_hex(String fileName) {

        // Initialised to NULL
        BufferedWriter buffered_write = null;
        FileWriter file_writer = null;

        // Convert string from binary to hex, then write to given file
        try {
            file_writer = new FileWriter(fileName);
            buffered_write = new BufferedWriter(file_writer);

            P1_map_descriptor_hex = format_string_to_hex(P1_map_descriptor_hex);

            buffered_write.write(P1_map_descriptor_hex);
        } catch (IOException e) {

        }

        finally {
            try {
                if (buffered_write != null)
                    buffered_write.close();
                if (file_writer != null)
                    file_writer.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Parse explored grid map results and store in TXT file
    public static void print_explored_results_to_file(String fileName, int[][] results) {

        // Initialise file reading object variables
        BufferedWriter buffered_write = null;
        FileWriter file_writer = null;

        if (P1_map_descriptor == null)
            init();

        try {
            file_writer = new FileWriter(fileName);
            buffered_write = new BufferedWriter(file_writer);
            StringBuilder string_builder = new StringBuilder();
            StringBuilder hexSB = new StringBuilder();

            // Fix padding
            string_builder.append("11" + System.getProperty("line.separator"));
            hexSB.append("11");

            // Adjust padding of '1's by counting known grids
            all_known_grids = 0;

            // Nested loops to process 2D map
            for (int w = P1_map_descriptor.length - 1; w >= 0; w--) {
                for (int h = 0; h < P1_map_descriptor[0].length; h++) {

                    if (results[w][h] == Explored_Types.convertTypeToInt("EMPTY")
                            || results[w][h] == Explored_Types.convertTypeToInt("OBSTACLE")) {
                        string_builder.append(1);
                        P1_map_descriptor[w][h] = 1;
                        hexSB.append(1);
                        all_known_grids++;
                    } else {
                        string_builder.append(0);
                        P1_map_descriptor[w][h] = 0;
                        hexSB.append(0);
                    }
                }

                string_builder.append(System.getProperty("line.separator"));
            }

            // Fix padding
            string_builder.append("11");
            hexSB.append("11");

            // Write string to TXT file
            buffered_write.write(string_builder.toString());

            // Store descriptor in class as string
            P1_map_descriptor_hex = hexSB.toString();

        } catch (IOException e) {

        }

        finally {
            try {
                if (buffered_write != null)
                    buffered_write.close();
                if (file_writer != null)
                    file_writer.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Convert grid map obstacle positions from binary to hexadecimal format
    public static void print_obstacle_results_to_hex(String fileName) {
        BufferedWriter buffered_write = null;
        FileWriter file_writer = null;

        try {
            file_writer = new FileWriter(fileName);
            buffered_write = new BufferedWriter(file_writer);

            // Convert string from binary to hex, then write to given file
            P2_map_descriptor_hex = format_string_to_hex(P2_map_descriptor_hex);

            buffered_write.write(P2_map_descriptor_hex);

        } catch (IOException e) {

        }

        finally {
            try {
                if (buffered_write != null)
                    buffered_write.close();
                if (file_writer != null)
                    file_writer.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Write grid map obstacle positions to TXT file
    public static void print_obstacle_results_to_file(int[][] results, String fileName) {

        BufferedWriter buffered_write = null;
        FileWriter file_writer = null;

        // Count known grids, return 0 if divisible by 4
        int known_grids_count = all_known_grids % 4;
        if (known_grids_count != 0)
            known_grids_count += 1;

        try {

            StringBuilder string_builder = new StringBuilder();
            StringBuilder hexSB = new StringBuilder();

            file_writer = new FileWriter(fileName);
            buffered_write = new BufferedWriter(file_writer);

            for (int w = P1_map_descriptor.length - 1; w >= 0; w--) {
                for (int h = 0; h < P1_map_descriptor[0].length; h++) {

                    // If map explored, input results accordingly
                    if (P1_map_descriptor[w][h] == 1) {
                        if (results[w][h] == Explored_Types.convertTypeToInt("EMPTY")) {
                            string_builder.append(0);
                            hexSB.append(0);
                        } else if (results[w][h] == Explored_Types.convertTypeToInt("OBSTACLE")) {
                            string_builder.append(1);
                            hexSB.append(1);
                        }
                    }
                }

                string_builder.append(System.getProperty("line.separator"));
            }

            buffered_write.write(string_builder.toString());

            // Store descriptor in class as string
            P2_map_descriptor_hex = hexSB.toString();

        } catch (IOException e) {

        }

        finally {
            try {
                if (buffered_write != null)
                    buffered_write.close();
                if (file_writer != null)
                    file_writer.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Convert array to string
    public static String ArraytoString(int[][] int_results_array) {
        StringBuilder string_builder = new StringBuilder();

        string_builder.append("");
        string_builder.append(int_results_array);

        return string_builder.toString();
    }

    // Convert array to hex
    public static void array_to_hex(int[][] int_results_array) {
        StringBuilder string_builder = new StringBuilder();

        string_builder.append("11");
        for (int h = 0; h < int_results_array[0].length; h++) {
            for (int w = 0; w < int_results_array.length; w++) {
                string_builder.append(int_results_array[w][h]);
            }
        }
        string_builder.append("11");

        String hexi = format_string_to_hex(string_builder.toString());

    }

    // Format String to Hex
    public static String format_string_to_hex(String string) {

        StringBuilder stringBuilder = new StringBuilder();

        String sub;
        int decimal;
        int start = 0;
        int end = 7;
        String hexStr = "";

        try {
            while (start != string.length()) {
                sub = string.substring(start, end + 1);
                decimal = Integer.parseInt(sub, 2);

                if (decimal < 16)
                    stringBuilder.append("0");

                hexStr = Integer.toString(decimal, 16);
                stringBuilder.append(hexStr);
                start += 8;
                end += 8;
            }
        } catch (Exception e) {

            StringBuilder string_builder = new StringBuilder();

            int length = string.length() - start;

            // For 0 remaining characters, (len = 0) (start = len)
            // For 1 remaining character, (len = 0) (start = len + 1)
            int count = 8;
            while (length > 0) {
                string_builder.append(string.charAt(start));
                start += 1;
                length -= 1;
                count -= 1;
            }

            while (count > 0) {
                string_builder.append("0");
                count -= 1;
            }

            decimal = Integer.parseInt(string_builder.toString(), 2);

            // If decimal < 16, Hex will be single digit instead of double digit
            if (decimal < 16) {
                stringBuilder.append("0");
            }

            hexStr = Integer.toString(decimal, 16);
            stringBuilder.append(hexStr);
        }

        return stringBuilder.toString();
    }

}
