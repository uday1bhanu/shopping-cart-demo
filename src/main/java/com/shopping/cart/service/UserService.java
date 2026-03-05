package com.shopping.cart.service;

import com.shopping.cart.model.User;

public interface UserService {

    User getCurrentUser();

    User getUserById(String userId);
}
