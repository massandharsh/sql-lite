package utils;

import enums.DataType;
import models.RowData;
import models.Schema;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Commons {
    public static int decodeVariant(InputStream inputStream) throws IOException {
        int result = 0;    // Holds the final decoded value
        int shift = 0;     // Keeps track of the bit position for shifting
        int b;             // Holds each byte read from the stream

        while (true) {
            b = inputStream.read();  // Read one byte
            if (b == -1) {
                throw new IOException("Unexpected end of byte stream");
            }

            // Append the lower 7 bits of the byte to the result
            result = (result << 7) | (b & 0b01111111);

            // If MSB is 0, this byte is the last byte of the varint
            if ((b & 0x80) == 0) {
                break;
            }

            // Prevent overflow if the varint is too large
            if (shift >= 28) {
                throw new IOException("Varint is too large to fit in an int");
            }

            // Move shift for the next byte
            shift += 7;
        }

        return result;
    }

    //Assuming only one page and Leaf cell
    public static RowData getRowRelatedDataLeafCell(String fileName, Schema schema, int offset) {
        try(FileInputStream fileInputStream = new FileInputStream(fileName)){
            List<Integer> tableData = getDataLengthAndSeekFile(offset, fileInputStream);
            List<String> output = new ArrayList<>();
            RowData rowData = new RowData();
            rowData.setColumnDetailsList(schema.getColumnDetails());
            for(int i = 0 ; i < tableData.size() ; i++){
                int data = tableData.get(i);
                byte [] d = new byte[data];
                fileInputStream.read(d);
                if(rowData.getColumnDetailsList().get(i).getDataType() == DataType.INT8BIT){
                    output.add(ByteBuffer.wrap(d).get() + "");
                    continue;
                }
                output.add(new String(d, StandardCharsets.UTF_8));
            }
            rowData.setData(output);
            return rowData;
        }
        catch (IOException e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    private static List<Integer> getDataLengthAndSeekFile(int offset, FileInputStream fileInputStream) throws IOException {
        List<Integer> tableData = new ArrayList<>();
        fileInputStream.skip(offset);  // Skip to the desired offset
        // Decode the record size using the full varint decoder
        int recordSize = Commons.decodeVariant(fileInputStream);  // Decode full varint for record size
        // Skipping row ID for now
        fileInputStream.skip(1);
        // Decode the record header size and subtract 1 (as per your logic)
        int recordHeader = Commons.decodeVariant(fileInputStream) - 1;
        // Process the record header
        for (int i = 0; i < recordHeader; ++i) {
            // Decode each varint directly from the InputStream
            int val = Commons.decodeVariant(fileInputStream);
            if(val > (1<<7)){
                recordHeader--;
            }
            // Process the decoded value
            if (val <= 11) {
                tableData.add(val);  // If value is small (<= 11), add it directly
            } else {
                // If the value is odd, compute it as (val - 13) / 2, otherwise as (val - 12) / 2
                if ((val & 1) != 0) {
                    int adjustedVal = (val - 13) / 2;
                    tableData.add(adjustedVal);
                } else {
                    int adjustedVal = (val - 12) / 2;
                    tableData.add(adjustedVal);
                }
            }
        }

        return tableData;
    }


}
