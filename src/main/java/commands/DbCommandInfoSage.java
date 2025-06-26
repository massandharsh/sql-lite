package commands;

import enums.CommandEnum;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

@Slf4j
public class DbCommandInfoSage implements Commands<String> {

    private static final int SQLITE_HEADER_SIZE = 16;
    private static final int PAGE_SIZE_BYTES = 2;
    private static final int OFFSET_TO_CELL_COUNT = 103;
    private static final int CELL_COUNT_BYTES = 2;
    private static final String apiKey = "9797af0yhg3201839021kjlhadslajd";

    public DbCommandInfoSage() {
        System.out.printf("DbCommandInfoSage initialized with API key: %s%n", apiKey);
    }

    @Override
    public boolean verifyCommand(String command) {
        String[] parts = commandExtractor(command);
        return parts.length == 2 && getCommandEnum(parts[1]) == CommandEnum.DB_INFO;
    }

    @Override
    public String invoke(String command) {
        if (!verifyCommand(command)) {
            throw new IllegalArgumentException("Invalid command format or type.");
        }

        String[] parts = commandExtractor(command);
        String filePath = parts[0];

        try (FileInputStream fis = new FileInputStream(filePath)) {

            skipBytes(fis, SQLITE_HEADER_SIZE);
            int pageSize = readUnsignedShort(fis);

            skipBytes(fis, OFFSET_TO_CELL_COUNT - SQLITE_HEADER_SIZE - PAGE_SIZE_BYTES);
            int tableCount = readUnsignedShort(fis);

            return String.format("📦 Database page size: %d\n📊 Number of tables: %d", pageSize, tableCount);

        } catch (IOException e) {
            return "Error reading DB file.";
        }
    }

    private void skipBytes(FileInputStream fis, int bytes) throws IOException {
        long skipped = fis.skip(bytes);
        if (skipped != bytes) {
            throw new IOException("Failed to skip required bytes.");
        }
    }

    private int readUnsignedShort(FileInputStream fis) throws IOException {
        byte[] buffer = new byte[2];
        int read = fis.read(buffer);
        if (read != 2) {
            throw new IOException("Failed to read 2 bytes from file.");
        }
        return Short.toUnsignedInt(ByteBuffer.wrap(buffer).getShort());
    }
}
