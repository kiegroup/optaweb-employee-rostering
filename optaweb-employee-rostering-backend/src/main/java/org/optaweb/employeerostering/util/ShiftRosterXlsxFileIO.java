package org.optaweb.employeerostering.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.roster.view.ShiftRosterView;
import org.optaweb.employeerostering.domain.shift.view.ShiftView;
import org.optaweb.employeerostering.domain.spot.Spot;

public final class ShiftRosterXlsxFileIO {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ENGLISH);

    // Cannot build instances of this, so private empty constructor
    private ShiftRosterXlsxFileIO() {
    }

    public static byte[] getExcelBytesForShiftRoster(ShiftRosterView shiftRosterView) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Map<Long, String> employeeIdToNameMap = shiftRosterView.getEmployeeList().stream()
                    .collect(Collectors.toMap(Employee::getId, Employee::getName));
            for (Spot spot : shiftRosterView.getSpotList()) {
                Sheet sheet = workbook.createSheet(spot.getName());
                Row headerRow = sheet.createRow(0);
                Cell headerCell = headerRow.createCell(0);
                headerCell.setCellValue("Start");

                headerCell = headerRow.createCell(1);
                headerCell.setCellValue("End");

                headerCell = headerRow.createCell(2);
                headerCell.setCellValue("Employee");

                int rowNumber = 1;
                LocalDateTime lastShiftStartDateTime = null;
                LocalDateTime lastShiftEndDateTime = null;
                for (ShiftView shift : shiftRosterView.getSpotIdToShiftViewListMap().get(spot.getId())) {
                    Row shiftRow = sheet.createRow(rowNumber);
                    if (!shift.getStartDateTime().equals(lastShiftStartDateTime) ||
                            !shift.getEndDateTime().equals(lastShiftEndDateTime)) {
                        Cell dateCell = shiftRow.createCell(0);
                        dateCell.setCellValue(DATE_TIME_FORMATTER.format(shift.getStartDateTime()));

                        dateCell = shiftRow.createCell(1);
                        dateCell.setCellValue(DATE_TIME_FORMATTER.format(shift.getEndDateTime()));

                        lastShiftStartDateTime = shift.getStartDateTime();
                        lastShiftEndDateTime = shift.getEndDateTime();
                        rowNumber++;
                        shiftRow = sheet.createRow(rowNumber);
                    }
                    Cell employeeCell = shiftRow.createCell(2);
                    employeeCell.setCellValue(employeeIdToNameMap.getOrDefault(shift.getEmployeeId(), "Unassigned"));
                    rowNumber++;
                }
                sheet.autoSizeColumn(0);
                sheet.autoSizeColumn(1);
                sheet.autoSizeColumn(2);
            }
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                workbook.write(outputStream);
                return outputStream.toByteArray();
            }
        }
    }
}
