package com.ozz.atlas.auth.search.init;


import com.ozz.atlas.auth.search.repository.UserSearchRepository;
import com.ozz.atlas.auth.search.service.UserSearchService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class UserSearchIndexer implements ApplicationRunner {

    private final UserSearchRepository userSearchRepository;
    private final UserSearchService userSearchService;

    public UserSearchIndexer(UserSearchRepository userSearchRepository,
                             UserSearchService userSearchService) {
        this.userSearchRepository = userSearchRepository;
        this.userSearchService = userSearchService;
    }

    @Override
    public void run(ApplicationArguments args) {
        userSearchService.reindexAllUsers();
    }
}