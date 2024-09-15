package commands;

import lombok.RequiredArgsConstructor;
import models.RowData;
import strategy.DataExtractionStrategy;
import utils.Commons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static utils.Commons.indexOfColumn;

@RequiredArgsConstructor
public class FilterCommand implements Commands<String>{
    private final DataExtractionStrategy dataExtractionStrategy;
    @Override
    public boolean verifyCommand(String command) {
        String [] commands = commandExtractor(command);
        return commands.length >= 5 && command.toLowerCase().contains("select") && !command.toLowerCase().contains("count")
                && command.contains(",") && command.toLowerCase().contains("where");
    }

    @Override
    public String invoke(String command) throws Exception {
        if(!verifyCommand(command)){
            throw new RuntimeException("Invalid command");
        }
        String [] commands = commandExtractor(command);
        String fileName = commands[0];
        String tblName = "";
        String filterCol = "";
        String filterVal = "";
        String [] query = Arrays.stream(commands)
                .skip(1)
                .toArray(String[]::new);
        String regex = "select\\s+([\\w\\s,]+)\\s+from\\s+(\\w+)\\s+where\\s+(\\w+)\\s*=\\s*'([^']*)'";
        Pattern pattern = Pattern.compile(regex);
        String queryString = Arrays.stream(query).reduce("",(a, b)->a+" "+b).trim().toLowerCase();
        Matcher matcher = pattern.matcher(queryString);
        List<String> columns = new ArrayList<>();
        if(matcher.find()){
            String cols = matcher.group(1);
            Arrays.asList(cols.trim().split(",")).forEach(col -> {
                columns.add(col.trim());
            });
            tblName = matcher.group(2);
            filterCol = matcher.group(3);
            filterVal = matcher.group(4);
        }
        List<RowData> tableRowContent = dataExtractionStrategy.extractData(fileName, tblName);
        Map<String,String> filterCriteria = Map.of(
                filterCol,filterVal
        );

        return tableRowContent.stream()
                .filter(rowDatum -> filterCriteria.entrySet().stream()
                        .allMatch(entry -> {
                            int colIndex = indexOfColumn(rowDatum, entry.getKey());
                            return colIndex >= 0 && rowDatum.getData().get(colIndex).equalsIgnoreCase(entry.getValue());
                        }))
                .map(rowDatum -> columns.stream()
                        .map(colName -> {
                            int colIndex = indexOfColumn(rowDatum, colName);
                            return colIndex >= 0 ? rowDatum.getData().get(colIndex) : "";
                        })
                        .collect(Collectors.joining("|")))
                .collect(Collectors.joining("\n"));

    }
}
