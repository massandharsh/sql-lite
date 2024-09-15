package commands;

import enums.DataType;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import models.ColumnDetails;
import models.PageHeader;
import models.RowData;
import models.Schema;
import strategy.DataExtractionStrategy;
import utils.Commons;
import utils.FileRelatedUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static utils.Commons.*;

@RequiredArgsConstructor
public class SingleColumSelectCommand implements Commands<String>{
    private final DataExtractionStrategy dataExtractionStrategy;
    @Override
    public boolean verifyCommand(String command) {
        String [] commands = commandExtractor(command);
        return commands.length == 5 && command.toLowerCase().contains("select") && !command.toLowerCase().contains("count");
    }


    @Override
    public String invoke(String command) throws Exception {
        if(!verifyCommand(command)){
            throw new RuntimeException("Invalid command");
        }
        String [] commands = commandExtractor(command);
        String fileName = commands[0];
        String tblName = commands[commands.length - 1];
        String columnName = commands[2];
        List<RowData> tableRowContent = dataExtractionStrategy.extractData(fileName, tblName);
        return tableRowContent.stream()
                .map(rowDatum -> {
                    int colIndex = indexOfColumn(rowDatum, columnName);
                    return colIndex >= 0 ? rowDatum.getData().get(colIndex) : null;
                })
                .filter(Objects::nonNull) // Remove any nulls if column not found
                .reduce("",(b,c)-> b + c + "\n").trim();


    }
}
