package universalgroup.UTaxi.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import universalgroup.UTaxi.Entity.Role;
import universalgroup.UTaxi.Entity.User;
import universalgroup.UTaxi.Entity.template.Attachment;
import universalgroup.UTaxi.Entity.template.AttachmentContent;
import universalgroup.UTaxi.Exeptions.ResourceNotFoundException;
import universalgroup.UTaxi.Pyload.ApiResponse;
import universalgroup.UTaxi.Pyload.RegisterDto;
import universalgroup.UTaxi.Repository.AttachmentContentRepository;
import universalgroup.UTaxi.Repository.AttachmentRepository;
import universalgroup.UTaxi.Repository.AuthRepository;
import universalgroup.UTaxi.Repository.RoleRepository;
import universalgroup.UTaxi.security.JwtTokenProvider;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthRepository authRepository;
    private final AttachmentRepository attachmentRepository;
    private final AttachmentContentRepository attachmentContentRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public UserDetails loadUserByUsername(String phoneNumber) throws UsernameNotFoundException {
        return authRepository.findUserByPhoneNumber(phoneNumber).orElseThrow(() -> new UsernameNotFoundException("getUser"));
    }

    public ApiResponse<?> register(RegisterDto registerDto) {
        try {
            Role getRole = roleRepository.findById(3).orElseThrow(() -> new ResourceNotFoundException(404, "user", "id", registerDto.getUserId()));
            if (!authRepository.existsByPhoneNumber(registerDto.getPhoneNumber())) {
                User build = User.builder()
                        .firstName("Undefined")
                        .lastName("Undefined")
                        .phoneNumber(registerDto.getPhoneNumber())
                        .email("Undefined")
                        .password("root1234")
                        .payed(true)
                        .roles(Collections.singleton(getRole))
                        .enabled(true)
                        .accountNonLocked(true)
                        .accountNonExpired(true)
                        .credentialsNonExpired(true)
                        .build();
                authRepository.save(build);
                return new ApiResponse<>("Registratsyadan o'tdingiz", true, 200, build);
            }
            return new ApiResponse<>("xato", false, 404);
        } catch (Exception e) {
            return new ApiResponse<>("xato " + e.getMessage(), false, 500);
        }
    }


    public UserDetails getUserById(UUID id) {
        return authRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(404, "user", "id", id));
    }

    public ApiResponse<?> addPhoto(UUID photoId, UUID userId) {
        try {
            User user = authRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException(404, "user", "id", userId));
            if (user.getPhotoId() == null) {
                user.setPhotoId(photoId);
                authRepository.save(user);
                return new ApiResponse<>("saqlandi", true);
            } else {
                AttachmentContent byAttachmentId = attachmentContentRepository.findByAttachmentId(user.getPhotoId());
                Attachment get1 = attachmentRepository.findById(user.getPhotoId()).orElseThrow((() -> new ResourceNotFoundException(404, "attachment", "id", photoId)));
                attachmentContentRepository.delete(byAttachmentId);
                attachmentRepository.delete(get1);
                user.setPhotoId(photoId);
                authRepository.save(user);
                return new ApiResponse<>("saqlandi", true);
            }
        } catch (Exception e) {
            return new ApiResponse<>("Xato", true);
        }
    }

    public ApiResponse<?> editFullName(UUID id, RegisterDto reqRegister) {
        try {
            Optional<User> byId = authRepository.findById(id);
            if (byId.isPresent()) {
                User user = byId.get();
                switch (reqRegister.getMalumot()) {
                    case "name and surname" -> {
                        user.setFirstName(reqRegister.getFirstName());
                        user.setLastName(reqRegister.getLastName());
                    }
                    case "phoneNumber" -> user.setPhoneNumber(reqRegister.getPhoneNumber());
                    case "password" -> {
                        if (reqRegister.getPassword().equals(reqRegister.getPrePassword())) {
                            user.setPassword(passwordEncoder().encode(reqRegister.getPassword()));
                        }
                    }
                    case "image" -> user.setPhotoId(reqRegister.getImg());
                }
                authRepository.save(user);
                return new ApiResponse<>("successfully edited fullName", true, 200, user);
            } else {
                return new ApiResponse<>("bunday user mavjud emas", false, 404);
            }
        } catch (Exception e) {
            return new ApiResponse<>("xatolik" + e.getMessage(), false, 500);
        }
    }

    public ApiResponse<?> addDriver(UUID id, RegisterDto registerDto) {
        try {
            User user = authRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(404, "user", "id", id));
            Role role = roleRepository.findById(1).orElseThrow(() -> new ResourceNotFoundException(404, "role", "id", 1));
            Role driver = roleRepository.findById(2).orElseThrow(() -> new ResourceNotFoundException(404, "role", "id", 1));
            for (Role userRole : user.getRoles()) {
                if (role.equals(userRole)) {
                    User build = User.builder()
                            .firstName(registerDto.getFirstName())
                            .lastName(registerDto.getLastName())
                            .phoneNumber(registerDto.getPhoneNumber())
                            .password(passwordEncoder().encode(registerDto.getPassword()))
                            .roles(Collections.singleton(driver))
                            .enabled(true)
                            .accountNonLocked(true)
                            .accountNonExpired(true)
                            .credentialsNonExpired(true)
                            .build();
                    authRepository.save(build);
                    return new ApiResponse<>("Shifokor saqlandi", true, 200, build);
                }
            }
            return new ApiResponse<>("Siz admin emassiz ", false, 500);
        } catch (Exception e) {
            return new ApiResponse<>("Shifokor saqlashda hatoli " + e.getMessage(), false, 500);
        }
    }

    public ApiResponse<?> deleteDriver(UUID id) {
        try {
            User user = authRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(404, "user", "id", id));
            authRepository.delete(user);
            return new ApiResponse<>("Shifokor o'chirildi", true, 200);
        } catch (Exception e) {
            return new ApiResponse<>("Shifokor saqlashda xatolik", false, 500);
        }
    }

    public List<User> getDriver() {
        try {
            Role driver = roleRepository.findById(2).orElseThrow(() -> new ResourceNotFoundException(404, "role", "id", 1));
            List<User> drivers = new ArrayList<>();
            for (User user : authRepository.findAll()) {
                for (Role role : user.getRoles()) {
                    if (role.equals(driver)) {
                        drivers.add(user);
                    }
                }

            }
            return drivers;
        } catch (Exception e) {
            return null;
        }
    }

    public ApiResponse<?> setPay(UUID userId, boolean isPayed) {
        try {
            User user = authRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException(404, "user", "id", userId));
            user.setPayed(isPayed);
            User save = authRepository.save(user);
            return new ApiResponse<>("saqlandi", true, 200, save);
        } catch (Exception e) {
            return new ApiResponse<>("xatolik " + e.getMessage(), false, 500);
        }

    }
}

