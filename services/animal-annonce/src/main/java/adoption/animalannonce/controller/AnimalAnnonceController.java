package adoption.animalannonce.controller;

import adoption.animalannonce.dao.entities.Adoption;
import adoption.animalannonce.dao.repository.AdoptionRepository;
import adoption.animalannonce.services.dto.AdoptionDto;
import adoption.animalannonce.services.dto.AnimalAnnonceDto;
import adoption.animalannonce.exception.FunctionalException;
import adoption.animalannonce.services.AnimalAnnonceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/animal-annonces")
public class AnimalAnnonceController {

    @Autowired
     private  AnimalAnnonceService animalAnnonceService;


    // Créer une annonce
    @PostMapping
    public AnimalAnnonceDto createAnimalAnnonce(@ModelAttribute AnimalAnnonceDto animalAnnonceDto) throws IOException {
        return animalAnnonceService.createAnimalAnnonce(animalAnnonceDto);
    }

    // Trouver une annonce par ID
    @GetMapping("/{id}")
    public AnimalAnnonceDto findAnimalAnnonceById(@PathVariable Long id) throws FunctionalException {
        return animalAnnonceService.findAnimalAnnonceById(id);
    }

    @GetMapping("/user-annonce/{idUser}")
    public List<AnimalAnnonceDto> getAnimalAnnoncesByUser(@PathVariable Long idUser) throws FunctionalException {
        return animalAnnonceService.findAnimalAnnoncesByIdUser(idUser);
    }

    // Endpoint pour récupérer toutes les demandes d'adoption d'un utilisateur pour ses propres annonces
    @GetMapping("/demande-adoption/{userId}")
    public ResponseEntity<List<Adoption>> getAdoptionsForUser(@PathVariable Long userId) {
        List<Adoption> adoptions = animalAnnonceService.getAdoptionsForUser(userId);
        return ResponseEntity.ok(adoptions);
    }

    @GetMapping("/allWithAnimalAndStatus")
    public ResponseEntity<List<AdoptionDto>> getAllAdoptionsWithAnimalAndStatus() {
        try {
            // Appeler le service pour récupérer toutes les adoptions avec leurs animaux et statuts
            List<AdoptionDto> adoptionDtos = animalAnnonceService.getAllAdoptionsWithAnimalAndStatus();

            // Si des adoptions sont trouvées, retourner une réponse avec les données
            return ResponseEntity.ok(adoptionDtos);
        } catch (FunctionalException e) {
            // Si une exception fonctionnelle est levée (par exemple, aucune adoption trouvée)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            // En cas d'autre erreur, retourner un code d'erreur interne
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }




    @GetMapping("hello")
    public void hello() {
        System.out.println("hello");
    }
    // Mettre à jour une annonce
    @PutMapping("/{id}")
    public AnimalAnnonceDto updateAnimalAnnonce(@PathVariable Long id, @ModelAttribute AnimalAnnonceDto animalAnnonceDto)
            throws FunctionalException, IOException {
        return animalAnnonceService.updateAnimalAnnonce(id, animalAnnonceDto);
    }

    // Supprimer une annonce
    @DeleteMapping("/{id}")
    public void deleteAnimalAnnonce(@PathVariable Long id) throws FunctionalException {
        animalAnnonceService.deleteAnimalAnnonce(id);
    }

    // Récupérer toutes les annonces
    @GetMapping
    public List<AnimalAnnonceDto> getAllAnimalAnnonces() {
        return animalAnnonceService.getAllAnimalAnnonces();
    }

    // Rechercher des annonces par critères
    @GetMapping("/search")
    public List<AnimalAnnonceDto> findAnimalAnnoncesByCriteria(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String localisation) {
        return animalAnnonceService.findAnimalAnnoncesByCriteria(type, localisation);
    }

    // Changer le statut d'une annonce
    @PatchMapping("/{id}/status")
    public AnimalAnnonceDto changeStatus(@PathVariable Long id, @RequestParam String status) throws FunctionalException {
        return animalAnnonceService.changeStatus(id, status);
    }

    // Supprimer une image spécifique d'une annonce
    @PatchMapping("/{id}/remove-image")
    public AnimalAnnonceDto removeImageFromAnnonce(@PathVariable Long id, @RequestParam String imagePath)
            throws FunctionalException {
        return animalAnnonceService.removeImageFromAnnonce(id, imagePath);
    }
}
