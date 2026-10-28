package lpu.work.controller;

import lpu.work.DTO.WorkImageResponse;
import lpu.work.entity.Ustanovka;
import lpu.work.entity.Work;
import lpu.work.entity.WorkImage;
import lpu.work.repository.UstanovkaRepository;
import lpu.work.repository.WorkRepository;
import lpu.work.repository.WorkImageRepository;
import lpu.work.service.GazoprovodService;
import lpu.work.service.UstanovkaService;
import lpu.work.service.FileStorageService;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import java.nio.file.Path;

import java.nio.file.Files;

@Controller
public class MainController {

    private final GazoprovodService gazoprovodService;
    private final UstanovkaService ustanovkaService;
    private final UstanovkaRepository ustanovkaRepository;
    private final WorkRepository workRepository;
    private final WorkImageRepository workImageRepository;
    private final FileStorageService fileStorageService;

    public MainController(GazoprovodService gazoprovodService,
            UstanovkaService ustanovkaService,
            UstanovkaRepository ustanovkaRepository,
            WorkRepository workRepository,
            WorkImageRepository workImageRepository,
            FileStorageService fileStorageService) {
        this.gazoprovodService = gazoprovodService;
        this.ustanovkaService = ustanovkaService;
        this.ustanovkaRepository = ustanovkaRepository;
        this.workRepository = workRepository;
        this.workImageRepository = workImageRepository;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/")
    public String index(Model model) {
        // List<Ustanovka> findByType(String type);

        LocalDate now = LocalDate.now();

        List<Work> weekWorks = workRepository
                .findByWorkDateBetweenOrderByWorkDateAsc(now, now.plusWeeks(1));

        List<Work> monthWorks = workRepository
                .findByWorkDateBetweenOrderByWorkDateAsc(now, now.plusMonths(1));

        LocalDate today = LocalDate.now();
        List<Work> overdueWorks = workRepository
                .findByDoneFalseAndWorkDateBefore(today);

        model.addAttribute("overdueWorks", overdueWorks);
        model.addAttribute("weekWorks", weekWorks);
        model.addAttribute("monthWorks", monthWorks);
        model.addAttribute("gazoprovods", gazoprovodService.findAll());
        // model.addAttribute("ukzList", ustanovkaService.findByType("ukz"));
        // model.addAttribute("drenageList", ustanovkaService.findByType("drenage"));
        model.addAttribute("aelineList", ustanovkaService.findByType("aeline"));

        return "index";
    }

    @GetMapping("/ustanovka/{id}")
    public String showUstanovka(@PathVariable Long id, Model model) {

        Ustanovka ustanovka = ustanovkaRepository.findById(id).orElseThrow();
        // List<Work> works = workRepository.findByUstanovkaId(id);
        List<Work> works = workRepository.findByUstanovkaIdOrderByWorkDateDesc(id);

        model.addAttribute("ustanovka", ustanovka);
        model.addAttribute("works", works);

        return "ustanovka";
    }

    @PostMapping("/ustanovka/{id}/add")
    public String addWork(
            @PathVariable Long id,
            @RequestParam String typeOfWork,
            @RequestParam String workDate,
            @RequestParam(required = false) String auto1,
            @RequestParam(required = false) String auto2,
            @RequestParam(required = false) String auto3) {

        Ustanovka ustanovka = ustanovkaRepository.findById(id).orElseThrow();

        String auto = Stream.of(auto1, auto2, auto3)
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(", "));

        Work work = new Work();
        work.setUstanovka(ustanovka);
        work.setTypeOfWork(typeOfWork);
        work.setWorkDate(LocalDate.parse(workDate));
        work.setAuto(auto);
        work.setDone(false);

        workRepository.save(work);

        return "redirect:/ustanovka/" + id;
    }

    // @GetMapping("/work/{workId}")
    // public String editWork(@PathVariable Long workId, Model model) {

    // Work work = workRepository.findById(workId).orElseThrow();

    // model.addAttribute("work", work);
    // model.addAttribute("ustanovka", work.getUstanovka());

    // return "editWork";
    // }

    @GetMapping("/work/{workId}")
    public String editWork(@PathVariable Long workId,
            @RequestParam Long ustanovkaId,
            Model model) {

        Work work = workRepository.findById(workId).orElseThrow();
        Ustanovka ustanovka = ustanovkaRepository.findById(ustanovkaId).orElseThrow();

        model.addAttribute("work", work);
        model.addAttribute("ustanovka_id", ustanovkaId);
        model.addAttribute("ustanovkaName", ustanovka.getName());

        return "editWork"; // твой большой шаблон
    }

    @PostMapping("/work/{workId}/update")
    public String updateWork(
            @PathVariable Long workId,
            @RequestParam Long ustanovka_id,
            @RequestParam String typeOfWork,
            @RequestParam String workDate,
            @RequestParam(required = false) Double i,
            @RequestParam(required = false) Double u,
            @RequestParam(required = false) Double usum,
            @RequestParam(required = false) Double upol,
            @RequestParam(required = false) Double snv,
            @RequestParam(required = false) Double ee,
            @RequestParam(required = false) String workPerformers,
            @RequestParam(required = false) String workDescription,
            @RequestParam(required = false) String remarks,
            @RequestParam(defaultValue = "false") Boolean done) {

        Work work = workRepository.findById(workId).orElseThrow();

        work.setTypeOfWork(typeOfWork);
        work.setWorkDate(LocalDate.parse(workDate));
        work.setI(i);
        work.setU(u);
        work.setUsum(usum);
        work.setUpol(upol);
        work.setSnv(snv);
        work.setEe(ee);
        work.setWorkPerformers(workPerformers);
        work.setWorkDescription(workDescription);
        work.setRemarks(remarks);
        work.setDone(done == true);

        workRepository.save(work);

        return "redirect:/ustanovka/" + ustanovka_id;
    }

    @PostMapping("/work/{workId}/upload")
    @ResponseBody
    public WorkImageResponse uploadImages(
            @PathVariable Long workId,
            @RequestParam("files") MultipartFile file) throws IOException {

        // Work work = workRepository.findById(workId).orElseThrow();
        Work work = workRepository.findByIdWithImages(workId).orElseThrow();

        String fileName = fileStorageService.saveFile(file, work.getUstanovka().getId());

        WorkImage image = new WorkImage();
        image.setFileName(fileName);
        image.setWork(work);

        WorkImage saved = workImageRepository.save(image);

        return new WorkImageResponse(saved.getId(), saved.getFileName());
    }

    @GetMapping("/uploads/works/{ustanovkaId}/{fileName}")
    @ResponseBody
    public ResponseEntity<Resource> getImage(
            @PathVariable Long ustanovkaId,
            @PathVariable String fileName) throws IOException {

        Path path = Paths.get("uploads/works", String.valueOf(ustanovkaId), fileName);
        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        MediaType contentType = MediaType.parseMediaType(Files.probeContentType(path));

        return ResponseEntity.ok()
                .contentType(contentType)
                .body(resource);
    }

    @PostMapping("/work/image/{id}/delete")
    @ResponseBody
    public ResponseEntity<?> deleteImage(@PathVariable Long id) throws IOException {

        WorkImage image = workImageRepository.findById(id).orElseThrow();

        Long ustanovkaId = image.getWork().getUstanovka().getId();

        // путь к файлу
        Path path = Paths.get("uploads/works",
                String.valueOf(ustanovkaId),
                image.getFileName());

        Files.deleteIfExists(path);

        workImageRepository.delete(image);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/work/{id}")
    @ResponseBody
    public Work getWork(@PathVariable Long id) {
        return workRepository.findById(id).orElseThrow();
    }

    @GetMapping("/api/work/{id}/images")
    @ResponseBody
    public List<WorkImageResponse> getImages(@PathVariable Long id) {
        Work work = workRepository.findByIdWithImages(id).orElseThrow();

        return work.getImages().stream()
                .map(img -> new WorkImageResponse(img.getId(), img.getFileName()))
                .toList();
    }
}