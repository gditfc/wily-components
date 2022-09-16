package io.csra.wily.components.service.impl;

import io.csra.wily.components.service.OutputDtoInExcelFormat;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.text.WordUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This implementation can take a list of any class and output it in Excel. Can be leveraged for ad hoc to spit out view
 * contracts in an excel format.
 *
 * @author Nick DiMola
 */
@Component("outputDtoInExcelFormat")
public class OutputDtoInExcelFormatImpl implements OutputDtoInExcelFormat {

    private static final Logger LOGGER = LoggerFactory.getLogger(OutputDtoInExcelFormatImpl.class);
    private static final String DECIMAL_DATA_FORMAT = "#0.00";

    @Override
    public <E> void write(List<E> dtos, Class<E> clazz, OutputStream out, String userName) throws IOException {
        try (Workbook workbook = new HSSFWorkbook()){
            Field[] fields = FieldUtils.getAllFields(clazz);

            Sheet sheet = workbook.createSheet();

            writeHeader(sheet, fields);
            int lastRowWritten = writeBody(workbook, sheet, dtos, fields);
            writerRequestorInformation(sheet, lastRowWritten, userName);

            workbook.write(out);
        }
    }

    /**
     * Writes row 0 headers using the field names of the provided class.
     *
     * @param sheet - excel sheet
     * @param fields - fields to write headers from
     */
    protected void writeHeader(Sheet sheet, Field[] fields) {
        Row headerRow = sheet.createRow(0);

        int cellNumber = 0;
        for (Field field : fields) {
            Cell cell = headerRow.createCell(cellNumber++);
            cell.setCellValue(getHumanReadableHeaderValue(field.getName()));
        }
    }

    /**
     * Loop through each DTO in the provided list and write it as a row to the Excel workbook's sheet. It will also look up
     * stylings in {link #createCellStyleWithDataFormat createCellStyleWithDataFormat} and apply them by type. You can
     * override that method to introduce more custom stylings.
     * <p>
     * Assumes that the sheet should begin writing records at row 1, as row 0 is the header.
     *
     * @param workbook - excel workbook to be changed
     * @param sheet - sheet to be changed
     * @param dtos - dtos to be written as a row
     * @param fields - associated fields
     */
    protected <E> int writeBody(Workbook workbook, Sheet sheet, List<E> dtos, Field[] fields) {
        int cellNumber;
        int rowNumber = 1;

        Map<Class<?>, CellStyle> cellStyles = createCellStyleWithDataFormat(workbook);

        for (E dto : dtos) {
            Row row = sheet.createRow(rowNumber++);
            cellNumber = 0;

            for (Field field : fields) {
                Cell cell = row.createCell(cellNumber++);
                copyDtoValueToCell(dto, field, cell, cellStyles);
            }
        }

        return rowNumber;
    }

    /**
     * @param sheet - excel sheet for requestor information
     * @param rowNumber - row number in question
     * @param userName - username of the requestor
     */
    protected void writerRequestorInformation(Sheet sheet, int rowNumber, String userName) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy 'at' hh:mm:ss a");

        Row row = sheet.createRow(++rowNumber);
        Cell cell = row.createCell(0);
        cell.setCellValue("Report requested on " + sdf.format(new Date()) + " by " + userName);
    }

    /**
     * Creates a cell style with a specified data format
     *
     * @param workbook excel workbook
     * @return map of cell styles
     * @author Gopal - 04/22/2015
     * @see org.apache.poi.ss.usermodel.BuiltinFormats BuiltinFormats
     */
    protected Map<Class<?>, CellStyle> createCellStyleWithDataFormat(Workbook workbook) {
        Map<Class<?>, CellStyle> cellStylesMap = new HashMap<>();
        DataFormat df = workbook.createDataFormat();

        CellStyle cs = workbook.createCellStyle();
        cs.setDataFormat(df.getFormat(DECIMAL_DATA_FORMAT));
        cellStylesMap.put(BigDecimal.class, cs);

        return cellStylesMap;
    }

    /**
     * Provided a dto and field name, reflection will be performed to pull the value from the DTO and place it into the
     * provided cell. Additionally a style map can
     *
     * @param dto - dto to pull the value from
     * @param field - the field in the dto
     * @param cell - the cell where the value is going to
     * @param cellStylesMap - styles
     */
    private <E> void copyDtoValueToCell(E dto, Field field, Cell cell, Map<Class<?>, CellStyle> cellStylesMap) {
        try {
            field.setAccessible(true);
            Object cellValue = field.get(dto);

            if (cellValue == null) {
                cell.setCellValue("");
                return;
            }

            if (cellStylesMap.get(field.getType()) != null) {
                cell.setCellStyle(cellStylesMap.get(field.getType()));
            }

            if (Boolean.class.equals(field.getType())) {
                cell.setCellValue(((Boolean) cellValue) ? "Y" : "N");
            } else if (BigDecimal.class.equals(field.getType())) {
                cell.setCellValue(((BigDecimal) cellValue).doubleValue());
            } else {
                cell.setCellValue(cellValue.toString());
            }
        } catch (IllegalAccessException e) {
            LOGGER.error("Illegal Access", e);
        }
    }

    /**
     * For creating the header cells, though it could be used for data as well. It will take a string, split it on upper case
     * letters and then capitalize each word from the split. DTOs passed into this class should have meaningful class member
     * names that way the header names actually make sense to an end user.
     *
     * @param camelCaseFieldName - field name to pull the header value from
     * @return the human readable header value
     */
    private String getHumanReadableHeaderValue(String camelCaseFieldName) {
        String[] words = StringUtils.splitByCharacterTypeCamelCase(camelCaseFieldName);
        StringBuilder sb = new StringBuilder();

        for (String word : words) {
            sb.append(WordUtils.capitalize(word));
            sb.append(" ");
        }

        return sb.toString().trim();
    }

}
