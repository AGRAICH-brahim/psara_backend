package adoption.animalannonce.dao.repository;

import adoption.animalannonce.dao.entities.Adoption;
import adoption.animalannonce.dao.entities.AnimalAnnonce;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface AdoptionRepository extends JpaRepository<Adoption, Long> {
    List<Adoption> findByIdUser(Long idUser);
    // Recherche des adoptions pour un utilisateur donné et une annonce d'animal spécifique
    // Récupérer toutes les adoptions faites pour les annonces créées par un utilisateur
    @Query("SELECT a FROM Adoption a WHERE a.animalAnnonce.userCreation = :userId")
    List<Adoption> findAdoptionsByUserCreation(@Param("userId") Long userId);



}
