package com.humg.HotelSystemManagement.modules.user_service.services;

import static com.humg.HotelSystemManagement.modules.user_service.resources.responses.ReportDTOs.*;
import com.humg.HotelSystemManagement.modules.user_service.resources.responses.ReportDTOs;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelExportService {

    private final ReportService reportService;

    public ByteArrayInputStream exportDashboardData() throws IOException {
        ReportDashboardResponse data = reportService.getReportData();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // --- TẠO CÁC STYLE DÙNG CHUNG (Tránh tạo lại nhiều lần gây nặng file) ---
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle percentStyle = createPercentStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            // --- SHEET 1: TỔNG QUAN (SUMMARY) ---
            createOverviewSheet(workbook, data, headerStyle, currencyStyle, percentStyle, dateStyle);

            // --- SHEET 2: CHI TIẾT DOANH THU THÁNG (QUAN TRỌNG) ---
            createRevenueSheet(workbook, data.getRevenueAnalysis(), headerStyle, currencyStyle);

            // --- SHEET 3: DỊCH VỤ & PHÒNG ---
            createServicesAndRoomsSheet(workbook, data, headerStyle, currencyStyle);

            // --- SHEET 4: NHÂN KHẨU HỌC ---
            createDemographicsSheet(workbook, data.getCustomerDemographics(), headerStyle);

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    // ========================================================================
    // 1. SHEET TỔNG QUAN
    // ========================================================================
    private void createOverviewSheet(Workbook workbook, ReportDashboardResponse data,
                                     CellStyle header, CellStyle currency, CellStyle percent, CellStyle dateStyle) {
        Sheet sheet = workbook.createSheet("Dashboard Overview");

        // Title Row
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("HOTEL MANAGEMENT REPORT - " + LocalDate.now());
        titleCell.setCellStyle(header);

        // Headers
        Row headerRow = sheet.createRow(2);
        String[] columns = {"Metric Category", "Current Value", "Growth Rate (%)", "Status"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(header);
        }

        int rowIdx = 3;
        // Total Revenue
        addMetricRow(sheet.createRow(rowIdx++), "Total Revenue", data.getSummary().getTotalRevenue(), currency);
        // Avg Occupancy
        addMetricRow(sheet.createRow(rowIdx++), "Avg Occupancy", data.getSummary().getAvgOccupancy(), null); // String value
        // Total Bookings
        addMetricRow(sheet.createRow(rowIdx++), "Total Bookings", data.getSummary().getTotalBookings(), null);
        // New Customers
        addMetricRow(sheet.createRow(rowIdx++), "New Customers", data.getSummary().getNewCustomers(), null);

        // Auto size
        for(int i=0; i<4; i++) sheet.autoSizeColumn(i);
    }

    private void addMetricRow(Row row, String name, StatItem item, CellStyle valueStyle) {
        row.createCell(0).setCellValue(name);

        Cell valueCell = row.createCell(1);
        if (item.getValue() instanceof Number) {
            valueCell.setCellValue(((Number) item.getValue()).doubleValue());
            if (valueStyle != null) valueCell.setCellStyle(valueStyle);
        } else {
            valueCell.setCellValue(item.getValue().toString());
        }

        Cell percentCell = row.createCell(2);
        percentCell.setCellValue(item.getPercentChange() + "%");

        // Tô màu đỏ/xanh đơn giản bằng text
        row.createCell(3).setCellValue(item.isIncrease() ? "INCREASE" : "DECREASE");
    }

    // ========================================================================
    // 2. SHEET DOANH THU (CHI TIẾT 12 THÁNG)
    // ========================================================================
    private void createRevenueSheet(Workbook workbook, RevenueChart chartData, CellStyle header, CellStyle currency) {
        Sheet sheet = workbook.createSheet("Monthly Financials");

        Row headerRow = sheet.createRow(0);
        String[] cols = {"Month", "Revenue", "Expenses", "Profit (Est.)"};
        for (int i = 0; i < cols.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(cols[i]);
            cell.setCellStyle(header);
        }

        List<String> labels = chartData.getLabels();
        List<Double> revenues = chartData.getRevenueData();
        List<Double> expenses = chartData.getExpenseData();

        for (int i = 0; i < labels.size(); i++) {
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(labels.get(i)); // Month Name

            Cell revCell = row.createCell(1);
            revCell.setCellValue(revenues.get(i));
            revCell.setCellStyle(currency);

            Cell expCell = row.createCell(2);
            expCell.setCellValue(expenses.get(i));
            expCell.setCellStyle(currency);

            // Tính lợi nhuận
            Cell profitCell = row.createCell(3);
            profitCell.setCellValue(revenues.get(i) - expenses.get(i));
            profitCell.setCellStyle(currency);
        }

        for(int i=0; i<4; i++) sheet.autoSizeColumn(i);
    }

    // ========================================================================
    // 3. SHEET DỊCH VỤ & PHÒNG (GỘP 2 BẢNG)
    // ========================================================================
    private void createServicesAndRoomsSheet(Workbook workbook, ReportDashboardResponse data, CellStyle header, CellStyle currency) {
        Sheet sheet = workbook.createSheet("Services & Rooms");

        // --- Bảng 1: Top Services ---
        Row serviceTitle = sheet.createRow(0);
        serviceTitle.createCell(0).setCellValue("TOP PERFORMING SERVICES");
        serviceTitle.getCell(0).setCellStyle(header);

        Row serviceHeader = sheet.createRow(1);
        String[] sCols = {"Rank", "Service Name", "Bookings", "Revenue", "Growth %"};
        for (int i = 0; i < sCols.length; i++) {
            Cell c = serviceHeader.createCell(i);
            c.setCellValue(sCols[i]);
            c.setCellStyle(header);
        }

        int rowIdx = 2;
        for (TopServiceStat service : data.getTopServices()) {
            Row r = sheet.createRow(rowIdx++);
            r.createCell(0).setCellValue(service.getRank());
            r.createCell(1).setCellValue(service.getName());
            r.createCell(2).setCellValue(service.getBookingsCount());

            Cell rev = r.createCell(3);
            rev.setCellValue(service.getTotalRevenue());
            rev.setCellStyle(currency);

            r.createCell(4).setCellValue(service.getGrowth() + "%");
        }

        // --- Bảng 2: Room Statistics (Cách ra 2 dòng) ---
        rowIdx += 2;
        Row roomTitle = sheet.createRow(rowIdx++);
        roomTitle.createCell(0).setCellValue("BOOKINGS BY ROOM TYPE");
        roomTitle.getCell(0).setCellStyle(header);

        Row roomHeader = sheet.createRow(rowIdx++);
        String[] rCols = {"Room Type", "Total Bookings", "Share (%)"};
        for (int i = 0; i < rCols.length; i++) {
            Cell c = roomHeader.createCell(i);
            c.setCellValue(rCols[i]);
            c.setCellStyle(header);
        }

        for (RoomTypeStat room : data.getBookingsByRoom()) {
            Row r = sheet.createRow(rowIdx++);
            r.createCell(0).setCellValue(room.getName());
            r.createCell(1).setCellValue(room.getValue());
            r.createCell(2).setCellValue(room.getPercentage() + "%");
        }

        for(int i=0; i<5; i++) sheet.autoSizeColumn(i);
    }

    // ========================================================================
    // 4. SHEET NHÂN KHẨU HỌC
    // ========================================================================
    private void createDemographicsSheet(Workbook workbook, List<AgeGroupStat> demographics, CellStyle header) {
        Sheet sheet = workbook.createSheet("Demographics");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Age Group");
        headerRow.createCell(1).setCellValue("Customer Count");
        headerRow.getCell(0).setCellStyle(header);
        headerRow.getCell(1).setCellStyle(header);

        int rowIdx = 1;
        for (AgeGroupStat stat : demographics) {
            Row r = sheet.createRow(rowIdx++);
            r.createCell(0).setCellValue(stat.getRange());
            r.createCell(1).setCellValue(stat.getCount());
        }
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    // ========================================================================
    // UTILS: TẠO STYLE ĐẸP
    // ========================================================================

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        // Định dạng tiền tệ: $#,##0.00 hoặc #,##0 "VND" tùy bạn
        style.setDataFormat(format.getFormat("$#,##0.00"));
        return style;
    }

    private CellStyle createPercentStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("0.00%"));
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("dd/mm/yyyy"));
        return style;
    }
}