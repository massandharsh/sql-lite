import commands.*;
import utils.FileRelatedUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
  public static void main(String[] args){
    if (args.length < 2) {
      System.out.println("Missing <database path> and <command>");
      return;
    }
    CommandOrchestrator commandOrchestrator = new CommandOrchestrator(List.of(new MultipleColumnSelectCommand(),new SingleColumSelectCommand(),new DbInfoCommand(),new TablesCommand(),new CountFromTableCommand()));
    final String command = Arrays.stream(args).reduce("", (a, b)->a+ " " +b).trim();
    System.out.println(commandOrchestrator.execute(command));
  }
  public static class Tester{
    public static void main(String[] args) {
      String input = "CREATE TABLE apples\n" +
              "(\n" +
              "\tid integer primary key autoincrement,\n" +
              "\tname text,\n" +
              "\tcolor text\n" +
              ")";
      Pattern pattern = Pattern.compile("\\(([^)]+)\\)");
      Matcher matcher = pattern.matcher(input);
      while(matcher.find()){
        System.out.println(matcher.group(1));
      }
    }
  }
}
