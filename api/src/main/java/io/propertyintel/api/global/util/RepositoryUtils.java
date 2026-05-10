package io.propertyintel.api.global.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class RepositoryUtils {

    public Pageable buildPageable(Integer limit, Sort sort, Integer pageNumber) {
        int size = (limit != null && limit > 0) ? limit : 20;   // default value
        return PageRequest.of(pageNumber, size, sort);
    }
}
