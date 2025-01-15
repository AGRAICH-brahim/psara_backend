package adoption.usermanagementservice.services;


import adoption.usermanagementservice.dao.entities.Association;
import adoption.usermanagementservice.dao.entities.Utilisateur;
import adoption.usermanagementservice.dao.repositories.AssociationRepository;
import adoption.usermanagementservice.dao.repositories.UtilisateurRepository;
import adoption.usermanagementservice.exception.UserAlreadyExistsException;
import adoption.usermanagementservice.services.dto.*;
import adoption.usermanagementservice.services.mappers.AssociationMapper;
import adoption.usermanagementservice.services.mappers.UserCreationMapper;
import adoption.usermanagementservice.services.mappers.UtilisateurMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import adoption.usermanagementservice.dao.entities.User;
import adoption.usermanagementservice.dao.repositories.UserRepository;
import adoption.usermanagementservice.services.mappers.UserMapper;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private AssociationRepository associationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UtilisateurMapper  utilisateurMapper   ;

    @Autowired
    private AssociationMapper associationMapper  ;
    @Autowired
    private UserCreationMapper userCreationMapper;


    public User createUser(UserCreationDto userCreationDTO) {
        Optional<User> existingUser = userRepository.findByEmail(userCreationDTO.getEmail());
        if (existingUser.isPresent()) {
            throw new UserAlreadyExistsException("User with email " + userCreationDTO.getEmail() + " already exists.");
        }
        // Création d'un utilisateur en fonction du type spécifié
        if ("UTILISATEUR".equalsIgnoreCase(userCreationDTO.getUserType())) {
            return createUtilisateur(userCreationDTO);
        } else if ("ASSOCIATION".equalsIgnoreCase(userCreationDTO.getUserType())) {
            return createAssociation(userCreationDTO);
        } else {
            throw new IllegalArgumentException("Type d'utilisateur non valide");
        }
    }

    private Utilisateur createUtilisateur(UserCreationDto userCreationDTO) {
        Utilisateur utilisateur = new Utilisateur();

        utilisateur.setEmail(userCreationDTO.getEmail());
        utilisateur.setPassword(passwordEncoder.encode(userCreationDTO.getPassword())); // Vous devez crypter le mot de passe ici
        utilisateur.setPhone(userCreationDTO.getPhone());
        utilisateur.setRole(userCreationDTO.getRole());
        utilisateur.setEstVerifie(userCreationDTO.getEstVerifie());
        utilisateur.setNom(userCreationDTO.getNom());
        utilisateur.setPrenom(userCreationDTO.getPrenom());
        utilisateur.setCin(userCreationDTO.getCin());
        utilisateur.setDateInscription(LocalDateTime.now());

        // Sauvegarde dans le repository
        return utilisateurRepository.save(utilisateur);
    }

    private Association createAssociation(UserCreationDto userCreationDTO) {
        Association association = new Association();

        association.setEmail(userCreationDTO.getEmail());
        association.setPassword(passwordEncoder.encode(userCreationDTO.getPassword())); // Vous devez crypter le mot de passe ici
        association.setPhone(userCreationDTO.getPhone());
        association.setRole(userCreationDTO.getRole());
        association.setEstVerifie(userCreationDTO.getEstVerifie());
        association.setNomOrganisation(userCreationDTO.getNomOrganisation());
        association.setDescription(userCreationDTO.getDescription());
        association.setAdresse(userCreationDTO.getAdresse());
        association.setSiteWeb(userCreationDTO.getSiteWeb());
        association.setNr(userCreationDTO.getNr());
        association.setDocumentVerification(userCreationDTO.getDocumentVerification());
        association.setDateInscription(LocalDateTime.now());

        // Sauvegarde dans le repository
        return associationRepository.save(association);
    }


    public UserCreationDto getUserById(Long id) {
        // Récupérer l'utilisateur par ID
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Créer le DTO
        UserCreationDto response = new UserCreationDto();

        // Remplir les informations générales de l'utilisateur
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole());
        response.setEstVerifie(user.getEstVerifie());

        // Vérifier le rôle et remplir les informations spécifiques
        if (user.getRole().equals("ROLE_ASSOCIATION")) {
            // Si l'utilisateur est une association
            Association association = associationRepository.findById(user.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Association not found"));

            response.setNomOrganisation(association.getNomOrganisation());
            response.setDescription(association.getDescription());
            response.setAdresse(association.getAdresse());
            response.setSiteWeb(association.getSiteWeb());
            response.setNr(association.getNr());
            response.setDocumentVerification(association.getDocumentVerification());
        } else if (!user.getRole().equals("ROLE_ASSOCIATION")) {
            // Si l'utilisateur est un utilisateur (type "UTILISATEUR")
            Utilisateur utilisateur = utilisateurRepository.findById(user.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Utilisateur not found"));

            response.setNom(utilisateur.getNom());
            response.setPrenom(utilisateur.getPrenom());
            response.setCin(utilisateur.getCin());
        } else {
            // Gestion d'un rôle inconnu
            throw new IllegalArgumentException("Unknown user role: " + user.getRole());
        }

        // Retourner le DTO
        return response;
    }


    public AssociationDto getAssociationById(Long id) {
        Association association = associationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Association not found"));
        return associationMapper.toAssociationDTO(association);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public UtilisateurDto getUtilisateurById(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur not found"));
        return utilisateurMapper.toUtilisateurDto(utilisateur);
    }


    public Optional<User> authenticateUser(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getPassword())) {
            return userOpt;
        }
        return Optional.empty();
    }

    public LoginResponseDto login(LoginResponseDto loginRequest) {
        LoginResponseDto response = new LoginResponseDto();
        try {
            // Authenticate the user using the provided email and password
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(), loginRequest.getPassword()
                    )
            );

            // Fetch the user details from the repository
            var user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Generate JWT and refresh tokens
            var jwt = jwtUtils.generateToken(user);
            var refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user);

            // Populate the response DTO with the appropriate details
            response.setUserId(user.getId());
            response.setStatusCode(200);
            response.setToken(jwt);
            response.setRole(user.getRole());
            response.setEmail(user.getEmail());
            response.setRefreshToken(refreshToken);

            if (!user.getRole().equals("ROLE_ASSOCIATION") ) {
                Optional<Utilisateur> utilisateur = utilisateurRepository.findById(user.getId());
                response.setNom(utilisateur.get().getNom());
                response.setPrenom(utilisateur.get().getPrenom());
            } else if (user.getRole().equals("ROLE_ASSOCIATION")){
                Optional<Association> association = associationRepository.findById(user.getId());
                response.setNom(association.get().getNomOrganisation());
                response.setPrenom(association.get().getDescription());

            }

            response.setExpirationTime("24Hrs");
            response.setMessage("Connexion réussie");

            return response;

        } catch (UsernameNotFoundException e) {
            // Handle case when the user is not found
            response.setStatusCode(404);
            response.setMessage("Utilisateur non trouvé: " + e.getMessage());
            return response;

        } catch (BadCredentialsException e) {
            // Return HTTP 401 instead of 200 with an error message
            response.setStatusCode(401);
            response.setMessage("Identifiants incorrects: " + e.getMessage());
            return response;

        } catch (Exception e) {
            // Handle any other exceptions that might occur
            response.setStatusCode(500);
            response.setMessage("Une erreur est survenue: " + e.getMessage());
            return response;
        }
    }



    /* delete user */
    public void deleteUser(Long id) {
        // Vérifier si l'utilisateur existe
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Supprimer l'utilisateur
        userRepository.delete(user);
    }

    /* change user status */
    public UserDto changeUserStatus(Long id) {
        // Récupérer l'utilisateur par ID
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Modifier le statut "estVerifie" de l'utilisateur
        user.setEstVerifie(true);

        // Sauvegarder les changements
        userRepository.save(user);

        // Retourner le DTO de type UserDto
        return userMapper.toDto(user);
    }



    /* get all users */

    public List<UserCreationDto> getAllUsers() {
        // Récupérer tous les utilisateurs
        List<User> users = userRepository.findAll();

        // Transformer les entités en DTO
        return users.stream().map(user -> {
            UserCreationDto dto = new UserCreationDto();
            dto.setId(user.getId());
            dto.setEmail(user.getEmail());
            dto.setPhone(user.getPhone());
            dto.setRole(user.getRole());
            dto.setEstVerifie(user.getEstVerifie());

            // Ajouter des informations spécifiques selon le rôle
            if ("ROLE_ASSOCIATION".equals(user.getRole())) {
                Association association = associationRepository.findById(user.getId())
                        .orElseThrow(() -> new EntityNotFoundException("Association not found"));
                dto.setNomOrganisation(association.getNomOrganisation());
                dto.setDescription(association.getDescription());
                dto.setAdresse(association.getAdresse());
                dto.setSiteWeb(association.getSiteWeb());
                dto.setNr(association.getNr());
                dto.setDocumentVerification(association.getDocumentVerification());
            } else if (!"ROLE_ASSOCIATION".equals(user.getRole())) {
                Utilisateur utilisateur = utilisateurRepository.findById(user.getId())
                        .orElseThrow(() -> new EntityNotFoundException("Utilisateur not found"));
                dto.setNom(utilisateur.getNom());
                dto.setPrenom(utilisateur.getPrenom());
                dto.setCin(utilisateur.getCin());
            }
            return dto;
        }).collect(Collectors.toList());
    }









    /*


    public UserDto registerUser(UserDto userDto) {
        Optional<User> existingUser = userRepository.findByEmail(userDto.getEmail());
        if (existingUser.isPresent()) {
            throw new UserAlreadyExistsException("User with email " + userDto.getEmail() + " already exists.");
        }

        User user = userMapper.toEntity(userDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setDateCreation(LocalDateTime.now());
        user = userRepository.save(user);
        return userMapper.toDto(user);
    }
    public UserDto registerUser_(UserDto userDto) {
        User user = userMapper.toEntity(userDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER"); // Default role
        user.setDateCreation(LocalDateTime.now());
        user = userRepository.save(user);
        return userMapper.toDto(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> authenticateUser(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getPassword())) {
            return userOpt;
        }
        return Optional.empty();
    }

    public UserDto login(UserDto loginRequest) {
        UserDto response = new UserDto();
        try {
            // Authenticate the user using the provided email and password
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(), loginRequest.getPassword()
                    )
            );

            // Fetch the user details from the repository
            var user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Generate JWT and refresh tokens
            var jwt = jwtUtils.generateToken(user);
            var refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user);

            // Populate the response DTO with the appropriate details
            response.setStatusCode(200);
            response.setToken(jwt);
            response.setRole(user.getRole());
            response.setNom(user.getNom());
            response.setPrenom(user.getPrenom());
            response.setRefreshToken(refreshToken);
            response.setExpirationTime("24Hrs");
            response.setMessage("Connexion réussie");

            return response;

        } catch (UsernameNotFoundException e) {
            // Handle case when the user is not found
            response.setStatusCode(404);
            response.setMessage("Utilisateur non trouvé: " + e.getMessage());
            return response;

        } catch (BadCredentialsException e) {
            // Return HTTP 401 instead of 200 with an error message
            response.setStatusCode(401);
            response.setMessage("Identifiants incorrects: " + e.getMessage());
            return response;

        } catch (Exception e) {
            // Handle any other exceptions that might occur
            response.setStatusCode(500);
            response.setMessage("Une erreur est survenue: " + e.getMessage());
            return response;
        }
    }



    public UserDto refreshToken(UserDto refreshTokenReqiest){
        UserDto response = new UserDto();
        try{
            String ourEmail = jwtUtils.extractUsername(refreshTokenReqiest.getToken());
            User users = userRepository.findByEmail(ourEmail).orElseThrow();
            if (jwtUtils.isTokenValid(refreshTokenReqiest.getToken(), users)) {
                var jwt = jwtUtils.generateToken(users);
                response.setStatusCode(200);
                response.setToken(jwt);
                response.setRefreshToken(refreshTokenReqiest.getToken());
                response.setExpirationTime("24Hr");
                response.setMessage("Successfully Refreshed Token");
            }
            response.setStatusCode(200);
            return response;

        }catch (Exception e){
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
            return response;
        }
    }


   public List<UserDto> findUserByCriteria(UserCriteria userCriteria) throws FunctionalException {
        List<User> userList = userRepository.findAll((Specification<User>) (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userCriteria.getId() != null) {
                predicates.add(builder.equal(root.get("id"), userCriteria.getId()));
            }
            if (userCriteria.getNom() != null) {
                predicates.add(builder.like(builder.lower(root.get("nom")), "%" + userCriteria.getNom().toLowerCase() + "%"));
            }
            if (userCriteria.getPrenom() != null) {
                predicates.add(builder.like(builder.lower(root.get("prenom")), "%" + userCriteria.getPrenom().toLowerCase() + "%"));
            }
            if (userCriteria.getPhone() != null) {
                predicates.add(builder.equal(root.get("phone"), userCriteria.getPhone()));
            }
            if (userCriteria.getEmail() != null) {
                predicates.add(builder.equal(root.get("email"), userCriteria.getEmail()));
            }
            if (userCriteria.getPassword() != null) {
                predicates.add(builder.equal(root.get("password"), userCriteria.getPassword()));
            }
            if (userCriteria.getRole() != null) {
                predicates.add(builder.equal(root.get("role"), userCriteria.getRole()));
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        });

        // Convertir la liste des utilisateurs en liste de UserDto
        List<UserDto> userDtoList = new ArrayList<>();
        for (User user : userList) {
            userDtoList.add(userMapper.toDto(user));
        }
        return userDtoList;
    }


    public UserDto findUserById(Long id) throws FunctionalException {
            UserCriteria userCriteria = UserCriteria.builder()
                    .id(id)
                    .build();
            List<UserDto> userDtoList = findUserByCriteria(userCriteria);
            if (userDtoList != null && !userDtoList.isEmpty()) {
                return userDtoList.get(0);
            } else {
                throw new FunctionalException("L'utilisateur avec l'id " + id + " n'existe pas.");
            }
        }

    public UserDto updateUser(Long id, UserDto userDto) throws FunctionalException {
        // Trouver l'utilisateur existant par ID
        UserDto existingUserDto = findUserById(id);

        // Mettre à jour les champs nécessaires
        userDto.setId(id);  // S'assurer que l'ID est correctement mis à jour
        userDto.setDateCreation(existingUserDto.getDateCreation());
        userDto.setDateUpdate(LocalDateTime.now());
        // Enregistrer l'utilisateur mis à jour
        User updatedUser = userRepository.save(userMapper.toEntity(userDto));

        // Mapper l'entité mise à jour en DTO
        return userMapper.toDto(updatedUser);
    }

    public ResponseDto deleteUserById(Long id) throws FunctionalException {
        ResponseDto responseDto = new ResponseDto();

        // Trouver l'utilisateur par ID
        UserDto userDto = findUserById(id);
        userRepository.deleteById(id);

        responseDto.setMessage("L'utilisateur " + userDto.getNom() + " " + userDto.getPrenom() + " a bien été supprimé");
        return responseDto;
    }


    public UserDto getMyInfo(String email){
        UserDto reqRes = new UserDto();
        try {
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isPresent()) {
                reqRes.setUsers(userOptional.get());
                reqRes.setStatusCode(200);
                reqRes.setMessage("successful");
            } else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("User not found for update");
            }

        }catch (Exception e){
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occurred while getting user info: " + e.getMessage());
        }
        return reqRes;

    }



     */

}