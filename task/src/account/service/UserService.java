package account.service;

import account.dto.UserDTO;
import account.responses.ChangePassResponse;
import account.responses.UserResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserService extends UserDetailsService {

    UserResponse addEmployee(UserDTO userDTO);

    UserResponse getUser(String user);

    ChangePassResponse updatePassword(String userName, String newPassword);
}
