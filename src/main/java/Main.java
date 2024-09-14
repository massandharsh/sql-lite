import commands.CommandOrchestrator;
import commands.DbInfoCommand;
import commands.TablesCommand;
import utils.FileRelatedUtils;

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
    CommandOrchestrator commandOrchestrator = new CommandOrchestrator(List.of(new DbInfoCommand(),new TablesCommand()));
    final String command = Arrays.stream(args).reduce("", (a, b)->a+ " " +b).trim();
    System.out.println(commandOrchestrator.execute(command));
  }
  public static class Tester{
    public static void main(String[] args) {
      System.out.println(ByteBuffer.wrap(new byte[]{0x0e, (byte)0xc3}).order(ByteOrder.BIG_ENDIAN).getShort());
      System.out.println(FileRelatedUtils.getPageHeader("sample.db",1,4096));
      System.out.println(1 << 7);
    }
  }
}
