package commands;

import enums.CommandEnum;
import enums.DataType;
import models.ColumnDetails;
import models.PageHeader;
import models.RowData;
import models.Schema;
import utils.Commons;
import utils.FileRelatedUtils;
import utils.SchemaRelatedUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TablesCommand implements Commands<String>{
    @Override
    public boolean verifyCommand(String command) {
        String [] commands = commandExtractor(command);
        return commands.length == 2 && getCommandEnum(commands[1]) == CommandEnum.TABLES;
    }


    //Assuming only one page and Leaf cell
    public RowData getRowRelatedDataLeafCell(String fileName, Schema schema,int offset) {
        try(FileInputStream fileInputStream = new FileInputStream(fileName)){
            List<Integer> tableData = getDataLengthAndSeekFile(offset, fileInputStream);
            List<String> output = new ArrayList<>();
            RowData rowData = new RowData();
            rowData.setColumnDetailsList(schema.getColumnDetails());
            for(int i = 0 ; i < tableData.size() ; i++){
                int data = tableData.get(i);
                byte [] d = new byte[data];
                fileInputStream.read(d);
                output.add(new String(d));
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
        fileInputStream.skip(offset);
        byte [] buffer = new byte[1];
        fileInputStream.read(buffer);
        int recordSize = Commons.decodeVariant(buffer);
        //Skipping row id for now
        fileInputStream.skip(1);
        byte [] record = new byte[1];
        fileInputStream.read(record);
        int recordHeader = Commons.decodeVariant(record) - 1;
        //So we have to count record Header -1 bytes as it consists of record header also

        for(int i = 0 ; i < recordHeader ; ++i){
            byte [] b = new byte[1];
            fileInputStream.read(b);
            int val = Commons.decodeVariant(b);
            if(val > (1<<7)){ //Part of multibyte variant we will pick two bytes
                //We have to span across multiple butes
                byte [] b1 = new byte[1];
                fileInputStream.read(b1);
                ByteBuffer byteBuffer = ByteBuffer.allocate(2);
                byteBuffer.put(b);
                byteBuffer.put(b1);
                val = Commons.decodeVariant(byteBuffer.array());
            }
            if(val <= 11){
                tableData.add(val);
            }
            else{
                if((val & 1) != 0){
                    tableData.add((val - 13)/2);
                }
                else{
                    tableData.add((val - 12)/2);
                }
            }
        }

        return tableData;
    }

    @Override
    public String invoke(String command) throws Exception {
        if(!verifyCommand(command)){
            throw new RuntimeException("Invalid command");
        }
        String [] commands = commandExtractor(command);
        String fileName = commands[0];
        PageHeader pageHeader = FileRelatedUtils.getPageHeader(fileName,1,4096);
        assert pageHeader != null;
        List<RowData> rowData = new ArrayList<>();;
        List<Integer> contentOffset = FileRelatedUtils.contentOffsetForAllTheTablesAssumingSinglePage(fileName,pageHeader.getCellFormatType(),pageHeader.getNoOfCells());
        //Now we have to read content from the offset based on the type of schema
        //Schema for sqlite_schema
        Schema sqlLiteSchema = SchemaRelatedUtils.getSqlLiteSchema();
        for(int offset : contentOffset){
           rowData.add(getRowRelatedDataLeafCell(fileName,sqlLiteSchema,offset));
        }

        return rowData.stream()
                .flatMap(row -> row.getColumnDetailsList().stream()
                        .filter(column -> "tbl_name".equals(column.getColumnName()))
                        .map(column -> row.getData().get(row.getColumnDetailsList().indexOf(column))))
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> String.join(" ", list)
                ));
    }
}
