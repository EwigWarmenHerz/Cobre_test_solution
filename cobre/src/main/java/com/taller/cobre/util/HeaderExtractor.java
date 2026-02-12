package com.taller.cobre.util;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebInputException;

import static com.taller.cobre.domain.model.constants.CommonConstants.SECRET_KEY_INPUT_HEADER;

public class HeaderExtractor {

    public static String extractSecretKey(ServerRequest request) {
       var secretHeader = request.headers()
            .firstHeader(SECRET_KEY_INPUT_HEADER);
        System.out.printf("SECRET header: " + secretHeader);
       if(secretHeader == null || secretHeader.isBlank()){
           throw new ServerWebInputException("Bad request");
       }
       return secretHeader;
    }
}
