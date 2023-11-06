package com.danteso.chromeextensionapiapplication.security.repo;

import com.danteso.chromeextensionapiapplication.security.entity.User;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface UserRepository extends CrudRepository<User, UUID> {

    User findByUsername(String username);

    User findByTelegramChatId(Long chatId);

}
