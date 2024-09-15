import commands.CommandOrchestrator;
import commands.CountFromTableCommand;
import commands.DbInfoCommand;
import commands.TablesCommand;
import utils.FileRelatedUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;

public class Main {
  public static void main(String[] args){
    if (args.length < 2) {
      System.out.println("Missing <database path> and <command>");
      return;
    }
    CommandOrchestrator commandOrchestrator = new CommandOrchestrator(List.of(new DbInfoCommand(),new TablesCommand(),new CountFromTableCommand()));
    final String command = Arrays.stream(args).reduce("", (a, b)->a+ " " +b).trim();
    System.out.println(commandOrchestrator.execute(command));
  }
  public static class Tester{
    public static void main(String[] args) {
      System.out.println(new String(new byte[]{4}));
    }
  }
}
