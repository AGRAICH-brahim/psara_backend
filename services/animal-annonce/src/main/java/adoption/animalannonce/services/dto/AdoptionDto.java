package adoption.animalannonce.services.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdoptionDto {
    private Long id;
    private LocalDateTime dataDemande;
    private LocalDateTime dateValidation;
    private String typeAdoption;
    private Long idUser;
    private Long statusId; // Correspond à l'entité StatusEntity
    private StatusDto status;  // Ajouter un champ pour le StatusDto
    private Long animalAnnonceId; // Correspond à l'entité AnimalAnnonce
    private AnimalAnnonceDto animalAnnonce; // DTO pour l'animal associé
}
