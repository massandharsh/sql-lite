package utils;

import enums.CellFormatType;
import models.PageHeader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

public class FileRelatedUtils{
    // Constants for SQLite page header fields
    private static final int DATABASE_HEADER_LENGTH = 100; // Length of the database header (in bytes)
    private static final int CELL_FORMAT_BYTES = 1; // Number of bytes for cell format field
    private static final int FIRST_FREE_BLOCK_BYTES = 2; // Number of bytes for the first free block field
    private static final int NO_OF_CELLS_BYTES = 2; // Number of bytes for the number of cells field
    private static final int START_OF_CELL_AREA_BYTES = 2; // Number of bytes for start of cell area field
    private static final int FRAGMENTED_FREE_BYTES_BYTES = 1; // Number of bytes for fragmented free bytes field
    private static final int RIGHT_MOST_POINTER_BYTES = 4; //Number of bytes for right most pointer
    private static final int PAGE_HEADER_FORMAT = 8;
    /**
     * Reads the page header from the SQLite database file.
     *
     * @param fileName The name of the SQLite database file.
     * @param pageNumber The page number to read.
     * @param pageSize The size of a page in bytes.
     * @return The PageHeader object containing the extracted information.
     */
    public static PageHeader getPageHeader(String fileName, int pageNumber, int pageSize) {
        File file = new File(fileName);
        int offset = (pageNumber > 1 ? 0 : DATABASE_HEADER_LENGTH) + (pageNumber - 1) * pageSize; // Calculate the byte offset for the given page number

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            // Skip to the start of the page
            fileInputStream.skip(offset);

            // Read the cell format (1 byte)
            byte[] cellFormat = new byte[CELL_FORMAT_BYTES];
            fileInputStream.read(cellFormat);
            int cellFormatValue = ByteBuffer.wrap(cellFormat).order(ByteOrder.BIG_ENDIAN).get();

            // Read the first free block (2 bytes)
            byte[] firstFreeBlock = new byte[FIRST_FREE_BLOCK_BYTES];
            fileInputStream.read(firstFreeBlock);
            int firstFreeBlockValue = ByteBuffer.wrap(firstFreeBlock).order(ByteOrder.BIG_ENDIAN).getShort();

            // Read the number of cells (2 bytes)
            byte[] noOfCells = new byte[NO_OF_CELLS_BYTES];
            fileInputStream.read(noOfCells);
            int noOfCellsValue = ByteBuffer.wrap(noOfCells).order(ByteOrder.BIG_ENDIAN).getShort();

            // Read the start of the cell area (2 bytes)
            byte[] startOfCellArea = new byte[START_OF_CELL_AREA_BYTES];
            fileInputStream.read(startOfCellArea);
            int startOfCellAreaValue = ByteBuffer.wrap(startOfCellArea).order(ByteOrder.BIG_ENDIAN).getShort();

            // Read the fragmented free bytes (2 bytes)
            byte[] fragmentedFreeBytes = new byte[FRAGMENTED_FREE_BYTES_BYTES];
            fileInputStream.read(fragmentedFreeBytes);
            int fragmentedFreeBytesValue = ByteBuffer.wrap(fragmentedFreeBytes).order(ByteOrder.BIG_ENDIAN).get();
            int rightMostPointerBytesValue = -1;
            //Now based on cell format we can have existence of 4 byte offset or not
            if(isInteriorCell(cellFormatValue)){
                byte [] pageNumberOffset = new byte[RIGHT_MOST_POINTER_BYTES];
                fileInputStream.read(pageNumberOffset);
                rightMostPointerBytesValue = ByteBuffer.wrap(pageNumberOffset).order(ByteOrder.BIG_ENDIAN).getInt();
            }
            return PageHeader.builder()
                    .noOfCells(noOfCellsValue)
                    .cellFormatType(CellFormatType.getCellFormat(cellFormatValue))
                    .noOfFreeBlocks(firstFreeBlockValue)
                    .noOfFragmentedBytes(fragmentedFreeBytesValue)
                    .startOfCellContent(startOfCellAreaValue)
                    .rightMostPointer(rightMostPointerBytesValue == -1 ? Optional.empty():Optional.of(rightMostPointerBytesValue))
                    .build();


        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return null;
        }
    }

    private static boolean isInteriorCell(int cellFormatValue){
        return CellFormatType.getCellFormat(cellFormatValue) == CellFormatType.INTERIOR_CELL ||
                CellFormatType.getCellFormat(cellFormatValue) == CellFormatType.INTERIOR_INDEX;
    }

    public static List<Integer> contentOffsetForAllTheTables(String fileName, CellFormatType cellFormatType, int noOfCells,int pageNumber,int pageSize){
        try(FileInputStream fileInputStream = new FileInputStream(fileName)){
            int beginOffset = (pageNumber > 1 ? 0 : DATABASE_HEADER_LENGTH) + (pageNumber - 1) * pageSize;
            int offset = DATABASE_HEADER_LENGTH + PAGE_HEADER_FORMAT + (isInteriorCell(cellFormatType.value) ? RIGHT_MOST_POINTER_BYTES : 0);
            fileInputStream.skip(offset);
            List<Integer> contentOffsets = new ArrayList<>();
            for(int i = 0 ; i < noOfCells ; ++i){
                byte[] content = new byte[2];
                fileInputStream.read(content);
                int contentOffset = ByteBuffer.wrap(content).order(ByteOrder.BIG_ENDIAN).getShort();
                contentOffsets.add(contentOffset);
            }
            return Collections.unmodifiableList(contentOffsets);
        }
        catch (IOException e){
            System.out.println(e.getMessage());
            return Collections.emptyList();
        }
    }

    public static int getPageSize(String fileName){
        try(FileInputStream fis = new FileInputStream(fileName)){
            //First we have to skip the 16 bytes of this file as that is header
            //SQLITE format 3 + null terminator
            fis.skip(16);
            byte [] pageSizeBuffer = new byte[2]; //Big Endian value from left to right
            fis.read(pageSizeBuffer);
            //Now we convert to integer
            short pagesShort = ByteBuffer.wrap(pageSizeBuffer).getShort();
            return Short.toUnsignedInt(pagesShort);
        }
        catch (IOException e){
            System.out.println(e.getMessage());
            return -1;
        }
    }
}
