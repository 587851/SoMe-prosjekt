package com.example.someprojectbackend.web;

import com.example.someprojectbackend.repo.UserRepository;
import com.example.someprojectbackend.security.JwtUtil;
import com.example.someprojectbackend.domain.User;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * REST-controller for filopplastinger.
 * <p>
 * For nå støttes kun avatar-opplasting.
 * Filene lagres under "./uploads/avatars/{userId}/".
 */
@RestController
@RequestMapping("/api/files")
public class FileController {

    private final JwtUtil jwtUtil;
    private final UserRepository users;

    public FileController(JwtUtil jwtUtil, UserRepository users) {
        this.jwtUtil = jwtUtil;
        this.users = users;
    }

    /**
     * Laster opp en ny avatar for innlogget bruker.
     * <p>
     * POST /api/files/avatar
     * <p>
     * - Krever Authorization-header ("Bearer <token>").
     * - Filer lagres under "./uploads/avatars/{userId}/".
     * - Filnavn genereres som "avatar_<timestamp>.png".
     * - Relativ sti lagres i databasen på brukeren.
     *
     * @param file       selve bildefilen (multipart/form-data)
     * @param authHeader Authorization-header med JWT
     * @return JSON med nøkkelen "avatarPath"
     * @throws IOException hvis filskriving feiler
     */
    @PostMapping(
            value = "/avatar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public String uploadAvatar(
            @RequestPart("file") MultipartFile file,
            @RequestHeader("Authorization") String authHeader) throws IOException {

        String email = jwtUtil.extractEmail(authHeader);
        User user = users.findByEmail(email).orElseThrow(() ->
                new IllegalArgumentException("User not found for email " + email));

        Path uploadDir = Path.of("uploads", "avatars", user.getId().toString());
        Files.createDirectories(uploadDir);

        String filename = "avatar_" + System.currentTimeMillis() + ".png";
        Path filePath = uploadDir.resolve(filename);

        file.transferTo(filePath);

        user.setAvatarKey("/files/avatars/" + user.getId() + "/" + filename);
        users.save(user);

        return "{ \"avatarPath\": \"" + user.getAvatarKey() + "\" }";
    }
}
