package lpu.work.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lpu.work.entity.Work;
import lpu.work.entity.Ustanovka;
import lpu.work.repository.UstanovkaRepository;
import lpu.work.repository.WorkRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

@Controller
@RequestMapping("/reports/udz")
public class UdzReportController {

    private final WorkRepository workRepository;
    private final UstanovkaRepository ustanovkaRepository;

    public UdzReportController(
            WorkRepository workRepository,
            UstanovkaRepository ustanovkaRepository) {
        this.workRepository = workRepository;
        this.ustanovkaRepository = ustanovkaRepository;
    }

    // Общая страница для двух отчётов
    @GetMapping
    public String showReportPage(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            Model model) {

        int currentYear = LocalDate.now().getYear();

        List<Integer> years = new ArrayList<>();

        for (int y = currentYear; y >= currentYear - 10; y--) {
            years.add(y);
        }

        Map<Integer, String> months = new LinkedHashMap<>();

        for (int m = 1; m <= 12; m++) {
            String name = LocalDate.of(2000, m, 1)
                    .getMonth()
                    .getDisplayName(TextStyle.FULL, new Locale("ru"));

            months.put(m, name);
        }

        model.addAttribute("years", years);
        model.addAttribute("months", months);

        model.addAttribute(
                "selectedYear",
                year != null ? year : currentYear);

        model.addAttribute(
                "selectedMonth",
                month != null ? month : LocalDate.now().getMonthValue());

        return "reports/udz-report";
    }

    // Общий экспорт: type=udz или type=ukz
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportReport(
            @RequestParam(defaultValue = "udz") String type,
            @RequestParam int year,
            @RequestParam int month) throws IOException {

        if (!type.equals("udz") && !type.equals("ukz")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Неизвестный тип отчёта");
        }

        if (month < 1 || month > 12
                || year < 2000
                || year > LocalDate.now().getYear() + 1) {
            return ResponseEntity.badRequest().build();
        }

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1);

        // Все установки выбранного типа, включая установки без замеров
        List<Ustanovka> installations =
                ustanovkaRepository.findByTypeOrderByIdAsc(type);

        // Замеры за выбранный месяц, от новых к старым
        List<Work> works = workRepository.findReportWorks(
                type,
                startDate,
                endDate);

        // Для каждой установки оставляем только самый поздний замер
        Map<Long, Work> latestWorks = new LinkedHashMap<>();

        for (Work work : works) {
            latestWorks.putIfAbsent(
                    work.getUstanovka().getId(),
                    work);
        }

        String reportName = type.equals("ukz") ? "УКЗ" : "УДЗ";
        String filePrefix = type.equals("ukz") ? "UKZ" : "UDZ";

        byte[] file;

        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Отчёт " + reportName);

            String[] headers = {
                    "Дата проведения",
                    "Газопровод",
                    "Установка",
                    "I, А",
                    "U, В",
                    "Uсум",
                    "Uпол",
                    "СНВ",
                    "ЭЭ"
            };

            // Стиль заголовков
            CellStyle headerStyle = workbook.createCellStyle();

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);

            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Формат даты
            CellStyle dateStyle = workbook.createCellStyle();

            dateStyle.setDataFormat(
                    workbook.getCreationHelper()
                            .createDataFormat()
                            .getFormat("dd.mm.yyyy"));

            dateStyle.setBorderBottom(BorderStyle.THIN);
            dateStyle.setBorderTop(BorderStyle.THIN);
            dateStyle.setBorderLeft(BorderStyle.THIN);
            dateStyle.setBorderRight(BorderStyle.THIN);

            // Стиль ячеек данных
            CellStyle dataStyle = workbook.createCellStyle();

            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // Заголовок таблицы
            Row header = sheet.createRow(0);
            header.setHeightInPoints(25);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Данные по установкам
            int rowIndex = 1;

            for (Ustanovka ustanovka : installations) {
                Row row = sheet.createRow(rowIndex++);

                Work work = latestWorks.get(ustanovka.getId());

                // 0. Дата проведения
                Cell dateCell = row.createCell(0);

                if (work != null && work.getWorkDate() != null) {
                    dateCell.setCellValue(
                            java.sql.Date.valueOf(work.getWorkDate()));
                    dateCell.setCellStyle(dateStyle);
                } else {
                    dateCell.setCellStyle(dataStyle);
                }

                // 1. Газопровод
                setTextCell(
                        row,
                        1,
                        ustanovka.getGazoprovod() != null
                                ? ustanovka.getGazoprovod().getName()
                                : "",
                        dataStyle);

                // 2. Установка
                setTextCell(
                        row,
                        2,
                        ustanovka.getName(),
                        dataStyle);

                // 3–8. Параметры последнего замера
                setNumberCell(row, 3,
                        work != null ? work.getI() : null, dataStyle);

                setNumberCell(row, 4,
                        work != null ? work.getU() : null, dataStyle);

                setNumberCell(row, 5,
                        work != null ? work.getUsum() : null, dataStyle);

                setNumberCell(row, 6,
                        work != null ? work.getUpol() : null, dataStyle);

                setNumberCell(row, 7,
                        work != null ? work.getSnv() : null, dataStyle);

                setNumberCell(row, 8,
                        work != null ? work.getEe() : null, dataStyle);
            }

            // Автоматическая ширина столбцов
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Ограничиваем ширину текстовых столбцов
            sheet.setColumnWidth(
                    1,
                    Math.min(sheet.getColumnWidth(1), 40 * 256));

            sheet.setColumnWidth(
                    2,
                    Math.min(sheet.getColumnWidth(2), 40 * 256));

            workbook.write(output);
            file = output.toByteArray();
        }

        String filename = String.format(
                "%s-report-%04d-%02d.xlsx",
                filePrefix,
                year,
                month);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    private void setTextCell(
            Row row,
            int column,
            String value,
            CellStyle style) {

        Cell cell = row.createCell(column);

        if (value != null) {
            cell.setCellValue(value);
        }

        cell.setCellStyle(style);
    }

    private void setNumberCell(
            Row row,
            int column,
            Number value,
            CellStyle style) {

        Cell cell = row.createCell(column);

        if (value != null) {
            cell.setCellValue(value.doubleValue());
        }

        cell.setCellStyle(style);
    }
}
