package com.igrowker.feature.parkify.common.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Service
public class UriBuilderServiceImpl implements UriBuilderService {
    @Override
    public URI buildUserLocationUri(String userId) {
        return ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/users/{id}")
                .buildAndExpand(userId)
                .toUri();
    }
}
