package com.docto.protechdoctolib.user;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.docto.protechdoctolib.registration.token.ConfirmationToken;
import com.docto.protechdoctolib.registration.token.ConfirmationTokenRepository;
import com.docto.protechdoctolib.rendez_vous.Rendez_vous;
import com.docto.protechdoctolib.rendez_vous.Rendez_vousDAO;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.List;

import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@CrossOrigin //to allow cross-origin request from the vue application to the backend (hosted on the same computer)
@RestController
@RequestMapping("/api/users")
@Transactional
public class UserController {

    private final UserRepository userDAO;

    private final Rendez_vousDAO rendez_vousDAO;

    private final ConfirmationTokenRepository confirmationTokenRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserController(UserRepository userDAO, Rendez_vousDAO rendez_vousDAO, ConfirmationTokenRepository confirmationTokenRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userDAO = userDAO;
        this.rendez_vousDAO = rendez_vousDAO;
        this.confirmationTokenRepository = confirmationTokenRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    /**
     * Donne la liste de tous les utilisateurs
     *
     * @return une liste de tous les créneaux
     */
    @GetMapping(path = "/admin")
    public List<UserDTO> findAll() {
        return userDAO.findAll().stream().map(UserDTO::new).collect(Collectors.toList());
    }

    /**
     * Renvoi toutes les infos utiles au front-end en récupérant l'id dans le header
     * @return
     */
    @GetMapping(path = "/user/getbyId")
    public UserDTO findById(HttpServletRequest request) {
        //get the id of the user with the token
        String acces_token = request.getHeader(AUTHORIZATION).substring("Bearer ".length());
        Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(acces_token);
        Long id= Long.parseLong(decodedJWT.getKeyId());

        UserDTO userDTO = userDAO.findById(id).map(UserDTO::new).orElse(null);
        if (userDTO == null) {
            throw new ResponseStatusException( //if not found throw 404 error
                    HttpStatus.NOT_FOUND, "user not found"
            );
        } else {
            return userDTO;
        }

    }

    /**
     * Give user infos by id
     * @param id
     * @return
     */
    @GetMapping(path = "/admin/{id}")
    public UserDTO findById(@PathVariable Long id) {
        UserDTO creneauId = userDAO.findById(id).map(UserDTO::new).orElse(null);
        if (creneauId == null) {
            throw new ResponseStatusException( //if not found throw 404 error
                    HttpStatus.NOT_FOUND, "entity not found"
            );
        } else {
            return creneauId;
        }

    }


    /**
     * Delete all user information, activation token and appointements taken by the user making the request
     * @param request
     */
    @DeleteMapping("/user")
    public void deleteParId(HttpServletRequest request) {
        //get the id of the person making the request
        String acces_token = request.getHeader(AUTHORIZATION).substring("Bearer ".length());
        Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(acces_token);
        Long id= Long.parseLong(decodedJWT.getKeyId());

        List<ConfirmationToken> aa= confirmationTokenRepository.findCrenauxByUserId(id); //get all confirmation tokens related to the user
        List<Rendez_vous> bb = rendez_vousDAO.findAllByIdUser(id); //get all appointments related to the user
        for (int j=0; j<bb.size();j++){ //delete each appointements
            rendez_vousDAO.deleteById(bb.get(j).getId());
        }
        for (int j=0; j<aa.size();j++){ //delete each confirmation token
            confirmationTokenRepository.deleteById(aa.get(j).getId());
        }

        userDAO.deleteById(id); //delete the user

    }

    @GetMapping(path = "/user/submit/{id}")
    public UserDTO findByIdSubmited(@PathVariable Long id,HttpServletRequest request) {
        UserDTO userDTO = userDAO.findById(id).map(UserDTO::new).orElse(null);
        if (userDTO == null) {
            throw new ResponseStatusException( //if not found throw 404 error
                    HttpStatus.NOT_FOUND, "user not found"
            );
        } else {
            return userDTO;
        }

    }


    /**
     * Prend un dto de User en paramètre,et modifier l'utilisateur en question avec les paramètres
     *
     * @param dto
     * @return le dto du créneau crée
     */
    @PostMapping("/user/modify") // (8)
    public UserDTO modify(@RequestBody UserDTO dto, HttpServletRequest request) {
        User user = null;

        //Get the correct userid
        String acces_token = request.getHeader(AUTHORIZATION).substring("Bearer ".length());
        Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(acces_token);
        dto.setId(Long.parseLong(decodedJWT.getKeyId()));

        try {
            user = userDAO.getReferenceById(dto.getId());  // Assign each of the new values
            user.setPrenom(dto.getFirstName());
            user.setNom(dto.getLastName());
            user.setPhonenumber(dto.getPhoneNumber());
            user.setSkypeAccount(dto.getSkypeAccount());
            user.setCampus(dto.getCampus());
            if(!dto.getPassword().equals("null")) { //to avoid accidentally modifying the password;
                user.setPassword(bCryptPasswordEncoder.encode(dto.getPassword()));
            }

        } catch (EntityNotFoundException e) { //if slot not found, throw 404 error
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "user id not found"
            );
        }

        return new UserDTO(user);
    }
}