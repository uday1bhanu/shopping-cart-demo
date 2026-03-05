package com.shopping.cart.service;

import com.shopping.cart.dto.request.LoginRequest;
import com.shopping.cart.dto.request.SignupRequest;
import com.shopping.cart.dto.response.JwtResponse;
import com.shopping.cart.dto.response.MessageResponse;

public interface AuthService {

    MessageResponse signup(SignupRequest request);

    JwtResponse login(LoginRequest request);
}
