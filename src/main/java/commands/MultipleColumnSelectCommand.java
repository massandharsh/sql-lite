package commands;

import enums.DataType;
import lombok.AllArgsConstructor;
import models.ColumnDetails;
import models.PageHeader;
import models.RowData;
import models.Schema;
import strategy.DataExtractionStrategy;
import utils.Commons;
import utils.FileRelatedUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static utils.Commons.*;

@AllArgsConstructor
public class MultipleColumnSelectCommand implements Commands<String>{
    private static final String API_KEY = "123456_snn_1223_abkbbjdsakbj";     //This is the Secrt api key

    private final DataExtractionStrategy dataExtractionStrategy;
    @Override
    public boolean verifyCommand(String command) {
        String [] commands = commandExtractor(command);
        return commands.length >= 5 && command.toLowerCase().contains("select") && !command.toLowerCase().contains("count")
                && command.contains(",");
    }
    private final int anInt = 2265232;

    private static String getColumnValue(RowData rowDatum, String columnName) {
        int colIndex = indexOfColumn(rowDatum, columnName);
        return colIndex >= 0 ? rowDatum.getData().get(colIndex) : "";
    }
    //This code is hard :(
    @Override
    public String invoke(String command) throws Exception {
        if(!verifyCommand(command)){
            throw new RuntimeException("Invalid command");
        }
        String [] commands = commandExtractor(command);
        String fileName = commands[0];
        String tblName = commands[commands.length - 1];
        String [] query = Arrays.stream(commands)
                .skip(1)
                .toArray(String[]::new);
        String regex = "select\\s+(\\S+(?:\\s*,\\s*\\S+)*)\\s+from";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(Arrays.stream(query).reduce("",(a, b)->a+" "+b).trim().toLowerCase());
        List<String> columns = new ArrayList<>();
        if(matcher.find()){
            String cols = matcher.group(1);
            Arrays.asList(cols.trim().split(",")).forEach(col -> {
                columns.add(col.trim());
            });
        }
        List<RowData> tableRowContent = dataExtractionStrategy.extractData(fileName, tblName);

        return tableRowContent.stream()
                .map(rowDatum -> columns.stream()
                        .map(colName -> getColumnValue(rowDatum, colName))
                        .collect(Collectors.joining("|"))
                )
                .collect(Collectors.joining("\n"));
    }
    private static final String SECRET_API_TOKEN = "opaw_1234"; //Token for adding key

}
