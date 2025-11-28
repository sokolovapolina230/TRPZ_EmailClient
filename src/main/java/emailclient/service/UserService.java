package emailclient.service;

import emailclient.model.User;
import emailclient.repository.UserRepository;

public class UserService {

    private final UserRepository repo = new UserRepository();

    public User login(String username, String password) {
        User u = repo.getByUsername(username);
        if (u == null) return null;
        return u.getPassword().equals(password) ? u : null;
    }

    public User register(String username, String password) {
        return repo.createUser(username, password);
    }
}

