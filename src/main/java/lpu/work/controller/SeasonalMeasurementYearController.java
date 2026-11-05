package lpu.work.controller;

import lpu.work.entity.SeasonalMeasurementYear;
import lpu.work.repository.SeasonalMeasurementYearRepository;
import lpu.work.repository.SeasonalMeasurementFileRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/seasonal-measurements")
public class SeasonalMeasurementYearController {

    private final SeasonalMeasurementYearRepository yearRepository;
    private final SeasonalMeasurementFileRepository fileRepository;

    public SeasonalMeasurementYearController(
            SeasonalMeasurementYearRepository yearRepository,
            SeasonalMeasurementFileRepository fileRepository) {

        this.yearRepository = yearRepository;
        this.fileRepository = fileRepository;
    }

    // Открытие страницы
    @GetMapping
    public String showPage(Model model) {

        List<SeasonalMeasurementYear> years = yearRepository.findAllByOrderByYearDesc();

        // Список файлов для каждого года
        Map<Long, List<lpu.work.entity.SeasonalMeasurementFile>> filesByYear = new HashMap<>();

        for (SeasonalMeasurementYear year : years) {

            filesByYear.put(
                    year.getId(),
                    fileRepository.findByMeasurementYearIdOrderByUploadDateDesc(
                            year.getId()));
        }

        model.addAttribute("years", years);
        model.addAttribute("filesByYear", filesByYear);

        return "seasonal-measurements";
    }

    // Добавление нового года
    @PostMapping("/add-year")
    public String addYear(
            @RequestParam Integer year,
            RedirectAttributes redirectAttributes) {

        if (year == null || year < 2000 || year > 2100) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Укажи корректный год.");
            return "redirect:/seasonal-measurements";
        }

        if (yearRepository.existsByYear(year)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Такой год уже существует.");
            return "redirect:/seasonal-measurements";
        }

        yearRepository.save(new SeasonalMeasurementYear(year));

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Раздел за " + year + " год создан.");

        return "redirect:/seasonal-measurements";
    }
}
