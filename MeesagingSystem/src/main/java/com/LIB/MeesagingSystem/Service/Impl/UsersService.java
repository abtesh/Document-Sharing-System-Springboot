package com.LIB.MeesagingSystem.Service.Impl;


import com.LIB.MeesagingSystem.Model.Users;
import com.LIB.MeesagingSystem.Repository.UserRepository;
import com.LIB.MeesagingSystem.exceptions.AccountBlockedException;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsersService {
    private final UserRepository usersRepository;

    public Users loadUserByUsername(String username) {
        return usersRepository.findByEmailAndIsActive(username,true).orElse(null);
    }

    public void saveUsers(String username, String name, String id){
        username=username.toLowerCase();
        Users users = usersRepository.findByEmail(username).orElse(null);
        if(users == null){
            Users user = Users.builder().id(id).email(username).isActive(true).name(name).build();
            usersRepository.save(user);
        }
        else{
            if(users.isActive())
                return;
            else{
                throw new AccountBlockedException("your account is blocked");
            }
        }

    }
}
