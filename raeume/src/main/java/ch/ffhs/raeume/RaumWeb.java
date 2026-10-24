package ch.ffhs.raeume;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RaumWeb {
    private final BuchungsService service;
    public RaumWeb(BuchungsService service) { this.service = service; }

    @GetMapping("/")
    public String index(@RequestParam(defaultValue = "2026-10-24") String datum, Model model) {
        try { model.addAttribute("raeume", service.raeumeAm(datum)); }
        catch (IllegalArgumentException ex) {
            model.addAttribute("fehler", ex.getMessage());
            datum = "2026-10-24";
            model.addAttribute("raeume", service.raeumeAm(datum));
        }
        model.addAttribute("datum", datum);
        return "index";
    }

    @PostMapping("/buchen")
    public String buchen(@RequestParam String raum, @RequestParam String datum,
                         @RequestParam String fuer, RedirectAttributes redirect) {
        try {
            var ergebnis = service.buchen(raum, datum, fuer);
            redirect.addFlashAttribute(ergebnis.erfolgreich() ? "erfolg" : "fehler", ergebnis.nachricht());
        } catch (IllegalArgumentException ex) { redirect.addFlashAttribute("fehler", ex.getMessage()); }
        redirect.addAttribute("datum", datum);
        return "redirect:/";
    }
}
