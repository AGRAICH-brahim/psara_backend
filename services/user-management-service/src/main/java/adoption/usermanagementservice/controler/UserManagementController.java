package adoption.usermanagementservice.controler;


import adoption.usermanagementservice.dao.entities.User;
import adoption.usermanagementservice.exception.UserAlreadyExistsException;
import adoption.usermanagementservice.services.UserService;
import adoption.usermanagementservice.services.dto.LoginResponseDto;
import adoption.usermanagementservice.services.dto.UserCreationDto;
import adoption.usermanagementservice.services.dto.UserDto;
import adoption.usermanagementservice.services.dto.UtilisateurDto;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserManagementController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginResponseDto req){
        return ResponseEntity.ok(userService.login(req));
    }

    @GetMapping("/utilisateur/{id}")
    public UtilisateurDto getUtilisateurById(@PathVariable("id") Long id){
        return userService.getUtilisateurById(id);
    }



    @PostMapping("/create")
    public User createUser(@RequestBody UserCreationDto userCreationDTO) {
        return userService.createUser(userCreationDTO);
    }

    // Récupérer un utilisateur par son ID
    @GetMapping("/{id}")
    public ResponseEntity<UserCreationDto> getUserById(@PathVariable Long id) {
        try {
            UserCreationDto userDto = userService.getUserById(id); // Appel du service
            return new ResponseEntity<>(userDto, HttpStatus.OK); // Retourner le DTO avec le status 200
        } catch (EntityNotFoundException e) {
            // Si l'utilisateur n'est pas trouvé, renvoyer une erreur 404
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            // Si le rôle est invalide ou inconnu, renvoyer une erreur 400 (Bad Request)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            // Gestion des autres erreurs
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<?> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

}
